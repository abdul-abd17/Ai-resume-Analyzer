package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfGeneratorTest {

    private PdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        pdfGenerator = new PdfGenerator();
    }

    private CanonicalReportModel createBaseModel() {
        return CanonicalReportModel.builder()
                .resumeId(1L)
                .originalFileName("Jane_Doe_Resume.pdf")
                .generatedDate(LocalDateTime.now())
                .candidateName("Jane Doe")
                .candidateEmail("jane@example.com")
                .candidatePhone("+1-555-0199")
                .candidateLocation("San Francisco, CA")
                .summary("Experienced Senior Software Engineer specializing in distributed backend microservices and Java cloud platforms.")
                .atsScore(88)
                .grammarScore(92)
                .jobMatchPercentage(85)
                .overallStrength(88)
                .topSkills(Arrays.asList("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes"))
                .missingKeywords(Arrays.asList("Kafka", "Redis"))
                .overusedWords(Arrays.asList("Managed", "Worked"))
                .grammarWeakVerbCount(1)
                .grammarPassiveCount(0)
                .grammarIssues(Collections.singletonList("weak verb: worked"))
                .targetJobTitle("Principal Java Architect")
                .matchedJobSkills(Arrays.asList("Java", "Spring Boot", "Docker"))
                .missingJobSkills(Collections.singletonList("Kafka"))
                .aiProfessionalSummary("Results-oriented Principal Java Architect with proven record in high-concurrency systems.")
                .aiImprovedExperience("• Engineered Spring Boot microservices handling [VERIFY METRIC] requests/sec.")
                .aiImprovedProjects("• Designed distributed caching architecture with Redis.")
                .build();
    }

    @Test
    @DisplayName("Should successfully generate PDF bytes for a complete report")
    void testCompleteReportPdfGeneration() throws Exception {
        CanonicalReportModel model = createBaseModel();
        byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF bytes should contain document data");
    }

    @Test
    @DisplayName("Should handle missing grammar analysis gracefully without throwing NPE")
    void testMissingGrammarAnalysis() throws Exception {
        CanonicalReportModel model = createBaseModel();
        model.setGrammarScore(null);
        model.setGrammarWeakVerbCount(null);
        model.setGrammarPassiveCount(null);
        model.setGrammarIssues(Collections.emptyList());

        byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }

    @Test
    @DisplayName("Should handle missing ATS analysis gracefully without throwing NPE")
    void testMissingAtsAnalysis() throws Exception {
        CanonicalReportModel model = createBaseModel();
        model.setAtsScore(null);
        model.setTopSkills(Collections.emptyList());
        model.setMissingKeywords(Collections.emptyList());
        model.setOverusedWords(Collections.emptyList());

        byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }

    @Test
    @DisplayName("Should handle extremely long resume text and AI recommendations across multiple pages")
    void testLongResumeAndMultiplePages() throws Exception {
        CanonicalReportModel model = createBaseModel();
        StringBuilder longSummary = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longSummary.append("Over the past decade, engineered distributed microservices architecture serving millions of concurrent global users. ");
        }
        model.setSummary(longSummary.toString());

        StringBuilder longAiExp = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longAiExp.append("• Engineered enterprise Spring Boot microservice line ").append(i).append(" with zero downtime deployments.\n");
        }
        model.setAiImprovedExperience(longAiExp.toString());

        byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 2000, "Multi-page PDF should generate successfully without clipping");
    }

    @Test
    @DisplayName("Should safely sanitize special characters, smart quotes, em-dashes, and Unicode symbols")
    void testSpecialCharactersHandling() throws Exception {
        CanonicalReportModel model = createBaseModel();
        model.setCandidateName("Jäñé Dôë — Senior Technical Recruiter & Architect’s Choice ✓ ★");
        model.setSummary("Architected system with smart quotes “double” and ‘single’, em-dash — and bullet • symbol.");

        byte[] pdfBytes = pdfGenerator.generatePdfReportBytes(model);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }
}
