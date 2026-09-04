package com.airesume.analyzer.service.ai;

import com.airesume.analyzer.dto.AIRecommendationDto;
import com.airesume.analyzer.dto.AIRecommendationResponse;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.mapper.AIRecommendationMapper;
import com.airesume.analyzer.repository.*;
import com.airesume.analyzer.service.ai.parser.AIResponseParser;
import com.airesume.analyzer.service.ai.prompt.PromptBuilder;
import com.airesume.analyzer.service.ai.provider.AIProvider;
import com.airesume.analyzer.service.ai.provider.AIProviderFactory;
import com.airesume.analyzer.service.ai.provider.RuleFallbackProvider;
import com.airesume.analyzer.service.parser.ResumeParserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIRecommendationServiceImplTest {

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
    private ResumeParserService resumeParserService;
    @Mock
    private PromptBuilder promptBuilder;
    @Mock
    private AIProviderFactory aiProviderFactory;
    @Mock
    private AIRecommendationMapper aiRecommendationMapper;
    @Mock
    private AIProvider activeProvider;

    @Spy
    private RuleFallbackProvider ruleFallbackProvider = new RuleFallbackProvider();
    @Spy
    private AIResponseParser aiResponseParser = new AIResponseParser(new ObjectMapper());

    @InjectMocks
    private AIRecommendationServiceImpl recommendationService;

    private User testUser;
    private Resume testResume;
    private ParsedResume testParsedResume;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testResume = Resume.builder().id(100L).user(testUser).fileName("resume.pdf").build();
        testParsedResume = ParsedResume.builder().id(10L).resume(testResume).rawText("Java developer").skills("Java, Spring").build();
    }

    private String validJsonCompletion() {
        return "{\n" +
                "  \"professionalSummary\": \"Summary\",\n" +
                "  \"improvedExperience\": \"Experience\",\n" +
                "  \"improvedProjects\": \"Projects\",\n" +
                "  \"improvedSkills\": \"Skills\",\n" +
                "  \"keywordRecommendations\": \"Keywords\",\n" +
                "  \"atsRecommendations\": \"ATS\",\n" +
                "  \"grammarRecommendations\": \"Grammar\",\n" +
                "  \"interviewPreparation\": \"Interview\",\n" +
                "  \"careerSuggestions\": \"Career\",\n" +
                "  \"overallFeedback\": \"Feedback\"\n" +
                "}";
    }

    @Test
    @DisplayName("Should successfully generate recommendations when active AI provider returns valid JSON")
    void testGenerateAIRecommendations_ValidProviderResponse() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(testResume));
        when(resumeParserService.getOrParseEntity(100L, "user@example.com")).thenReturn(testParsedResume);
        when(promptBuilder.buildPrompt(any(), any(), any(), any())).thenReturn("Test Prompt");
        when(aiProviderFactory.getActiveProvider()).thenReturn(activeProvider);
        when(activeProvider.getProviderName()).thenReturn("TestAIProvider");
        when(activeProvider.generateCompletionWithSystem(anyString(), anyString())).thenReturn(validJsonCompletion());
        when(aiRecommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiRecommendationMapper.toDto(any())).thenAnswer(invocation -> {
            AIRecommendation rec = invocation.getArgument(0);
            return AIRecommendationDto.builder()
                    .professionalSummary(rec.getProfessionalSummary())
                    .improvedExperience(rec.getImprovedExperience())
                    .build();
        });

        AIRecommendationDto result = recommendationService.generateAIRecommendations(100L, "user@example.com");

        assertNotNull(result);
        assertEquals("Summary", result.getProfessionalSummary());
        verify(aiRecommendationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should fallback to RuleFallbackProvider when active AI provider returns malformed/invalid JSON")
    void testGenerateAIRecommendations_MalformedJsonFallback() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(resumeRepository.findById(100L)).thenReturn(Optional.of(testResume));
        when(resumeParserService.getOrParseEntity(100L, "user@example.com")).thenReturn(testParsedResume);
        when(promptBuilder.buildPrompt(any(), any(), any(), any())).thenReturn("Test Prompt");
        when(aiProviderFactory.getActiveProvider()).thenReturn(activeProvider);
        when(activeProvider.getProviderName()).thenReturn("FailingAIProvider");
        when(activeProvider.generateCompletionWithSystem(anyString(), anyString())).thenReturn("Malformed text { not json");

        when(aiRecommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiRecommendationMapper.toDto(any())).thenAnswer(invocation -> {
            AIRecommendation rec = invocation.getArgument(0);
            return AIRecommendationDto.builder()
                    .professionalSummary(rec.getProfessionalSummary())
                    .improvedExperience(rec.getImprovedExperience())
                    .build();
        });

        AIRecommendationDto result = recommendationService.generateAIRecommendations(100L, "user@example.com");

        assertNotNull(result);
        assertTrue(result.getProfessionalSummary().contains("Analysis generated using static rule engine"));
        assertTrue(result.getImprovedExperience().contains("[VERIFY METRIC]"));
        verify(ruleFallbackProvider, times(1)).generateCompletionWithSystem(anyString(), anyString());
    }

    @Test
    @DisplayName("Should test RuleFallbackProvider produces valid evidence-grounded response with placeholders")
    void testRuleFallbackProvider_Behavior() {
        String output = ruleFallbackProvider.generateCompletionWithSystem("sys", "prompt");
        Optional<AIRecommendationResponse> parsed = aiResponseParser.parseAndValidate(output, "Raw resume");

        assertTrue(parsed.isPresent(), "RuleFallbackProvider output must be valid JSON");
        assertTrue(parsed.get().getImprovedExperience().contains("[VERIFY METRIC]"));
    }
}
