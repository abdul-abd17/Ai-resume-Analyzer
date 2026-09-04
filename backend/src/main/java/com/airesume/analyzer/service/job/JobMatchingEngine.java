package com.airesume.analyzer.service.job;

import com.airesume.analyzer.entity.JobDescription;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.service.parser.ExperienceDateExtractor;
import com.airesume.analyzer.service.skill.SkillAliasMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class JobMatchingEngine {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RankedSkill {
        private String skill;
        private int frequencyInJd;
        private boolean isMandatory;

        public int getPriorityScore() {
            return (isMandatory ? 10 : 1) * frequencyInJd;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendationItem {
        private String priority; // HIGH, MEDIUM, LOW
        private String text;
        private int priorityLevel; // 3 = HIGH, 2 = MEDIUM, 1 = LOW
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MatchEngineResult {
        private int overallMatchPercentage;
        private int skillMatchPercentage;
        private int experienceMatchPercentage;
        private int educationMatchPercentage;
        private int projectMatchPercentage;
        private int titleMatchPercentage;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<String> extraSkills;
        private List<String> matchedKeywords;
        private List<String> missingKeywords;
        private List<String> priorityRecommendations;
        private String hiringRecommendation;
        private Map<String, String> factorExplanations;
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
            "by", "from", "up", "about", "into", "through", "after", "is", "are", "was", "were",
            "be", "been", "being", "have", "has", "had", "do", "does", "did", "this", "that",
            "we", "you", "our", "their", "must", "should", "will", "required", "experience", "work",
            "role", "candidate", "position", "team", "looking", "seeking", "ability", "strong"
    ));

    public MatchEngineResult compare(ParsedResume resume, JobDescription jd, List<SkillKeyword> seedKeywords) {
        if (resume == null || (!StringUtils.hasText(resume.getRawText()) && !StringUtils.hasText(resume.getSkills()) && !StringUtils.hasText(resume.getExperience()) && !StringUtils.hasText(resume.getSummary()))) {
            return MatchEngineResult.builder()
                    .overallMatchPercentage(0)
                    .skillMatchPercentage(0)
                    .experienceMatchPercentage(0)
                    .educationMatchPercentage(0)
                    .projectMatchPercentage(0)
                    .titleMatchPercentage(0)
                    .matchedSkills(Collections.emptyList())
                    .missingSkills(Collections.emptyList())
                    .extraSkills(Collections.emptyList())
                    .matchedKeywords(Collections.emptyList())
                    .missingKeywords(Collections.emptyList())
                    .priorityRecommendations(Collections.singletonList("[HIGH] Please upload a valid resume to compute job alignment scores."))
                    .hiringRecommendation("Low Match — No resume data available.")
                    .factorExplanations(Collections.singletonMap("Resume Input", "No candidate resume provided (0%)"))
                    .build();
        }
        if (jd == null) {
            jd = JobDescription.builder().build();
        }
        if (seedKeywords == null) {
            seedKeywords = Collections.emptyList();
        }

        String resumeText = resume.getRawText() != null ? resume.getRawText() : "";
        String jdText = buildFullJdText(jd);

        Map<String, String> factorExplanations = new LinkedHashMap<>();

        // 1. Skill Extraction & Boundary-Safe Alias Resolution
        Set<String> jdCanonicalSkills = extractJdSkills(jd, jdText, seedKeywords);
        Set<String> resumeCanonicalSkills = extractResumeSkills(resume, resumeText, seedKeywords);

        // Separate Mandatory vs Preferred Skills PER SKILL
        Set<String> mandatoryJdSkills = new HashSet<>();
        Set<String> preferredJdSkills = new HashSet<>();
        categorizeSkillsMandatoryVsPreferred(jd, jdText, jdCanonicalSkills, mandatoryJdSkills, preferredJdSkills);

        // Matched vs Missing
        Set<String> matchedMandatory = new HashSet<>();
        Set<String> missingMandatory = new HashSet<>();
        for (String mSkill : mandatoryJdSkills) {
            if (isSkillMatchedInResume(mSkill, resumeCanonicalSkills, resumeText)) {
                matchedMandatory.add(mSkill);
            } else {
                missingMandatory.add(mSkill);
            }
        }

        Set<String> matchedPreferred = new HashSet<>();
        Set<String> missingPreferred = new HashSet<>();
        for (String pSkill : preferredJdSkills) {
            if (isSkillMatchedInResume(pSkill, resumeCanonicalSkills, resumeText)) {
                matchedPreferred.add(pSkill);
            } else {
                missingPreferred.add(pSkill);
            }
        }

        Set<String> matchedAllSkills = new HashSet<>(matchedMandatory);
        matchedAllSkills.addAll(matchedPreferred);

        Set<String> missingAllSkills = new HashSet<>(missingMandatory);
        missingAllSkills.addAll(missingPreferred);

        Set<String> extraSkills = new HashSet<>();
        for (String rSkill : resumeCanonicalSkills) {
            if (!jdCanonicalSkills.contains(rSkill)) {
                extraSkills.add(rSkill);
            }
        }

        // Skill Score Calculation (Mandatory = 75% weight of skills score, Preferred = 25%)
        int skillMatchPct = 100;
        if (!jdCanonicalSkills.isEmpty()) {
            double mandatoryScore = mandatoryJdSkills.isEmpty() ? 100.0 : ((double) matchedMandatory.size() / mandatoryJdSkills.size()) * 100.0;
            double preferredScore = preferredJdSkills.isEmpty() ? 100.0 : ((double) matchedPreferred.size() / preferredJdSkills.size()) * 100.0;
            skillMatchPct = (int) Math.round((mandatoryScore * 0.75) + (preferredScore * 0.25));
        }
        factorExplanations.put("Skill Match Factor", String.format("Matched %d/%d mandatory skills and %d/%d preferred skills (%d%%)",
                matchedMandatory.size(), mandatoryJdSkills.size(), matchedPreferred.size(), preferredJdSkills.size(), skillMatchPct));

        // 2. Job Title & Role Relevance Score
        int titleMatchPct = evaluateTitleRelevance(resume, jd, factorExplanations);

        // 3. Deterministic Years of Experience Score
        int expMatchPct = evaluateExperienceMatch(resume, resumeText, jd, factorExplanations);

        // 4. Education Requirements Score
        int eduMatchPct = evaluateEducationMatch(resume, jd, factorExplanations);

        // 5. Project Relevance Score
        int projMatchPct = evaluateProjectMatch(resume, jdCanonicalSkills, factorExplanations);

        // 6. Terminology & Word Overlap Score
        int termMatchPct = evaluateTerminologyOverlap(resumeText, jdText, factorExplanations);

        // 5-Part Weighted Overall Score:
        // Skills: 35%, Experience: 20%, Title Relevance: 15%, Education: 10%, Projects: 10%, Terminology: 10%
        double rawOverall = (skillMatchPct * 0.35)
                + (expMatchPct * 0.20)
                + (titleMatchPct * 0.15)
                + (eduMatchPct * 0.10)
                + (projMatchPct * 0.10)
                + (termMatchPct * 0.10);

        int overallMatchPercentage = Math.min(100, Math.max(0, (int) Math.round(rawOverall)));

        // Priority Ranking for Missing Skills
        Map<String, Integer> jdWordFreq = buildWordFrequency(jdText);
        PriorityQueue<RankedSkill> missingHeap = new PriorityQueue<>(
                Comparator.comparingInt(RankedSkill::getPriorityScore).reversed()
        );

        for (String mSkill : missingAllSkills) {
            boolean isMandatory = mandatoryJdSkills.contains(mSkill);
            int freq = jdWordFreq.getOrDefault(mSkill.toLowerCase(), 1);
            missingHeap.offer(new RankedSkill(mSkill, freq, isMandatory));
        }

        List<String> rankedMissingSkills = new ArrayList<>();
        while (!missingHeap.isEmpty()) {
            rankedMissingSkills.add(capitalize(missingHeap.poll().getSkill()));
        }

        List<String> matchedSkillsList = matchedAllSkills.stream().map(this::capitalize).sorted().collect(Collectors.toList());
        List<String> extraSkillsList = extraSkills.stream().map(this::capitalize).sorted().collect(Collectors.toList());

        // 7. Recommendations
        List<RecommendationItem> recItems = new ArrayList<>();
        if (!missingMandatory.isEmpty()) {
            String top3Mandatory = missingMandatory.stream().map(this::capitalize).limit(3).collect(Collectors.joining(", "));
            recItems.add(new RecommendationItem("HIGH", "Add critical mandatory skills required for this job: " + top3Mandatory, 3));
        }
        if (expMatchPct < 75 && jd.getMinimumExperience() != null) {
            recItems.add(new RecommendationItem("HIGH", String.format("Target role requires %d+ years of experience.", jd.getMinimumExperience()), 3));
        }
        if (titleMatchPct < 60 && StringUtils.hasText(jd.getTitle())) {
            recItems.add(new RecommendationItem("MEDIUM", "Tailor your professional title and summary to align with target role: " + jd.getTitle(), 2));
        }
        if (eduMatchPct < 80 && StringUtils.hasText(jd.getEducationRequirement())) {
            recItems.add(new RecommendationItem("MEDIUM", "Explicitly highlight academic background matching: " + jd.getEducationRequirement(), 2));
        }

        recItems.sort((r1, r2) -> Integer.compare(r2.getPriorityLevel(), r1.getPriorityLevel()));
        List<String> priorityRecommendations = recItems.stream()
                .map(r -> "[" + r.getPriority() + "] " + r.getText())
                .collect(Collectors.toList());

        // Verdict
        String hiringRecommendation;
        if (overallMatchPercentage >= 80) {
            hiringRecommendation = "Strong Match — Candidate metrics align cleanly with target role requirements.";
        } else if (overallMatchPercentage >= 60) {
            hiringRecommendation = "Moderate Match — Targeted skill or experience alignment required.";
        } else {
            hiringRecommendation = "Low Match — Significant requirement gaps identified for target role.";
        }

        return MatchEngineResult.builder()
                .overallMatchPercentage(overallMatchPercentage)
                .skillMatchPercentage(skillMatchPct)
                .experienceMatchPercentage(expMatchPct)
                .educationMatchPercentage(eduMatchPct)
                .projectMatchPercentage(projMatchPct)
                .titleMatchPercentage(titleMatchPct)
                .matchedSkills(matchedSkillsList)
                .missingSkills(rankedMissingSkills)
                .extraSkills(extraSkillsList)
                .matchedKeywords(matchedSkillsList)
                .missingKeywords(rankedMissingSkills)
                .priorityRecommendations(priorityRecommendations)
                .hiringRecommendation(hiringRecommendation)
                .factorExplanations(factorExplanations)
                .build();
    }

    private String buildFullJdText(JobDescription jd) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(jd.getTitle())) sb.append(jd.getTitle()).append(" ");
        if (StringUtils.hasText(jd.getDescription())) sb.append(jd.getDescription()).append(" ");
        if (StringUtils.hasText(jd.getRequiredSkills())) sb.append(jd.getRequiredSkills()).append(" ");
        if (StringUtils.hasText(jd.getPreferredSkills())) sb.append(jd.getPreferredSkills()).append(" ");
        return sb.toString();
    }

    private Set<String> extractJdSkills(JobDescription jd, String jdText, List<SkillKeyword> seedKeywords) {
        Set<String> skills = new HashSet<>();

        if (StringUtils.hasText(jd.getRequiredSkills())) {
            for (String s : jd.getRequiredSkills().split("[,;\\n]+")) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) skills.add(can);
            }
        }
        if (StringUtils.hasText(jd.getPreferredSkills())) {
            for (String s : jd.getPreferredSkills().split("[,;\\n]+")) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) skills.add(can);
            }
        }

        for (SkillKeyword sk : seedKeywords) {
            if (sk != null && sk.getKeyword() != null) {
                if (SkillAliasMapper.containsSkill(jdText, sk.getKeyword())) {
                    skills.add(SkillAliasMapper.getCanonicalSkill(sk.getKeyword()));
                }
            }
        }
        return skills;
    }

    private Set<String> extractResumeSkills(ParsedResume resume, String resumeText, List<SkillKeyword> seedKeywords) {
        Set<String> skills = new HashSet<>();

        if (StringUtils.hasText(resume.getSkills())) {
            for (String s : resume.getSkills().split("[,;\\n]+")) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) skills.add(can);
            }
        }
        if (resume.getSkillsList() != null) {
            for (String s : resume.getSkillsList()) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) skills.add(can);
            }
        }

        for (SkillKeyword sk : seedKeywords) {
            if (sk != null && sk.getKeyword() != null) {
                if (SkillAliasMapper.containsSkill(resumeText, sk.getKeyword())) {
                    skills.add(SkillAliasMapper.getCanonicalSkill(sk.getKeyword()));
                }
            }
        }
        return skills;
    }

    private void categorizeSkillsMandatoryVsPreferred(JobDescription jd, String jdText, Set<String> allSkills, Set<String> mandatory, Set<String> preferred) {
        Set<String> explicitRequired = new HashSet<>();
        if (StringUtils.hasText(jd.getRequiredSkills())) {
            for (String s : jd.getRequiredSkills().split("[,;\\n]+")) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) explicitRequired.add(can);
            }
        }

        Set<String> explicitPreferred = new HashSet<>();
        if (StringUtils.hasText(jd.getPreferredSkills())) {
            for (String s : jd.getPreferredSkills().split("[,;\\n]+")) {
                String can = SkillAliasMapper.getCanonicalSkill(s);
                if (!can.isEmpty()) explicitPreferred.add(can);
            }
        }

        String lowerJd = jdText.toLowerCase();

        for (String skill : allSkills) {
            if (explicitRequired.contains(skill)) {
                mandatory.add(skill);
            } else if (explicitPreferred.contains(skill)) {
                preferred.add(skill);
            } else {
                // Check local sentence context in JD text
                boolean isMandatorySentence = checkSentenceMandatoryContext(lowerJd, skill);
                if (isMandatorySentence) {
                    mandatory.add(skill);
                } else {
                    preferred.add(skill);
                }
            }
        }

        // If no skills categorized as mandatory, treat all as mandatory
        if (mandatory.isEmpty() && !allSkills.isEmpty()) {
            mandatory.addAll(allSkills);
        }
    }

    private boolean checkSentenceMandatoryContext(String jdText, String skill) {
        String[] sentences = jdText.split("[.\\n;]+");
        for (String sentence : sentences) {
            if (SkillAliasMapper.containsSkill(sentence, skill)) {
                if (sentence.contains("must have") || sentence.contains("required") || sentence.contains("minimum") || sentence.contains("essential") || sentence.contains("mandatory")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSkillMatchedInResume(String skill, Set<String> resumeCanonicalSkills, String resumeText) {
        if (resumeCanonicalSkills.contains(skill)) return true;
        return SkillAliasMapper.containsSkill(resumeText, skill);
    }

    private int evaluateTitleRelevance(ParsedResume resume, JobDescription jd, Map<String, String> factors) {
        if (!StringUtils.hasText(jd.getTitle())) {
            factors.put("Job Title Factor", "No target job title specified in JD (100% default)");
            return 100;
        }

        String targetTitle = jd.getTitle().toLowerCase().trim();
        String resumeSummary = resume.getSummary() != null ? resume.getSummary().toLowerCase() : "";
        String resumeExp = resume.getExperience() != null ? resume.getExperience().toLowerCase() : "";

        String[] titleTokens = targetTitle.replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        int matchedTokens = 0;
        int totalTokens = 0;

        for (String token : titleTokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 2 && !STOP_WORDS.contains(trimmed)) {
                totalTokens++;
                if (resumeSummary.contains(trimmed) || resumeExp.contains(trimmed)) {
                    matchedTokens++;
                }
            }
        }

        int score = totalTokens > 0 ? (int) Math.round(((double) matchedTokens / totalTokens) * 100) : 100;
        factors.put("Job Title Factor", String.format("Title '%s' token overlap: %d/%d keywords matched (%d%%)", jd.getTitle(), matchedTokens, totalTokens, score));
        return score;
    }

    private int evaluateExperienceMatch(ParsedResume resume, String resumeText, JobDescription jd, Map<String, String> factors) {
        Integer requiredExpYears = jd.getMinimumExperience();
        int candidateYears = ExperienceDateExtractor.extractTotalExperienceYears(resumeText);

        if (requiredExpYears == null || requiredExpYears <= 0) {
            factors.put("Experience Duration Factor", String.format("Candidate has ~%d years experience. JD specifies no minimum requirement (100%%)", candidateYears));
            return 100;
        }

        int score;
        if (candidateYears >= requiredExpYears) {
            score = 100;
        } else if (candidateYears >= Math.ceil(requiredExpYears * 0.75)) {
            score = 80;
        } else if (candidateYears >= Math.ceil(requiredExpYears * 0.5)) {
            score = 60;
        } else if (candidateYears > 0) {
            score = 40;
        } else {
            score = StringUtils.hasText(resume.getExperience()) ? 50 : 20;
        }

        factors.put("Experience Duration Factor", String.format("Extracted ~%d years experience vs %d required years (%d%%)", candidateYears, requiredExpYears, score));
        return score;
    }

    private int evaluateEducationMatch(ParsedResume resume, JobDescription jd, Map<String, String> factors) {
        String reqEdu = jd.getEducationRequirement();
        String resumeEdu = resume.getEducation();

        if (!StringUtils.hasText(reqEdu)) {
            factors.put("Education Requirement Factor", "No education requirement specified in JD (100%)");
            return 100;
        }

        if (!StringUtils.hasText(resumeEdu)) {
            factors.put("Education Requirement Factor", String.format("JD requires '%s' but candidate education section is empty (40%%)", reqEdu));
            return 40;
        }

        int reqLevel = getDegreeLevel(reqEdu);
        int candidateLevel = getDegreeLevel(resumeEdu);

        int score;
        if (candidateLevel >= reqLevel) {
            score = 100;
        } else {
            score = 70;
        }

        factors.put("Education Requirement Factor", String.format("Candidate education level (%d) vs Required level (%d) (%d%%)", candidateLevel, reqLevel, score));
        return score;
    }

    private int getDegreeLevel(String text) {
        if (text == null) return 1;
        String lower = text.toLowerCase();
        if (lower.contains("phd") || lower.contains("doctor") || lower.contains("doctorate")) return 4;
        if (lower.contains("master") || lower.contains("m.s") || lower.contains("m.tech") || lower.contains("mba")) return 3;
        if (lower.contains("bachelor") || lower.contains("b.s") || lower.contains("b.tech") || lower.contains("degree")) return 2;
        return 1;
    }

    private int evaluateProjectMatch(ParsedResume resume, Set<String> jdSkills, Map<String, String> factors) {
        if (!StringUtils.hasText(resume.getProjects())) {
            factors.put("Projects Relevance Factor", "No projects section present in resume (40%)");
            return 40;
        }

        String projText = resume.getProjects();
        int matchedSkills = 0;
        for (String skill : jdSkills) {
            if (SkillAliasMapper.containsSkill(projText, skill)) {
                matchedSkills++;
            }
        }

        int score = jdSkills.isEmpty() ? 85 : Math.min(100, Math.max(50, 50 + (matchedSkills * 15)));
        factors.put("Projects Relevance Factor", String.format("Projects mention %d target JD technical skills (%d%%)", matchedSkills, score));
        return score;
    }

    private int evaluateTerminologyOverlap(String resumeText, String jdText, Map<String, String> factors) {
        Map<String, Integer> jdWordFreq = buildWordFrequency(jdText);
        Map<String, Integer> resumeWordFreq = buildWordFrequency(resumeText);

        if (jdWordFreq.isEmpty()) {
            factors.put("Terminology Factor", "JD terminology baseline empty (100%)");
            return 100;
        }

        int matchedCount = 0;
        for (String word : jdWordFreq.keySet()) {
            if (resumeWordFreq.containsKey(word)) {
                matchedCount++;
            }
        }

        int score = Math.min(100, (int) Math.round(((double) matchedCount / jdWordFreq.size()) * 100));
        factors.put("Terminology Factor", String.format("Vocabulary overlap: %d/%d JD domain terms (%d%%)", matchedCount, jdWordFreq.size(), score));
        return score;
    }

    private Map<String, Integer> buildWordFrequency(String text) {
        Map<String, Integer> map = new HashMap<>();
        if (text == null) return map;
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9+#.\\s]", " ").split("\\s+");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.length() > 2 && !STOP_WORDS.contains(trimmed)) {
                map.put(trimmed, map.getOrDefault(trimmed, 0) + 1);
            }
        }
        return map;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
