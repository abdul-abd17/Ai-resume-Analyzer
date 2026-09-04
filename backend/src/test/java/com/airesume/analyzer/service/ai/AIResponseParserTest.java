package com.airesume.analyzer.service.ai;

import com.airesume.analyzer.dto.AIRecommendationResponse;
import com.airesume.analyzer.service.ai.parser.AIResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AIResponseParserTest {

    private AIResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new AIResponseParser(new ObjectMapper());
    }

    private String createSampleValidJson() {
        return "{\n" +
                "  \"professionalSummary\": \"Results-driven Software Engineer with 5+ years of experience.\",\n" +
                "  \"improvedExperience\": \"• Engineered scalable REST APIs using Java and Spring Boot.\",\n" +
                "  \"improvedProjects\": \"• Developed high-throughput microservices architecture.\",\n" +
                "  \"improvedSkills\": \"Java, Spring Boot, PostgreSQL, Docker, Kubernetes\",\n" +
                "  \"keywordRecommendations\": \"Place Microservices and REST APIs at the top of experience section.\",\n" +
                "  \"atsRecommendations\": \"Use standard section headers: Experience, Skills, Education.\",\n" +
                "  \"grammarRecommendations\": \"Use active verbs like Engineered, Developed, Optimized.\",\n" +
                "  \"interviewPreparation\": \"Prepare to explain Spring Boot auto-configuration and JPA caching.\",\n" +
                "  \"careerSuggestions\": \"Target Senior Backend Engineer positions.\",\n" +
                "  \"overallFeedback\": \"Strong technical foundation with clear impact.\"\n" +
                "}";
    }

    @Test
    @DisplayName("Should successfully parse clean valid JSON into AIRecommendationResponse")
    void testValidJsonResponse() {
        String json = createSampleValidJson();
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(json, "Java developer with experience");

        assertTrue(result.isPresent());
        assertEquals("Results-driven Software Engineer with 5+ years of experience.", result.get().getProfessionalSummary());
        assertTrue(result.get().isValid());
    }

    @Test
    @DisplayName("Should successfully parse markdown-wrapped JSON (```json ... ```)")
    void testMarkdownWrappedJson() {
        String markdownJson = "```json\n" + createSampleValidJson() + "\n```";
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(markdownJson, "Java developer");

        assertTrue(result.isPresent());
        assertEquals("Java, Spring Boot, PostgreSQL, Docker, Kubernetes", result.get().getImprovedSkills());
    }

    @Test
    @DisplayName("Should return empty Optional for malformed JSON")
    void testMalformedJson() {
        String malformed = "{ \"professionalSummary\": \"Broken JSON without closing quotes }";
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(malformed, "Java developer");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty Optional when required keys are missing or blank")
    void testMissingKeys() {
        String missingKeyJson = "{\n" +
                "  \"professionalSummary\": \"Engineer\",\n" +
                "  \"improvedExperience\": \"Built APIs\"\n" +
                "}";
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(missingKeyJson, "Java developer");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty Optional for null or empty raw text")
    void testEmptyAiResponse() {
        assertFalse(parser.parseAndValidate(null, "Resume text").isPresent());
        assertFalse(parser.parseAndValidate("   ", "Resume text").isPresent());
    }

    @Test
    @DisplayName("Should detect fabricated metric percentages without [VERIFY] placeholder and reject response")
    void testFabricatedMetrics() {
        String fabricatedJson = "{\n" +
                "  \"professionalSummary\": \"Increased application speed by 95%.\",\n" +
                "  \"improvedExperience\": \"• Boosted API latency by 80% and saved $500k.\",\n" +
                "  \"improvedProjects\": \"• Scaled users by 300%.\",\n" +
                "  \"improvedSkills\": \"Java, Spring Boot\",\n" +
                "  \"keywordRecommendations\": \"Keyword advice\",\n" +
                "  \"atsRecommendations\": \"ATS advice\",\n" +
                "  \"grammarRecommendations\": \"Grammar advice\",\n" +
                "  \"interviewPreparation\": \"Interview advice\",\n" +
                "  \"careerSuggestions\": \"Career advice\",\n" +
                "  \"overallFeedback\": \"Feedback\"\n" +
                "}";

        // Source resume does NOT mention 95%, 80%, or 300%
        String sourceResume = "Developed Java web applications and optimized database queries.";
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(fabricatedJson, sourceResume);

        assertFalse(result.isPresent(), "Fabricated percentage without [VERIFY] placeholder must be rejected");
    }

    @Test
    @DisplayName("Should accept metric percentages when marked with [VERIFY METRIC] or present in source resume")
    void testGroundedOrPlaceholderMetrics() {
        String groundedJson = "{\n" +
                "  \"professionalSummary\": \"Increased application speed by [VERIFY METRIC]%.\",\n" +
                "  \"improvedExperience\": \"• Reduced latency by 20% in production.\",\n" +
                "  \"improvedProjects\": \"• Scaled services.\",\n" +
                "  \"improvedSkills\": \"Java, Spring Boot\",\n" +
                "  \"keywordRecommendations\": \"Keyword advice\",\n" +
                "  \"atsRecommendations\": \"ATS advice\",\n" +
                "  \"grammarRecommendations\": \"Grammar advice\",\n" +
                "  \"interviewPreparation\": \"Interview advice\",\n" +
                "  \"careerSuggestions\": \"Career advice\",\n" +
                "  \"overallFeedback\": \"Feedback\"\n" +
                "}";

        // Source resume actually contains 20%
        String sourceResume = "Reduced latency by 20% in production using Spring Boot.";
        Optional<AIRecommendationResponse> result = parser.parseAndValidate(groundedJson, sourceResume);

        assertTrue(result.isPresent(), "Grounded metric present in source resume and [VERIFY METRIC] placeholders must be accepted");
    }
}
