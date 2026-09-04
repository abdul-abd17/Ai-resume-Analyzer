package com.airesume.analyzer.service.grammar;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PassiveVoiceDetector {

    private static final Pattern PASSIVE_PATTERN = Pattern.compile(
            "(?i)\\b(am|is|are|was|were|being|been|be)\\s+([a-z]+ed|[a-z]+en)\\b"
    );

    public List<String> detectPassiveVoice(String text) {
        List<String> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            Matcher matcher = PASSIVE_PATTERN.matcher(trimmed);
            while (matcher.find()) {
                String match = matcher.group();
                if (!match.toLowerCase().contains("based in") && !match.toLowerCase().contains("located in")) {
                    results.add("Passive voice detected: \"" + match + "\" -> Convert to active voice action verb.");
                }
            }
        }
        return results;
    }
}
