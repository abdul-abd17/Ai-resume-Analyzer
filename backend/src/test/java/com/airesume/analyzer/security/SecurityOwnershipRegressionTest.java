package com.airesume.analyzer.security;

import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.repository.*;
import com.airesume.analyzer.service.ResumeServiceImpl;
import com.airesume.analyzer.service.ai.AIRecommendationServiceImpl;
import com.airesume.analyzer.service.ats.AtsAnalysisServiceImpl;
import com.airesume.analyzer.service.grammar.GrammarAnalysisServiceImpl;
import com.airesume.analyzer.service.job.JobDescriptionServiceImpl;
import com.airesume.analyzer.service.parser.ResumeParserServiceImpl;
import com.airesume.analyzer.service.report.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityOwnershipRegressionTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private ParsedResumeRepository parsedResumeRepository;
    @Mock
    private AnalysisReportRepository analysisReportRepository;
    @Mock
    private GrammarAnalysisRepository grammarAnalysisRepository;
    @Mock
    private MatchAnalysisRepository matchAnalysisRepository;
    @Mock
    private AIRecommendationRepository aiRecommendationRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    @InjectMocks
    private ResumeServiceImpl resumeService;
    @InjectMocks
    private ResumeParserServiceImpl resumeParserService;
    @InjectMocks
    private AtsAnalysisServiceImpl atsAnalysisService;
    @InjectMocks
    private GrammarAnalysisServiceImpl grammarAnalysisService;
    @InjectMocks
    private JobDescriptionServiceImpl jobDescriptionService;
    @InjectMocks
    private AIRecommendationServiceImpl aiRecommendationService;
    @InjectMocks
    private ReportServiceImpl reportService;

    private User ownerUser;
    private User unauthorizedUser;

    private Resume ownerResume;
    private ParsedResume ownerParsedResume;
    private AnalysisReport ownerAtsReport;
    private GrammarAnalysis ownerGrammarReport;
    private MatchAnalysis ownerMatchAnalysis;
    private AIRecommendation ownerAiRecommendation;
    private Report ownerGeneratedReport;
    private JobDescription ownerJobDescription;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id(100L).email("owner@security.com").build();
        unauthorizedUser = User.builder().id(999L).email("attacker@security.com").build();

        ownerResume = Resume.builder().id(1L).user(ownerUser).fileName("resume.pdf").originalFileName("resume.pdf").storagePath("uploads/resumes/resume.pdf").build();
        ownerParsedResume = ParsedResume.builder().id(10L).resume(ownerResume).build();
        ownerAtsReport = AnalysisReport.builder().id(20L).resume(ownerResume).build();
        ownerGrammarReport = GrammarAnalysis.builder().id(30L).resume(ownerResume).build();
        ownerJobDescription = JobDescription.builder().id(40L).user(ownerUser).build();
        ownerMatchAnalysis = MatchAnalysis.builder().id(50L).resume(ownerResume).jobDescription(ownerJobDescription).build();
        ownerAiRecommendation = AIRecommendation.builder().id(60L).resume(ownerResume).build();
        ownerGeneratedReport = Report.builder().id(70L).resume(ownerResume).filePath("uploads/reports/report.pdf").build();
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to Resume resources throws UnauthorizedAccessException")
    void testUnauthorizedResumeAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(ownerResume));

        assertThrows(UnauthorizedAccessException.class, () -> resumeService.getResumeById(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> resumeService.downloadResume(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> resumeService.deleteResume(1L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to Parsed Resume resources throws UnauthorizedAccessException")
    void testUnauthorizedParsedResumeAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findByIdWithLock(1L)).thenReturn(Optional.of(ownerResume));

        assertThrows(UnauthorizedAccessException.class, () -> resumeParserService.parseResume(1L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to ATS Analysis Reports throws UnauthorizedAccessException")
    void testUnauthorizedAtsReportAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(ownerResume));
        when(analysisReportRepository.findById(20L)).thenReturn(Optional.of(ownerAtsReport));

        assertThrows(UnauthorizedAccessException.class, () -> atsAnalysisService.analyzeResume(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> atsAnalysisService.getAnalysisByResumeId(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> atsAnalysisService.deleteAnalysisReport(20L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to Grammar Analysis Reports throws UnauthorizedAccessException")
    void testUnauthorizedGrammarReportAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(ownerResume));
        when(grammarAnalysisRepository.findById(30L)).thenReturn(Optional.of(ownerGrammarReport));

        assertThrows(UnauthorizedAccessException.class, () -> grammarAnalysisService.analyzeGrammar(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> grammarAnalysisService.getGrammarAnalysisByResumeId(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> grammarAnalysisService.deleteGrammarAnalysis(30L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to Job Match Comparisons throws UnauthorizedAccessException")
    void testUnauthorizedJobMatchAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(ownerResume));

        assertThrows(UnauthorizedAccessException.class, () -> jobDescriptionService.compareResume(1L, 40L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> jobDescriptionService.getMatchAnalysisByResume(1L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to AI Recommendations throws UnauthorizedAccessException")
    void testUnauthorizedAiRecommendationAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(ownerResume));
        when(aiRecommendationRepository.findById(60L)).thenReturn(Optional.of(ownerAiRecommendation));

        assertThrows(UnauthorizedAccessException.class, () -> aiRecommendationService.generateAIRecommendations(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> aiRecommendationService.getAIRecommendationsByResumeId(1L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> aiRecommendationService.deleteAIRecommendation(60L, "attacker@security.com"));
    }

    @Test
    @DisplayName("Security Check: Unauthorized access to Generated Evaluation Reports throws UnauthorizedAccessException")
    void testUnauthorizedGeneratedReportAccess() {
        when(userRepository.findByEmail("attacker@security.com")).thenReturn(Optional.of(unauthorizedUser));
        when(reportRepository.findById(70L)).thenReturn(Optional.of(ownerGeneratedReport));

        assertThrows(UnauthorizedAccessException.class, () -> reportService.getReportById(70L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> reportService.getReportPreview(70L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> reportService.downloadReportPdf(70L, "attacker@security.com"));
        assertThrows(UnauthorizedAccessException.class, () -> reportService.deleteReport(70L, "attacker@security.com"));
    }
}
