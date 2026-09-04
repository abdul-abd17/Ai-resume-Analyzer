package com.airesume.analyzer.service.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class OpenAIProvider implements AIProvider {

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final RuleFallbackProvider fallbackProvider;

    public OpenAIProvider(RuleFallbackProvider fallbackProvider) {
        this.fallbackProvider = fallbackProvider;
    }

    @Override
    public String generateCompletion(String prompt) {
        return generateCompletionWithSystem("You are an expert HRTech AI software architect and resume coach.", prompt);
    }

    @Override
    public String generateCompletionWithSystem(String systemInstruction, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("OpenAI API key not configured. Using RuleFallbackProvider.");
            return fallbackProvider.generateCompletionWithSystem(systemInstruction, prompt);
        }

        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemInstruction));
            messages.add(Map.of("role", "user", "content", prompt));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("response_format", Map.of("type", "json_object"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("OpenAI API request failed: {}. Falling back to RuleFallbackProvider.", e.getMessage());
        }

        return fallbackProvider.generateCompletionWithSystem(systemInstruction, prompt);
    }

    @Override
    public String getProviderName() {
        return "OpenAIProvider";
    }
}
