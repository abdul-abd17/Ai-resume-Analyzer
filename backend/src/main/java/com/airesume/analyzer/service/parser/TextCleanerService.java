package com.airesume.analyzer.service.parser;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class TextCleanerService {

    private static final Pattern PAGE_NUMBER_PATTERN = Pattern.compile("(?i)^\\s*(page\\s+\\d+(\\s+of\\s+\\d+)?|-\\s*\\d+\\s*-|\\d+\\s*/\\s*\\d+)\\s*$");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("[ \\t]+");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("(\\r?\\n){3,}");

    public String cleanText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        // 1. Normalize Unicode
        String normalized = Normalizer.normalize(rawText, Normalizer.Form.NFC);

        // 2. Process line by line
        String[] lines = normalized.split("\\r?\\n");
        StringBuilder cleanedBuilder = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip page numbers or blank footer lines
            if (PAGE_NUMBER_PATTERN.matcher(trimmed).matches()) {
                continue;
            }

            // Replace multiple horizontal spaces with single space
            String cleanLine = MULTIPLE_SPACES.matcher(trimmed).replaceAll(" ");
            cleanedBuilder.append(cleanLine).append("\n");
        }

        // 3. Remove excessive empty lines
        String cleaned = MULTIPLE_NEWLINES.matcher(cleanedBuilder.toString()).replaceAll("\n\n");

        return cleaned.trim();
    }
}
