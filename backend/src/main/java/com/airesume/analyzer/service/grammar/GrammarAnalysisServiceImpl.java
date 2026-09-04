package com.airesume.analyzer.service.grammar;

import com.airesume.analyzer.dto.GrammarAnalysisDto;
import com.airesume.analyzer.entity.GrammarAnalysis;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.GrammarAnalysisMapper;
import com.airesume.analyzer.repository.GrammarAnalysisRepository;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.service.parser.ResumeParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GrammarAnalysisServiceImpl implements GrammarAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;
    private final GrammarAnalysisEngine grammarAnalysisEngine;
    private final GrammarAnalysisMapper grammarAnalysisMapper;

    @Override
    @Transactional
    public GrammarAnalysisDto analyzeGrammar(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to analyze this resume.");
        }

        // Auto-parse if parsed resume does not exist (centralized and thread-safe)
        ParsedResume parsedResume = resumeParserService.getOrParseEntity(resumeId, currentUserEmail);

        // Execute Grammar Engine
        GrammarAnalysisEngine.GrammarEngineResult result = grammarAnalysisEngine.analyze(parsedResume);

        GrammarAnalysis entity = GrammarAnalysis.builder()
                .resume(resume)
                .grammarScore(result.getGrammarScore())
                .readabilityScore(result.getReadabilityScore())
                .spellingErrorCount(result.getSpellingErrorCount())
                .grammarErrorCount(result.getGrammarErrorCount())
                .passiveVoiceCount(result.getPassiveVoiceCount())
                .repeatedWordCount(result.getRepeatedWordCount())
                .longSentenceCount(result.getLongSentenceCount())
                .weakVerbCount(result.getWeakVerbCount())
                .formattingIssueCount(result.getFormattingIssueCount())
                .spellingErrors(grammarAnalysisMapper.toJson(result.getSpellingErrors()))
                .grammarErrors(grammarAnalysisMapper.toJson(result.getGrammarErrors()))
                .weakVerbs(grammarAnalysisMapper.toJson(result.getWeakVerbs()))
                .passiveVoiceInstances(grammarAnalysisMapper.toJson(result.getPassiveVoiceInstances()))
                .suggestions(grammarAnalysisMapper.toJson(result.getSuggestions()))
                .analysisDate(LocalDateTime.now())
                .build();

        GrammarAnalysis saved = grammarAnalysisRepository.save(entity);
        return grammarAnalysisMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GrammarAnalysisDto getGrammarAnalysisByResumeId(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this report.");
        }

        GrammarAnalysis entity = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("No grammar analysis found for this resume. Click 'Analyze Grammar' to run."));

        return grammarAnalysisMapper.toDto(entity);
    }

    @Override
    @Transactional
    public void deleteGrammarAnalysis(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        GrammarAnalysis entity = grammarAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grammar analysis report not found with ID: " + id));

        if (!entity.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this report.");
        }

        grammarAnalysisRepository.delete(entity);
    }
}
