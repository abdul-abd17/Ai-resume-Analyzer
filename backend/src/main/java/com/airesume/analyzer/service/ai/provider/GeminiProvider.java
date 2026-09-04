package com.airesume.analyzer.service.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class GeminiProvider implements AIProvider {

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final RuleFallbackProvider fallbackProvider;

    public GeminiProvider(RuleFallbackProvider fallbackProvider) {
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String generateCompletion(String prompt) {
        return generateCompletionWithSystem("You are an expert HRTech AI software architect and resume coach.", prompt);
    }

    @Override
    public String generateCompletionWithSystem(String systemInstruction, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("Gemini API key not configured. Using RuleFallbackProvider.");
            return fallbackProvider.generateCompletionWithSystem(systemInstruction, prompt);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Collections.singletonMap("text", systemInstruction + "\n\n" + prompt);
            Map<String, Object> contentsObj = Collections.singletonMap("parts", Collections.singletonList(textPart));
            Map<String, Object> genConfig = Map.of(
                    "responseMimeType", "application/json",
                    "temperature", 0.2
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(contentsObj));
            requestBody.put("generationConfig", genConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map candidate = (Map) candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List parts = (List) content.get("parts");
                    Map firstPart = (Map) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            log.error("Gemini API request failed: {}. Falling back to RuleFallbackProvider.", e.getMessage());
        }

        return fallbackProvider.generateCompletionWithSystem(systemInstruction, prompt);
    }

    @Override
    public String getProviderName() {
        return "GeminiProvider";
    }
}
