package com.airesume.analyzer.service.ats;

import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.service.skill.SkillAliasMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AtsAnalysisEngine {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillFrequency {
        private String keyword;
        private String category;
        private int frequency;
        private int weight;

        public int getScorePower() {
            return frequency * weight;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuggestionItem {
        private String priority; // HIGH, MEDIUM, LOW
        private String text;
        private int priorityValue; // 3 for HIGH, 2 for MEDIUM, 1 for LOW
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AtsEngineResult {
        private int overallScore;
        private int contactScore;
        private int summaryScore;
        private int skillsScore;
        private int experienceScore;
        private int educationScore;
        private int projectsScore;
        private int certificationScore;
        private int keywordCoverage;
        private int missingKeywordCount;
        private int duplicateKeywordCount;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> suggestions;
        private List<Map<String, Object>> topSkills;
        private List<String> missingKeywords;
        private List<String> duplicateKeywords;
        private Map<String, Integer> categorySkillCounts;

        /**
         * Transparent factor breakdown detailing the exact criteria contributing to the score.
         */
        private Map<String, String> factorExplanations;
    }

    private static final Set<String> ACTION_VERBS = new HashSet<>(Arrays.asList(
            "architected", "spearheaded", "engineered", "implemented", "designed", "optimized",
            "developed", "built", "building", "refactored", "launched", "managed", "orchestrated",
            "automated", "scaled", "reduced", "increased", "championed", "led", "leading", "created",
            "delivered", "integrated", "transformed", "migrated", "deployed", "pioneered"
    ));

    private static final Pattern METRICS_PATTERN = Pattern.compile(
            "\\b(?:\\d+(?:\\.\\d+)?%(?:\\s*(?:increase|decrease|improvement|reduction))?|\\$\\d+(?:[kKMB])?|\\d+[kKMB]?\\+?\\s*(?:years?|y\\.?o\\.?|users|clients|transactions|requests|ms|seconds|percent|x|microservices|services)|[1-9]\\d*x)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public AtsEngineResult analyze(ParsedResume parsedResume, List<SkillKeyword> seedKeywords) {
        if (parsedResume == null) {
            parsedResume = ParsedResume.builder().build();
        }
        if (seedKeywords == null) {
            seedKeywords = Collections.emptyList();
        }

        String rawText = parsedResume.getRawText() != null ? parsedResume.getRawText() : "";
        StringBuilder fullSearchBuilder = new StringBuilder(rawText);
        if (parsedResume.getSummary() != null) fullSearchBuilder.append(" ").append(parsedResume.getSummary());
        if (parsedResume.getSkills() != null) fullSearchBuilder.append(" ").append(parsedResume.getSkills());
        if (parsedResume.getExperience() != null) fullSearchBuilder.append(" ").append(parsedResume.getExperience());
        if (parsedResume.getProjects() != null) fullSearchBuilder.append(" ").append(parsedResume.getProjects());
        if (parsedResume.getEducation() != null) fullSearchBuilder.append(" ").append(parsedResume.getEducation());
        if (parsedResume.getCertifications() != null) fullSearchBuilder.append(" ").append(parsedResume.getCertifications());

        String fullSearchText = fullSearchBuilder.toString();
        Map<String, String> factorExplanations = new LinkedHashMap<>();

        // 1. Boundary-Safe Skill Keyword Matching & Alias Handling
        Map<String, SkillKeyword> seedMap = new HashMap<>();
        for (SkillKeyword sk : seedKeywords) {
            if (sk != null && sk.getKeyword() != null) {
                seedMap.put(SkillAliasMapper.getCanonicalSkill(sk.getKeyword()), sk);
            }
        }

        Set<String> matchedCanonicalSkills = new HashSet<>();
        List<String> missingKeywords = new ArrayList<>();
        Map<String, Integer> skillFrequencyMap = new HashMap<>();

        for (SkillKeyword sk : seedKeywords) {
            if (sk == null || sk.getKeyword() == null) continue;
            String canonical = SkillAliasMapper.getCanonicalSkill(sk.getKeyword());
            int count = SkillAliasMapper.countSkillOccurrences(fullSearchText, sk.getKeyword());

            if (count > 0) {
                matchedCanonicalSkills.add(canonical);
                skillFrequencyMap.put(sk.getKeyword(), skillFrequencyMap.getOrDefault(sk.getKeyword(), 0) + count);
            } else if (!missingKeywords.contains(sk.getKeyword())) {
                missingKeywords.add(sk.getKeyword());
            }
        }

        int totalSeedKeywords = seedKeywords.size();
        int matchedCount = matchedCanonicalSkills.size();
        int keywordCoverage = totalSeedKeywords > 0 ? (int) Math.round(((double) matchedCount / totalSeedKeywords) * 100) : 0;

        // 2. Overused Keyword Detection (Detecting word repetitions > 8 times, excluding common stop words)
        Map<String, Integer> wordFreq = buildWordFrequency(rawText);
        List<String> duplicateKeywords = new ArrayList<>();
        wordFreq.forEach((word, count) -> {
            if (count >= 8 && word.length() > 3) {
                duplicateKeywords.add(word + " (" + count + "x)");
            }
        });
        int duplicateKeywordCount = duplicateKeywords.size();

        // 3. Ranking Top Skills via Max-Heap
        PriorityQueue<SkillFrequency> maxHeap = new PriorityQueue<>(
                Comparator.comparingInt(SkillFrequency::getScorePower).reversed()
        );
        skillFrequencyMap.forEach((kw, freq) -> {
            SkillKeyword sk = seedMap.get(SkillAliasMapper.getCanonicalSkill(kw));
            int weight = sk != null ? sk.getWeight() : 1;
            String category = sk != null ? sk.getCategory() : "General";
            maxHeap.offer(new SkillFrequency(kw, category, freq, weight));
        });

        List<Map<String, Object>> topSkills = new ArrayList<>();
        int heapCount = 0;
        while (!maxHeap.isEmpty() && heapCount < 5) {
            SkillFrequency sf = maxHeap.poll();
            Map<String, Object> map = new HashMap<>();
            map.put("keyword", sf.getKeyword());
            map.put("category", sf.getCategory());
            map.put("frequency", sf.getFrequency());
            map.put("power", sf.getScorePower());
            topSkills.add(map);
            heapCount++;
        }

        // 4. Skill Category Distribution
        Map<String, Integer> categorySkillCounts = new TreeMap<>();
        for (String matchedCan : matchedCanonicalSkills) {
            SkillKeyword sk = seedMap.get(matchedCan);
            if (sk != null && sk.getCategory() != null) {
                categorySkillCounts.put(sk.getCategory(), categorySkillCounts.getOrDefault(sk.getCategory(), 0) + 1);
            }
        }

        // 5. Deterministic Section Evaluation
        int contactScore = evaluateContact(parsedResume, factorExplanations);               // Max 10
        int summaryScore = evaluateSummary(parsedResume, factorExplanations);               // Max 10
        int skillsScore = evaluateSkills(parsedResume, matchedCount, factorExplanations);    // Max 25
        int experienceScore = evaluateExperience(parsedResume, factorExplanations);         // Max 20
        int educationScore = evaluateEducation(parsedResume, factorExplanations);           // Max 10
        int projectsScore = evaluateProjects(parsedResume, factorExplanations);             // Max 10
        int certificationScore = evaluateCertifications(parsedResume, factorExplanations);   // Max 5
        int coverageScore = Math.min(10, (int) Math.round(keywordCoverage / 10.0));
        factorExplanations.put("Keyword Coverage Factor", String.format("%d%% seed keyword coverage (+%d pts)", keywordCoverage, coverageScore));

        int formattingScore = evaluateFormatting(parsedResume, rawText, duplicateKeywordCount, factorExplanations); // Max 10

        int overallScore = contactScore + summaryScore + skillsScore + experienceScore
                + educationScore + projectsScore + certificationScore + coverageScore + formattingScore;
        overallScore = Math.min(100, Math.max(0, overallScore));

        // 6. Generating Strengths, Weaknesses, and Priority Suggestions
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<SuggestionItem> suggestionList = new ArrayList<>();

        if (contactScore >= 8) strengths.add("Complete contact and professional links");
        else weaknesses.add("Missing contact links (LinkedIn, GitHub, or Phone)");

        if (summaryScore >= 7) strengths.add("Well-structured professional summary section");
        else weaknesses.add("Brief or missing professional summary statement");

        if (skillsScore >= 18) strengths.add("High technical skill keyword alignment");
        else weaknesses.add("Low keyword match density for industry standards");

        if (experienceScore >= 14) strengths.add("Detailed work experience with metrics and action verbs");
        else weaknesses.add("Work experience lacks quantifiable impact metrics or action verbs");

        if (educationScore >= 8) strengths.add("Clear education and academic degree information");
        if (projectsScore >= 8) strengths.add("Includes key technical projects section");

        if (duplicateKeywordCount > 3) {
            weaknesses.add("Keyword over-stuffing detected (" + duplicateKeywordCount + " repeated words)");
        }

        if (!missingKeywords.isEmpty()) {
            String topMissing = missingKeywords.stream().limit(3).collect(Collectors.joining(", "));
            suggestionList.add(new SuggestionItem("HIGH", "Add crucial missing keywords to your skills section: " + topMissing, 3));
        }
        if (contactScore < 10) {
            suggestionList.add(new SuggestionItem("HIGH", "Include your LinkedIn URL and complete contact information at the top of your resume.", 3));
        }
        if (skillsScore < 20) {
            suggestionList.add(new SuggestionItem("HIGH", "Expand your skills section with modern frameworks, tools, and databases.", 3));
        }
        if (summaryScore < 7) {
            suggestionList.add(new SuggestionItem("MEDIUM", "Write a 3-4 sentence professional summary highlighting your core skills and achievements.", 2));
        }
        if (projectsScore < 8) {
            suggestionList.add(new SuggestionItem("MEDIUM", "Add a 'Key Projects' section detailing technical stacks and measurable outcomes.", 2));
        }
        if (certificationScore < 5) {
            suggestionList.add(new SuggestionItem("LOW", "Add relevant industry certifications (e.g. AWS, Java, Scrum) to boost recruiter trust.", 1));
        }

        suggestionList.sort((s1, s2) -> Integer.compare(s2.getPriorityValue(), s1.getPriorityValue()));
        List<String> sortedSuggestions = suggestionList.stream()
                .map(s -> "[" + s.getPriority() + "] " + s.getText())
                .collect(Collectors.toList());

        return AtsEngineResult.builder()
                .overallScore(overallScore)
                .contactScore(contactScore)
                .summaryScore(summaryScore)
                .skillsScore(skillsScore)
                .experienceScore(experienceScore)
                .educationScore(educationScore)
                .projectsScore(projectsScore)
                .certificationScore(certificationScore)
                .keywordCoverage(keywordCoverage)
                .missingKeywordCount(missingKeywords.size())
                .duplicateKeywordCount(duplicateKeywordCount)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .suggestions(sortedSuggestions)
                .topSkills(topSkills)
                .missingKeywords(missingKeywords.stream().limit(10).collect(Collectors.toList()))
                .duplicateKeywords(duplicateKeywords)
                .categorySkillCounts(categorySkillCounts)
                .factorExplanations(factorExplanations)
                .build();
    }

    private int evaluateContact(ParsedResume r, Map<String, String> factors) {
        int score = 0;
        List<String> items = new ArrayList<>();
        if (r.getEmail() != null && !r.getEmail().isBlank()) { score += 3; items.add("Email"); }
        if (r.getPhone() != null && !r.getPhone().isBlank()) { score += 2; items.add("Phone"); }
        if (r.getLocation() != null && !r.getLocation().isBlank()) { score += 2; items.add("Location"); }
        if (r.getLinkedinUrl() != null || r.getGithubUrl() != null || r.getPortfolioUrl() != null) { score += 3; items.add("Links"); }

        factors.put("Contact Info Factor", String.format("Found: %s (%d/10 pts)", items.isEmpty() ? "None" : String.join(", ", items), score));
        return Math.min(10, score);
    }

    private int evaluateSummary(ParsedResume r, Map<String, String> factors) {
        if (r.getSummary() == null || r.getSummary().isBlank()) {
            factors.put("Summary Factor", "No summary section found (2/10 pts)");
            return 2;
        }

        // Quality analysis: Check for actionable words vs fluff
        String summary = r.getSummary().trim();
        int score = 5; // Base score for present summary
        int actionCount = countActionVerbs(summary);
        int metricsCount = countMetrics(summary);

        if (actionCount > 0) score += 3;
        if (metricsCount > 0) score += 2;

        int finalScore = Math.min(10, score);
        factors.put("Summary Factor", String.format("Summary present with %d action verbs, %d metrics (%d/10 pts)", actionCount, metricsCount, finalScore));
        return finalScore;
    }

    private int evaluateSkills(ParsedResume r, int matchedCount, Map<String, String> factors) {
        int score = Math.min(25, matchedCount * 4);
        if (r.getSkills() != null && !r.getSkills().isBlank()) {
            score = Math.max(score, 8);
        }
        int finalScore = Math.min(25, Math.max(0, score));
        factors.put("Skills Factor", String.format("Matched %d canonical seed skills (%d/25 pts)", matchedCount, finalScore));
        return finalScore;
    }

    private int evaluateExperience(ParsedResume r, Map<String, String> factors) {
        if (r.getExperience() == null || r.getExperience().isBlank()) {
            factors.put("Experience Factor", "No experience section found (0/20 pts)");
            return 0;
        }

        String expText = r.getExperience();
        int score = 6; // Base score for having work experience section

        int actionVerbsCount = countActionVerbs(expText);
        int metricsCount = countMetrics(expText);

        if (actionVerbsCount >= 5) score += 7;
        else if (actionVerbsCount >= 2) score += 4;

        if (metricsCount >= 3) score += 7;
        else if (metricsCount >= 1) score += 4;

        int finalScore = Math.min(20, score);
        factors.put("Experience Factor", String.format("Experience present with %d action verbs, %d quantifiable metrics (%d/20 pts)", actionVerbsCount, metricsCount, finalScore));
        return finalScore;
    }

    private int evaluateEducation(ParsedResume r, Map<String, String> factors) {
        if (r.getEducation() == null || r.getEducation().isBlank()) {
            factors.put("Education Factor", "No education section found (0/10 pts)");
            return 0;
        }
        factors.put("Education Factor", "Education section present (10/10 pts)");
        return 10;
    }

    private int evaluateProjects(ParsedResume r, Map<String, String> factors) {
        if (r.getProjects() == null || r.getProjects().isBlank()) {
            factors.put("Projects Factor", "No projects section found (0/10 pts)");
            return 0;
        }
        factors.put("Projects Factor", "Projects section present (10/10 pts)");
        return 10;
    }

    private int evaluateCertifications(ParsedResume r, Map<String, String> factors) {
        if (r.getCertifications() == null || r.getCertifications().isBlank()) {
            factors.put("Certifications Factor", "No certifications section found (0/5 pts)");
            return 0;
        }
        factors.put("Certifications Factor", "Certifications section present (5/5 pts)");
        return 5;
    }

    private int evaluateFormatting(ParsedResume r, String fullSearchText, int duplicateCount, Map<String, String> factors) {
        int score = 10;
        if (duplicateCount > 5) score -= 4;
        if (fullSearchText == null || fullSearchText.length() < 100) score -= 3;
        int finalScore = Math.max(2, score);
        factors.put("Formatting & Density Factor", String.format("Parseable document structure, %d word repetition flags (%d/10 pts)", duplicateCount, finalScore));
        return finalScore;
    }

    private int countActionVerbs(String text) {
        if (text == null || text.isBlank()) return 0;
        String[] words = text.toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
        int count = 0;
        for (String w : words) {
            if (ACTION_VERBS.contains(w.trim())) {
                count++;
            }
        }
        return count;
    }

    private int countMetrics(String text) {
        if (text == null || text.isBlank()) return 0;
        Matcher m = METRICS_PATTERN.matcher(text);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    private Map<String, Integer> buildWordFrequency(String text) {
        Map<String, Integer> map = new HashMap<>();
        if (text == null) return map;
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9+#.\\s]", " ").split("\\s+");
        Set<String> stopWords = new HashSet<>(Arrays.asList("and", "the", "for", "with", "that", "this", "from", "have", "been"));
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 2 && !stopWords.contains(trimmed)) {
                map.put(trimmed, map.getOrDefault(trimmed, 0) + 1);
            }
        }
        return map;
    }
}
