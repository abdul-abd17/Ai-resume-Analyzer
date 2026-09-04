package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import com.airesume.analyzer.dto.ReportDto;
import com.airesume.analyzer.dto.ReportPreviewDto;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ReportMapper;
import com.airesume.analyzer.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;
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
    private UserRepository userRepository;
    @Mock
    private PdfGenerator pdfGenerator;
    @Mock
    private ReportMapper reportMapper;

    @Spy
    private ReportModelBuilder reportModelBuilder = new ReportModelBuilder();
    @Spy
    private HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator();

    @InjectMocks
    private ReportServiceImpl reportService;

    private User ownerUser;
    private User otherUser;
    private Resume testResume;
    private Report testReport;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id(1L).email("owner@example.com").build();
        otherUser = User.builder().id(2L).email("other@example.com").build();
        testResume = Resume.builder().id(10L).user(ownerUser).originalFileName("Jane_Doe_Resume.pdf").build();

        testReport = Report.builder()
                .id(100L)
                .resume(testResume)
                .reportName("Evaluation Report - Jane_Doe_Resume.pdf")
                .filePath("uploads/reports/test_report.pdf")
                .generatedBy("owner@example.com")
                .generatedDate(LocalDateTime.now())
                .downloadCount(0)
                .build();
    }

    @Test
    @DisplayName("Should successfully generate report and return DTO")
    void testGenerateReport_Success() throws Exception {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));
        when(pdfGenerator.generatePdfReportBytes(any())).thenReturn("PDF_BYTES".getBytes());
        when(reportRepository.save(any())).thenReturn(testReport);
        when(reportMapper.toDto(any())).thenReturn(ReportDto.builder().id(100L).resumeId(10L).reportName("Report").build());

        ReportDto result = reportService.generateReport(10L, "owner@example.com");

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(reportRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when user attempts to access another user's report")
    void testUnauthorizedReportAccess() {
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(reportRepository.findById(100L)).thenReturn(Optional.of(testReport));

        assertThrows(UnauthorizedAccessException.class, () -> reportService.getReportById(100L, "other@example.com"));
        assertThrows(UnauthorizedAccessException.class, () -> reportService.downloadReportPdf(100L, "other@example.com"));
        assertThrows(UnauthorizedAccessException.class, () -> reportService.deleteReport(100L, "other@example.com"));
    }

    @Test
    @DisplayName("Should dynamically regenerate PDF bytes when physical file on disk is missing")
    void testMissingPhysicalPdfFile_DynamicRegeneration() throws Exception {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(reportRepository.findById(100L)).thenReturn(Optional.of(testReport));
        when(pdfGenerator.generatePdfReportBytes(any())).thenReturn("REGENERATED_PDF_BYTES".getBytes());

        // File path "uploads/reports/non_existent.pdf" does not exist on disk
        testReport.setFilePath("uploads/reports/non_existent_" + System.currentTimeMillis() + ".pdf");

        byte[] pdfBytes = reportService.downloadReportPdf(100L, "owner@example.com");

        assertNotNull(pdfBytes);
        assertArrayEquals("REGENERATED_PDF_BYTES".getBytes(), pdfBytes);
        verify(pdfGenerator, times(1)).generatePdfReportBytes(any());
    }

    @Test
    @DisplayName("Should generate rich HTML report preview from CanonicalReportModel")
    void testGetReportPreview_Success() {
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(ownerUser));
        when(reportRepository.findById(100L)).thenReturn(Optional.of(testReport));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(testResume));
        when(reportMapper.toDto(any())).thenReturn(ReportDto.builder().id(100L).resumeId(10L).reportName("Report").build());

        ReportPreviewDto preview = reportService.getReportPreview(100L, "owner@example.com");

        assertNotNull(preview);
        assertNotNull(preview.getHtmlContent());
        assertTrue(preview.getHtmlContent().contains("EXECUTIVE ATS RESUME EVALUATION REPORT"));
    }
}
