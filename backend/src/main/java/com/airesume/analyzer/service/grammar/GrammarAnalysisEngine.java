package com.airesume.analyzer.service.grammar;

import com.airesume.analyzer.entity.ParsedResume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrammarAnalysisEngine {

    private final WeakVerbDetector weakVerbDetector;
    private final PassiveVoiceDetector passiveVoiceDetector;
    private final ReadabilityAnalyzer readabilityAnalyzer;

    private static final Pattern REPEATED_WORD_PATTERN = Pattern.compile("(?i)\\b(\\w+)\\s+\\1\\b");

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GrammarEngineResult {
        private int grammarScore;
        private int readabilityScore;
        private int spellingErrorCount;
        private int grammarErrorCount;
        private int passiveVoiceCount;
        private int repeatedWordCount;
        private int longSentenceCount;
        private int weakVerbCount;
        private int formattingIssueCount;
        private List<String> spellingErrors;
        private List<String> grammarErrors;
        private List<String> weakVerbs;
        private List<String> passiveVoiceInstances;
        private List<String> suggestions;
    }

    public GrammarEngineResult analyze(ParsedResume parsedResume) {
        String rawText = parsedResume.getRawText() != null ? parsedResume.getRawText() : "";

        List<String> spellingErrors = new ArrayList<>();
        List<String> grammarErrors = new ArrayList<>();
        int spellingCount = 0;
        int grammarCount = 0;

        // 1. Execute LanguageTool Java API
        try {
            JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
            List<RuleMatch> matches = langTool.check(rawText);

            for (RuleMatch match : matches) {
                String errorSnippet = rawText.substring(
                        Math.max(0, match.getFromPos()),
                        Math.min(rawText.length(), match.getToPos())
                );

                String msg = match.getMessage() + " (near '" + errorSnippet + "')";
                if (!match.getSuggestedReplacements().isEmpty()) {
                    msg += " -> Suggestions: " + String.join(", ", match.getSuggestedReplacements().stream().limit(3).collect(Collectors.toList()));
                }

                if (match.getRule().getCategory().getId().toString().toLowerCase().contains("typo") ||
                    match.getRule().getId().toLowerCase().contains("spelling")) {
                    spellingCount++;
                    spellingErrors.add(msg);
                } else {
                    grammarCount++;
                    grammarErrors.add(msg);
                }
            }
        } catch (Exception e) {
            log.warn("LanguageTool execution warning: {}. Falling back to custom heuristic checks.", e.getMessage());
        }

        // 2. Custom Detectors
        List<String> weakVerbs = weakVerbDetector.detectWeakVerbs(rawText);
        int weakVerbCount = weakVerbs.size();

        List<String> passiveVoiceInstances = passiveVoiceDetector.detectPassiveVoice(rawText);
        int passiveVoiceCount = passiveVoiceInstances.size();

        ReadabilityAnalyzer.ReadabilityMetrics readability = readabilityAnalyzer.analyzeReadability(rawText);

        // 3. Repeated Words Detection
        List<String> repeatedWords = new ArrayList<>();
        Matcher repMatcher = REPEATED_WORD_PATTERN.matcher(rawText);
        while (repMatcher.find()) {
            repeatedWords.add("Duplicate adjacent word: '" + repMatcher.group() + "'");
        }
        int repeatedWordCount = repeatedWords.size();

        // 4. Formatting Checks
        int formattingIssueCount = 0;
        if (rawText.contains("  ")) formattingIssueCount += 2; // Double spaces
        if (parsedResume.getSummary() == null || parsedResume.getSummary().isBlank()) formattingIssueCount += 2;

        // 5. Score Calculation
        int grammarScore = Math.max(30, 100 - (spellingCount * 4 + grammarCount * 5));
        int readabilityScore = readability.getReadabilityScore();

        // 6. Suggestions Priority List
        List<String> suggestions = new ArrayList<>();

        if (spellingCount > 0) {
            suggestions.add("[HIGH] Fix " + spellingCount + " spelling typos found in your resume text.");
        }

        if (grammarCount > 0) {
            suggestions.add("[HIGH] Resolve " + grammarCount + " grammar and punctuation inconsistencies.");
        }

        if (weakVerbCount > 0) {
            suggestions.add("[HIGH] Replace weak verbs ('worked', 'helped', 'handled') with strong action verbs ('spearheaded', 'orchestrated', 'engineered').");
        }

        if (passiveVoiceCount > 0) {
            suggestions.add("[MEDIUM] Convert " + passiveVoiceCount + " passive voice sentences to active, impact-oriented statements.");
        }

        if (readability.getLongSentenceCount() > 0) {
            suggestions.add("[MEDIUM] Break down " + readability.getLongSentenceCount() + " long sentences (>25 words) into concise bullet points.");
        }

        if (formattingIssueCount > 0) {
            suggestions.add("[LOW] Clean up double spaces and formatting alignment inconsistencies.");
        }

        return GrammarEngineResult.builder()
                .grammarScore(grammarScore)
                .readabilityScore(readabilityScore)
                .spellingErrorCount(spellingCount)
                .grammarErrorCount(grammarCount)
                .passiveVoiceCount(passiveVoiceCount)
                .repeatedWordCount(repeatedWordCount)
                .longSentenceCount(readability.getLongSentenceCount())
                .weakVerbCount(weakVerbCount)
                .formattingIssueCount(formattingIssueCount)
                .spellingErrors(spellingErrors.stream().limit(10).collect(Collectors.toList()))
                .grammarErrors(grammarErrors.stream().limit(10).collect(Collectors.toList()))
                .weakVerbs(weakVerbs)
                .passiveVoiceInstances(passiveVoiceInstances.stream().limit(8).collect(Collectors.toList()))
                .suggestions(suggestions)
                .build();
    }
}
