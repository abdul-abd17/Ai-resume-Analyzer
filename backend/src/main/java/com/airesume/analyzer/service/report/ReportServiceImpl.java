package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import com.airesume.analyzer.dto.ReportDto;
import com.airesume.analyzer.dto.ReportPreviewDto;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ReportMapper;
import com.airesume.analyzer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Path REPORT_DIR = Paths.get("uploads/reports").toAbsolutePath().normalize();

    private final ReportRepository reportRepository;
    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final UserRepository userRepository;
    private final PdfGenerator pdfGenerator;
    private final ReportMapper reportMapper;
    private final ReportModelBuilder reportModelBuilder;
    private final HtmlReportGenerator htmlReportGenerator;

    @Override
    @Transactional
    public ReportDto generateReport(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to generate a report for this resume.");
        }

        ParsedResume parsed = parsedResumeRepository.findByResumeId(resumeId).orElse(null);
        AnalysisReport ats = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId).orElse(null);
        GrammarAnalysis grammar = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(resumeId).orElse(null);
        MatchAnalysis match = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId).orElse(null);
        AIRecommendation ai = aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId).orElse(null);

        CanonicalReportModel model = reportModelBuilder.buildCanonicalModel(resume, parsed, ats, grammar, match, ai);

        try {
            Files.createDirectories(REPORT_DIR);
            String reportFileName = "Report_" + resumeId + "_" + System.currentTimeMillis() + ".pdf";
            Path targetFile = REPORT_DIR.resolve(reportFileName);

            byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);
            Files.write(targetFile, pdfBytes);

            Report report = Report.builder()
                    .resume(resume)
                    .reportName("Evaluation Report - " + resume.getOriginalFileName())
                    .reportType("PDF")
                    .generatedBy(currentUserEmail)
                    .generatedDate(LocalDateTime.now())
                    .filePath(targetFile.toString())
                    .fileSize((long) pdfBytes.length)
                    .status("GENERATED")
                    .downloadCount(0)
                    .sharedCount(0)
                    .build();

            Report saved = reportRepository.save(report);
            return reportMapper.toDto(saved);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDto> getReportsForCurrentUser(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        List<Report> reports = reportRepository.findByResumeUserIdOrderByGeneratedDateDesc(user.getId());
        return reports.stream().map(reportMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDto getReportById(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        if (!report.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this report.");
        }

        return reportMapper.toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportPreviewDto getReportPreview(Long id, String currentUserEmail) {
        ReportDto dto = getReportById(id, currentUserEmail);

        Resume resume = resumeRepository.findById(dto.getResumeId()).orElse(null);
        ParsedResume parsed = parsedResumeRepository.findByResumeId(dto.getResumeId()).orElse(null);
        AnalysisReport ats = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(dto.getResumeId()).orElse(null);
        GrammarAnalysis grammar = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(dto.getResumeId()).orElse(null);
        MatchAnalysis match = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(dto.getResumeId()).orElse(null);
        AIRecommendation ai = aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(dto.getResumeId()).orElse(null);

        CanonicalReportModel model = reportModelBuilder.buildCanonicalModel(resume, parsed, ats, grammar, match, ai);
        String htmlContent = htmlReportGenerator.generateHtmlReport(model);

        return ReportPreviewDto.builder()
                .reportInfo(dto)
                .candidateName(model.getCandidateName())
                .candidateEmail(model.getCandidateEmail())
                .summary(model.getSummary())
                .atsScore(model.getAtsScore())
                .grammarScore(model.getGrammarScore())
                .jobMatchPercentage(model.getJobMatchPercentage())
                .topSkills(model.getTopSkills())
                .missingKeywords(model.getMissingKeywords())
                .grammarIssues(model.getGrammarIssues())
                .aiProfessionalSummary(model.getAiProfessionalSummary())
                .aiImprovedExperience(model.getAiImprovedExperience())
                .aiInterviewPreparation(model.getAiInterviewPreparation())
                .htmlContent(htmlContent)
                .build();
    }

    @Override
    @Transactional
    public byte[] downloadReportPdf(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        if (!report.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to download this report.");
        }

        report.setDownloadCount(report.getDownloadCount() + 1);
        reportRepository.save(report);

        try {
            Path file = Paths.get(report.getFilePath());
            if (Files.exists(file)) {
                return Files.readAllBytes(file);
            }

            // Fallback dynamically regenerate if physical PDF file is missing on disk
            ParsedResume parsed = parsedResumeRepository.findByResumeId(report.getResume().getId()).orElse(null);
            AnalysisReport ats = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(report.getResume().getId()).orElse(null);
            GrammarAnalysis grammar = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(report.getResume().getId()).orElse(null);
            MatchAnalysis match = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(report.getResume().getId()).orElse(null);
            AIRecommendation ai = aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(report.getResume().getId()).orElse(null);

            CanonicalReportModel model = reportModelBuilder.buildCanonicalModel(report.getResume(), parsed, ats, grammar, match, ai);
            byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);

            // Re-save file to disk
            try {
                Files.createDirectories(file.getParent());
                Files.write(file, pdfBytes);
            } catch (Exception ignored) {}

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read report PDF file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteReport(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        if (!report.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this report.");
        }

        try {
            Path filePath = Paths.get(report.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {}

        reportRepository.delete(report);
    }
}
