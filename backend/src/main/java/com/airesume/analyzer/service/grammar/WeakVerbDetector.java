package com.airesume.analyzer.service.grammar;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WeakVerbDetector {

    private static final Map<String, String> WEAK_TO_STRONG_VERBS = new LinkedHashMap<>();

    static {
        WEAK_TO_STRONG_VERBS.put("worked on", "Spearheaded / Architected");
        WEAK_TO_STRONG_VERBS.put("helped", "Facilitated / Accelerated");
        WEAK_TO_STRONG_VERBS.put("did", "Executed / Implemented");
        WEAK_TO_STRONG_VERBS.put("made", "Engineered / Authored");
        WEAK_TO_STRONG_VERBS.put("handled", "Orchestrated / Managed");
        WEAK_TO_STRONG_VERBS.put("assisted", "Collaborated / Supported");
        WEAK_TO_STRONG_VERBS.put("responsible for", "Spearheaded / Directed");
        WEAK_TO_STRONG_VERBS.put("good communication", "Articulate stakeholder messaging");
        WEAK_TO_STRONG_VERBS.put("team player", "Cross-functional collaborator");
        WEAK_TO_STRONG_VERBS.put("hardworking", "Results-driven engineering focus");
        WEAK_TO_STRONG_VERBS.put("very", "Omit filler word");
        WEAK_TO_STRONG_VERBS.put("really", "Omit filler word");
    }

    public List<String> detectWeakVerbs(String text) {
        List<String> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        for (Map.Entry<String, String> entry : WEAK_TO_STRONG_VERBS.entrySet()) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(entry.getKey()) + "\\b");
            Matcher matcher = pattern.matcher(text);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            if (count > 0) {
                results.add("Found weak phrase '" + entry.getKey() + "' (" + count + "x) -> Suggestion: Use '" + entry.getValue() + "'");
            }
        }
        return results;
    }
}
