package com.airesume.analyzer.service.ai.provider;

public interface AIProvider {

    String generateCompletion(String prompt);

    String generateCompletionWithSystem(String systemInstruction, String prompt);

    String getProviderName();
}
