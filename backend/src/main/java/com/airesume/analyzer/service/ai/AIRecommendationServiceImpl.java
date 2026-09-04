package com.airesume.analyzer.service.ai;

import com.airesume.analyzer.dto.AIRecommendationDto;
import com.airesume.analyzer.dto.AIRecommendationResponse;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.AIRecommendationMapper;
import com.airesume.analyzer.repository.*;
import com.airesume.analyzer.service.ai.parser.AIResponseParser;
import com.airesume.analyzer.service.ai.provider.AIProvider;
import com.airesume.analyzer.service.ai.provider.AIProviderFactory;
import com.airesume.analyzer.service.ai.provider.RuleFallbackProvider;
import com.airesume.analyzer.service.ai.prompt.PromptBuilder;
import com.airesume.analyzer.service.ai.prompt.PromptTemplates;
import com.airesume.analyzer.service.parser.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationServiceImpl implements AIRecommendationService {

    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;
    private final PromptBuilder promptBuilder;
    private final AIProviderFactory aiProviderFactory;
    private final AIRecommendationMapper aiRecommendationMapper;
    private final AIResponseParser aiResponseParser;
    private final RuleFallbackProvider ruleFallbackProvider;

    @Override
    @Transactional
    public AIRecommendationDto generateAIRecommendations(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to generate AI recommendations for this resume.");
        }

        // Auto-parse if parsed resume does not exist (centralized and thread-safe)
        ParsedResume parsedResume = resumeParserService.getOrParseEntity(resumeId, currentUserEmail);

        Optional<AnalysisReport> atsOpt = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId);
        Optional<GrammarAnalysis> grammarOpt = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId);
        Optional<MatchAnalysis> matchOpt = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        String prompt = promptBuilder.buildPrompt(parsedResume, atsOpt.orElse(null), grammarOpt.orElse(null), matchOpt.orElse(null));

        AIProvider provider = aiProviderFactory.getActiveProvider();
        log.info("Generating AI recommendations using provider: {}", provider.getProviderName());

        String rawResumeText = parsedResume != null ? parsedResume.getRawText() : "";
        String aiResponseText = provider.generateCompletionWithSystem(PromptTemplates.SYSTEM_INSTRUCTION, prompt);

        // Parse AI Completion Output with strict validation & anti-hallucination check
        Optional<AIRecommendationResponse> responseOpt = aiResponseParser.parseAndValidate(aiResponseText, rawResumeText);

        AIRecommendationResponse validatedResponse;
        if (responseOpt.isPresent()) {
            validatedResponse = responseOpt.get();
        } else {
            log.warn("AI completion output from provider '{}' was invalid, incomplete, or ungrounded. Executing RuleFallbackProvider.", provider.getProviderName());
            String fallbackText = ruleFallbackProvider.generateCompletionWithSystem(PromptTemplates.SYSTEM_INSTRUCTION, prompt);
            validatedResponse = aiResponseParser.parseAndValidate(fallbackText, rawResumeText)
                    .orElseThrow(() -> new IllegalStateException("RuleFallbackProvider output failed validation."));
        }

        AIRecommendation entity = AIRecommendation.builder()
                .resume(resume)
                .professionalSummary(validatedResponse.getProfessionalSummary())
                .improvedExperience(validatedResponse.getImprovedExperience())
                .improvedProjects(validatedResponse.getImprovedProjects())
                .improvedSkills(validatedResponse.getImprovedSkills())
                .keywordRecommendations(validatedResponse.getKeywordRecommendations())
                .atsRecommendations(validatedResponse.getAtsRecommendations())
                .grammarRecommendations(validatedResponse.getGrammarRecommendations())
                .interviewPreparation(validatedResponse.getInterviewPreparation())
                .careerSuggestions(validatedResponse.getCareerSuggestions())
                .overallFeedback(validatedResponse.getOverallFeedback())
                .createdAt(LocalDateTime.now())
                .build();

        AIRecommendation saved = aiRecommendationRepository.save(entity);
        return aiRecommendationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AIRecommendationDto getAIRecommendationsByResumeId(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view AI recommendations for this resume.");
        }

        AIRecommendation entity = aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("No AI recommendations found. Click 'Generate AI Suggestions' to run AI engine."));

        return aiRecommendationMapper.toDto(entity);
    }

    @Override
    @Transactional
    public void deleteAIRecommendation(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        AIRecommendation entity = aiRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI Recommendation not found with ID: " + id));

        if (!entity.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this recommendation.");
        }

        aiRecommendationRepository.delete(entity);
    }
}
