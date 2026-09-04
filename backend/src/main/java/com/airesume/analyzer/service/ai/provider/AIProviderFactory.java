package com.airesume.analyzer.service.ai.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIProviderFactory {

    private final GeminiProvider geminiProvider;
    private final OpenAIProvider openAIProvider;
    private final RuleFallbackProvider ruleFallbackProvider;

    @Value("${ai.provider:gemini}")
    private String activeProviderName;

    public AIProvider getActiveProvider() {
        if ("openai".equalsIgnoreCase(activeProviderName)) {
            return openAIProvider;
        } else if ("gemini".equalsIgnoreCase(activeProviderName)) {
            return geminiProvider;
        }
        return ruleFallbackProvider;
    }
}
