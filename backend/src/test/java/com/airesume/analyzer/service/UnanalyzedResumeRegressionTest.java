package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.DashboardDto;
import com.airesume.analyzer.dto.ReportPreviewDto;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.repository.*;
import com.airesume.analyzer.service.ai.prompt.PromptBuilder;
import com.airesume.analyzer.service.dashboard.DashboardServiceImpl;
import com.airesume.analyzer.service.report.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnanalyzedResumeRegressionTest {

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

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("PromptBuilder should not contain fallback ATS score 75 or default skills when analysis is missing")
    void testPromptBuilderWithUnanalyzedData() {
        PromptBuilder builder = new PromptBuilder();
        ParsedResume parsed = ParsedResume.builder()
                .fullName("John Doe")
                .summary(null)
                .build();

        String prompt = builder.buildPrompt(parsed, null, null, null);

        assertFalse(prompt.contains("75"));
        assertFalse(prompt.contains("Java, Spring Boot, React"));
        assertFalse(prompt.contains("Docker, AWS, Microservices"));
        assertTrue(prompt.contains("Not analyzed"));
    }

    @Test
    @DisplayName("Dashboard quick stats for unanalyzed resume must be zero and skills empty")
    void testDashboardWithUnanalyzedResume() {
        User user = User.builder().id(1L).email("test@example.com").build();
        Resume resume = Resume.builder().id(10L).originalFileName("test.pdf").user(user).uploadedAt(java.time.LocalDateTime.now()).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(resumeRepository.findByUserId(1L)).thenReturn(Collections.singletonList(resume));
        when(analysisReportRepository.findByResumeUserIdOrderByAnalysisDateDesc(1L)).thenReturn(Collections.emptyList());
        when(grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(10L)).thenReturn(Optional.empty());
        when(matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.empty());
        when(aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.empty());

        DashboardDto dto = dashboardService.getAggregatedDashboardData("test@example.com");

        assertEquals(0, dto.getQuickStats().getAverageAtsScore());
        assertEquals(0, dto.getQuickStats().getAverageGrammarScore());
        assertEquals(0, dto.getQuickStats().getLatestJobMatchPercentage());
        assertEquals(0, dto.getQuickStats().getOverallResumeStrengthScore());
        assertTrue(((java.util.List<?>) dto.getSkillAnalytics().get("topSkills")).isEmpty());
    }
}
