package com.airesume.analyzer.service.ats;

import com.airesume.analyzer.dto.AnalysisReportDto;
import com.airesume.analyzer.entity.AnalysisReport;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.AnalysisReportMapper;
import com.airesume.analyzer.repository.AnalysisReportRepository;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.SkillKeywordRepository;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.service.parser.ResumeParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtsAnalysisServiceImpl implements AtsAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final SkillKeywordRepository skillKeywordRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;
    private final AtsAnalysisEngine atsAnalysisEngine;
    private final AnalysisReportMapper analysisReportMapper;

    @Override
    @Transactional
    public AnalysisReportDto analyzeResume(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to analyze this resume.");
        }

        // Auto-parse if parsed resume does not exist (centralized and thread-safe)
        ParsedResume parsedResume = resumeParserService.getOrParseEntity(resumeId, currentUserEmail);

        List<SkillKeyword> seedKeywords = skillKeywordRepository.findByStatus("ACTIVE");

        // Execute Java DSA ATS Analysis Engine
        AtsAnalysisEngine.AtsEngineResult result = atsAnalysisEngine.analyze(parsedResume, seedKeywords);

        // Save Analysis Report Entity
        AnalysisReport report = AnalysisReport.builder()
                .resume(resume)
                .overallScore(result.getOverallScore())
                .contactScore(result.getContactScore())
                .summaryScore(result.getSummaryScore())
                .skillsScore(result.getSkillsScore())
                .experienceScore(result.getExperienceScore())
                .educationScore(result.getEducationScore())
                .projectsScore(result.getProjectsScore())
                .certificationScore(result.getCertificationScore())
                .keywordCoverage(result.getKeywordCoverage())
                .missingKeywordCount(result.getMissingKeywordCount())
                .duplicateKeywordCount(result.getDuplicateKeywordCount())
                .resumeStrength(analysisReportMapper.toJson(result.getStrengths()))
                .resumeWeakness(analysisReportMapper.toJson(result.getWeaknesses()))
                .suggestions(analysisReportMapper.toJson(result.getSuggestions()))
                .topSkills(analysisReportMapper.toJson(result.getTopSkills()))
                .missingKeywords(analysisReportMapper.toJson(result.getMissingKeywords()))
                .duplicateKeywords(analysisReportMapper.toJson(result.getDuplicateKeywords()))
                .analysisDate(LocalDateTime.now())
                .build();

        AnalysisReport savedReport = analysisReportRepository.save(report);

        AnalysisReportDto dto = analysisReportMapper.toDto(savedReport);
        dto.setCategorySkillCounts(result.getCategorySkillCounts());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisReportDto getAnalysisByResumeId(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this report.");
        }

        AnalysisReport report = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("No ATS analysis report found for this resume. Click 'Analyze Resume' to run analysis."));

        return analysisReportMapper.toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisReportDto> getAnalysisHistory(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        List<AnalysisReport> reports = analysisReportRepository.findByResumeUserIdOrderByAnalysisDateDesc(user.getId());

        // Use LinkedList queue to process history
        LinkedList<AnalysisReportDto> historyQueue = new LinkedList<>();
        for (AnalysisReport r : reports) {
            historyQueue.addLast(analysisReportMapper.toDto(r));
        }

        return historyQueue;
    }

    @Override
    @Transactional
    public void deleteAnalysisReport(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        AnalysisReport report = analysisReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis report not found with ID: " + id));

        if (!report.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this report.");
        }

        analysisReportRepository.delete(report);
    }
}
