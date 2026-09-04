package com.airesume.analyzer.service.parser;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexExtractor {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?\\d{1,3}[ -.]?)?\\(?\\d{3}\\)?[ -.]?\\d{3}[ -.]?\\d{4}");
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile("(?i)(?:https?://)?(?:www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+/?");
    private static final Pattern GITHUB_PATTERN = Pattern.compile("(?i)(?:https?://)?(?:www\\.)?github\\.com/[a-zA-Z0-9_-]+/?");
    private static final Pattern PORTFOLIO_PATTERN = Pattern.compile("(?i)(?:https?://)?(?:www\\.)?[a-zA-Z0-9-]+(?:\\.[a-zA-Z]{2,})+(?:/[^\\s]*)?");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?i)\\b([A-Z][a-zA-Z\\s.]+),\\s*([A-Z]{2}|[A-Z][a-zA-Z\\s.]+)\\b");

    public String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().toLowerCase().trim();
        }
        return null;
    }

    public String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }

    public String extractLinkedin(String text) {
        Matcher matcher = LINKEDIN_PATTERN.matcher(text);
        if (matcher.find()) {
            String url = matcher.group().trim();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            return url;
        }
        return null;
    }

    public String extractGithub(String text) {
        Matcher matcher = GITHUB_PATTERN.matcher(text);
        if (matcher.find()) {
            String url = matcher.group().trim();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            return url;
        }
        return null;
    }

    public String extractPortfolio(String text) {
        Matcher matcher = PORTFOLIO_PATTERN.matcher(text);
        while (matcher.find()) {
            String match = matcher.group().trim();
            if (!match.toLowerCase().contains("linkedin.com") && !match.toLowerCase().contains("github.com")) {
                if (!match.startsWith("http")) {
                    match = "https://" + match;
                }
                return match;
            }
        }
        return null;
    }

    public String extractFullName(String text, String email) {
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(lines.length, 6); i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.toLowerCase().contains("resume") || line.toLowerCase().contains("curriculum vitae")) {
                continue;
            }
            if (EMAIL_PATTERN.matcher(line).find() || PHONE_PATTERN.matcher(line).find()) {
                continue;
            }
            // Check if line looks like a person's name (2 to 4 words, letters only)
            if (line.matches("^[A-Za-z\\s.'\\-]{2,40}$") && line.split("\\s+").length >= 2) {
                return line;
            }
        }

        // Fallback: derive name from email prefix if present
        if (email != null && email.contains("@")) {
            String prefix = email.split("@")[0].replaceAll("[0-9._-]", " ");
            String[] parts = prefix.trim().split("\\s+");
            StringBuilder nameBuilder = new StringBuilder();
            for (String part : parts) {
                if (part.length() > 1) {
                    nameBuilder.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            String derived = nameBuilder.toString().trim();
            if (!derived.isEmpty()) {
                return derived;
            }
        }

        return "Candidate";
    }

    public String extractLocation(String text) {
        Matcher matcher = LOCATION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }
        return null;
    }
}
