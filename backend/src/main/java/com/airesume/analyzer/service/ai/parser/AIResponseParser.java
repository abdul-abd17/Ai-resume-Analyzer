package com.airesume.analyzer.service.ai.parser;

import com.airesume.analyzer.dto.AIRecommendationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIResponseParser {

    private final ObjectMapper objectMapper;

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern METRIC_PATTERN = Pattern.compile("\\b\\d{1,3}%|\\$\\d+(?:\\.\\d+)?\\s*[kKMB]?|\\b\\d+\\s*(?:x|k|M|B|users|clients)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Parses and validates raw AI text into a typed AIRecommendationResponse.
     * Returns Optional.empty() if json is malformed, missing keys, or contains invalid structure.
     */
    public Optional<AIRecommendationResponse> parseAndValidate(String rawText, String originalResumeText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            log.warn("AI raw text is null or empty.");
            return Optional.empty();
        }

        String jsonContent = extractJsonContent(rawText);
        if (jsonContent.isEmpty()) {
            log.warn("Failed to extract JSON content from raw AI output.");
            return Optional.empty();
        }

        try {
            AIRecommendationResponse response = objectMapper.readValue(jsonContent, AIRecommendationResponse.class);
            if (response == null || !response.isValid()) {
                log.warn("AI recommendation response failed key completeness validation (missing required keys).");
                return Optional.empty();
            }

            // Evidence grounding check: check for fabricated metrics not in original resume text or missing [VERIFY]
            if (containsFabricatedMetric(response, originalResumeText)) {
                log.warn("AI recommendation response contains ungrounded/fabricated metrics without [VERIFY] placeholder.");
                return Optional.empty();
            }

            return Optional.of(response);
        } catch (Exception e) {
            log.warn("Jackson JSON parsing failed for AI output: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String extractJsonContent(String rawText) {
        String trimmed = rawText.trim();

        // 1. Try markdown code block matcher
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 2. Direct JSON object search
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');

        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1).trim();
        }

        return "";
    }

    private boolean containsFabricatedMetric(AIRecommendationResponse response, String originalResumeText) {
        if (originalResumeText == null) {
            originalResumeText = "";
        }

        String combinedResponseText = String.join(" ",
                nullToEmpty(response.getProfessionalSummary()),
                nullToEmpty(response.getImprovedExperience()),
                nullToEmpty(response.getImprovedProjects())
        );

        Matcher metricMatcher = METRIC_PATTERN.matcher(combinedResponseText);
        while (metricMatcher.find()) {
            String metricStr = metricMatcher.group();
            // If the metric string is not in original resume text (case-insensitive)
            if (!containsIgnoreCase(originalResumeText, metricStr)) {
                int startIdx = Math.max(0, metricMatcher.start() - 40);
                int endIdx = Math.min(combinedResponseText.length(), metricMatcher.end() + 40);
                String context = combinedResponseText.substring(startIdx, endIdx);
                if (!context.toUpperCase().contains("VERIFY")) {
                    log.warn("Fabricated metric '{}' detected without [VERIFY] placeholder in context: {}", metricStr, context);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (source == null || target == null) return false;
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
