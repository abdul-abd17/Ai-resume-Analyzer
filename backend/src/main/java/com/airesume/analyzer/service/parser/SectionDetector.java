package com.airesume.analyzer.service.parser;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class SectionDetector {

    public enum SectionType {
        SUMMARY,
        SKILLS,
        EDUCATION,
        EXPERIENCE,
        PROJECTS,
        CERTIFICATIONS,
        LANGUAGES,
        ACHIEVEMENTS,
        UNKNOWN
    }

    private static final Map<SectionType, Pattern> SECTION_PATTERNS = new LinkedHashMap<>();

    static {
        SECTION_PATTERNS.put(SectionType.SUMMARY, Pattern.compile("(?i)^\\s*(summary|objective|profile|about\\s+me|professional\\s+summary|executive\\s+summary)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.SKILLS, Pattern.compile("(?i)^\\s*(skills|technical\\s+skills|core\\s+competencies|technologies|expertise|tools\\s+&\\s+languages)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.EXPERIENCE, Pattern.compile("(?i)^\\s*(experience|work\\s+experience|employment\\s+history|professional\\s+experience|work\\s+history)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.EDUCATION, Pattern.compile("(?i)^\\s*(education|academic\\ background|qualifications|education\\s+&\\s+training)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.PROJECTS, Pattern.compile("(?i)^\\s*(projects|key\\s+projects|personal\\s+projects|academic\\s+projects)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.CERTIFICATIONS, Pattern.compile("(?i)^\\s*(certifications|licenses\\s+&\\s+certifications|certificates|courses)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.LANGUAGES, Pattern.compile("(?i)^\\s*(languages|language\\s+proficiency|spoken\\s+languages)\\s*:?\\s*$"));
        SECTION_PATTERNS.put(SectionType.ACHIEVEMENTS, Pattern.compile("(?i)^\\s*(achievements|awards|honors|awards\\s+&\\s+achievements|accomplishments)\\s*:?\\s*$"));
    }

    public Map<SectionType, String> detectAndSplitSections(String cleanedText) {
        Map<SectionType, StringBuilder> sectionBuilders = new EnumMap<>(SectionType.class);
        for (SectionType type : SectionType.values()) {
            sectionBuilders.put(type, new StringBuilder());
        }

        String[] lines = cleanedText.split("\\r?\\n");
        SectionType currentSection = SectionType.SUMMARY; // default header block

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            SectionType detectedHeader = matchHeader(trimmed);
            if (detectedHeader != SectionType.UNKNOWN) {
                currentSection = detectedHeader;
            } else {
                sectionBuilders.get(currentSection).append(line).append("\n");
            }
        }

        Map<SectionType, String> result = new EnumMap<>(SectionType.class);
        for (Map.Entry<SectionType, StringBuilder> entry : sectionBuilders.entrySet()) {
            String content = entry.getValue().toString().trim();
            if (!content.isEmpty()) {
                result.put(entry.getKey(), content);
            }
        }

        return result;
    }

    private SectionType matchHeader(String line) {
        for (Map.Entry<SectionType, Pattern> entry : SECTION_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(line).matches()) {
                return entry.getKey();
            }
        }
        return SectionType.UNKNOWN;
    }
}
