package com.airesume.analyzer.service.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReadabilityAnalyzer {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReadabilityMetrics {
        private int readabilityScore;
        private double avgSentenceLength;
        private double avgWordLength;
        private int totalSentences;
        private int totalWords;
        private int longSentenceCount;
        private List<String> longSentences;
        private String difficultyCategory;
    }

    public ReadabilityMetrics analyzeReadability(String text) {
        if (text == null || text.isBlank()) {
            return ReadabilityMetrics.builder()
                    .readabilityScore(70)
                    .avgSentenceLength(12.0)
                    .avgWordLength(5.0)
                    .totalSentences(5)
                    .totalWords(60)
                    .longSentenceCount(0)
                    .longSentences(new ArrayList<>())
                    .difficultyCategory("Standard Resume")
                    .build();
        }

        String[] sentences = text.split("[.!?]+\\s*");
        String[] words = text.replaceAll("[^a-zA-Z0-9\\s]", " ").trim().split("\\s+");

        int totalSentences = Math.max(1, sentences.length);
        int totalWords = Math.max(1, words.length);

        double avgSentenceLength = (double) totalWords / totalSentences;

        int totalCharCount = 0;
        for (String w : words) {
            totalCharCount += w.length();
        }
        double avgWordLength = (double) totalCharCount / totalWords;

        List<String> longSentences = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            String[] sWords = trimmed.split("\\s+");
            if (sWords.length > 25) {
                longSentences.add("Long sentence (" + sWords.length + " words): \"" + (trimmed.length() > 60 ? trimmed.substring(0, 60) + "..." : trimmed) + "\"");
            }
        }
        int longSentenceCount = longSentences.size();

        // Flesch Reading Ease Formula Heuristic
        // Score = 206.835 - (1.015 * ASL) - (84.6 * ASW)
        double totalSyllables = totalWords * 1.5; // Approximation
        double fleschScore = 206.835 - (1.015 * avgSentenceLength) - (84.6 * (totalSyllables / totalWords));
        int readabilityScore = Math.min(100, Math.max(30, (int) Math.round(fleschScore)));

        String category;
        if (readabilityScore >= 80) category = "Easy / Professional";
        else if (readabilityScore >= 60) category = "Standard Business";
        else category = "Complex / Dense";

        return ReadabilityMetrics.builder()
                .readabilityScore(readabilityScore)
                .avgSentenceLength(Math.round(avgSentenceLength * 10.0) / 10.0)
                .avgWordLength(Math.round(avgWordLength * 10.0) / 10.0)
                .totalSentences(totalSentences)
                .totalWords(totalWords)
                .longSentenceCount(longSentenceCount)
                .longSentences(longSentences)
                .difficultyCategory(category)
                .build();
    }
}
