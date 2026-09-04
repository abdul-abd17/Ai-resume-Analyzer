package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.CanonicalReportModel;
import com.airesume.analyzer.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportModelBuilder {

    public CanonicalReportModel buildCanonicalModel(
            Resume resume,
            ParsedResume parsed,
            AnalysisReport ats,
            GrammarAnalysis grammar,
            MatchAnalysis match,
            AIRecommendation ai
    ) {
        String originalFileName = resume != null ? resume.getOriginalFileName() : "resume.pdf";
        String candidateName = parsed != null && isNonEmpty(parsed.getFullName()) ? parsed.getFullName() : "Candidate";
        String candidateEmail = parsed != null && isNonEmpty(parsed.getEmail()) ? parsed.getEmail() : "N/A";
        String candidatePhone = parsed != null && isNonEmpty(parsed.getPhone()) ? parsed.getPhone() : "N/A";
        String candidateLocation = parsed != null && isNonEmpty(parsed.getLocation()) ? parsed.getLocation() : "N/A";
        String summary = parsed != null && isNonEmpty(parsed.getSummary()) ? parsed.getSummary() : "Not analyzed";

        Integer atsScore = ats != null ? ats.getOverallScore() : null;
        Integer grammarScore = grammar != null ? grammar.getGrammarScore() : null;
        Integer jobMatchPercentage = match != null ? match.getOverallMatchPercentage() : null;

        Integer overallStrength = null;
        if (atsScore != null && grammarScore != null && jobMatchPercentage != null) {
            overallStrength = (atsScore + grammarScore + jobMatchPercentage) / 3;
        } else if (atsScore != null || grammarScore != null || jobMatchPercentage != null) {
            int total = 0;
            int count = 0;
            if (atsScore != null) { total += atsScore; count++; }
            if (grammarScore != null) { total += grammarScore; count++; }
            if (jobMatchPercentage != null) { total += jobMatchPercentage; count++; }
            overallStrength = total / count;
        }

        List<String> topSkills = ats != null && isNonEmpty(ats.getTopSkills())
                ? parseList(ats.getTopSkills())
                : (parsed != null && isNonEmpty(parsed.getSkills()) ? parseList(parsed.getSkills()) : Collections.emptyList());

        List<String> missingKeywords = ats != null && isNonEmpty(ats.getMissingKeywords()) ? parseList(ats.getMissingKeywords()) : Collections.emptyList();
        List<String> overusedWords = ats != null && isNonEmpty(ats.getDuplicateKeywords()) ? parseList(ats.getDuplicateKeywords()) : Collections.emptyList();

        Integer grammarWeakVerbCount = grammar != null ? grammar.getWeakVerbCount() : null;
        Integer grammarPassiveCount = grammar != null ? grammar.getPassiveVoiceCount() : null;
        List<String> grammarIssues = grammar != null && isNonEmpty(grammar.getWeakVerbs()) ? parseList(grammar.getWeakVerbs()) : Collections.emptyList();

        String targetJobTitle = match != null && match.getJobDescription() != null && isNonEmpty(match.getJobDescription().getTitle())
                ? match.getJobDescription().getTitle()
                : "Not specified";

        List<String> matchedJobSkills = match != null && isNonEmpty(match.getMatchedSkills()) ? parseList(match.getMatchedSkills()) : Collections.emptyList();
        List<String> missingJobSkills = match != null && isNonEmpty(match.getMissingSkills()) ? parseList(match.getMissingSkills()) : Collections.emptyList();

        String aiProfSummary = ai != null && isNonEmpty(ai.getProfessionalSummary()) ? ai.getProfessionalSummary() : "Not analyzed";
        String aiImprovedExperience = ai != null && isNonEmpty(ai.getImprovedExperience()) ? ai.getImprovedExperience() : "Not analyzed";
        String aiImprovedProjects = ai != null && isNonEmpty(ai.getImprovedProjects()) ? ai.getImprovedProjects() : "Not analyzed";
        String aiImprovedSkills = ai != null && isNonEmpty(ai.getImprovedSkills()) ? ai.getImprovedSkills() : "Not analyzed";
        String aiKeywordRecommendations = ai != null && isNonEmpty(ai.getKeywordRecommendations()) ? ai.getKeywordRecommendations() : "Not analyzed";
        String aiAtsRecommendations = ai != null && isNonEmpty(ai.getAtsRecommendations()) ? ai.getAtsRecommendations() : "Not analyzed";
        String aiGrammarRecommendations = ai != null && isNonEmpty(ai.getGrammarRecommendations()) ? ai.getGrammarRecommendations() : "Not analyzed";
        String aiInterviewPrep = ai != null && isNonEmpty(ai.getInterviewPreparation()) ? ai.getInterviewPreparation() : "Not analyzed";
        String aiCareerSuggestions = ai != null && isNonEmpty(ai.getCareerSuggestions()) ? ai.getCareerSuggestions() : "Not analyzed";
        String aiOverallFeedback = ai != null && isNonEmpty(ai.getOverallFeedback()) ? ai.getOverallFeedback() : "Not analyzed";

        return CanonicalReportModel.builder()
                .resumeId(resume != null ? resume.getId() : null)
                .originalFileName(originalFileName)
                .generatedDate(LocalDateTime.now())
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .candidatePhone(candidatePhone)
                .candidateLocation(candidateLocation)
                .summary(summary)
                .atsScore(atsScore)
                .grammarScore(grammarScore)
                .jobMatchPercentage(jobMatchPercentage)
                .overallStrength(overallStrength)
                .topSkills(topSkills)
                .missingKeywords(missingKeywords)
                .overusedWords(overusedWords)
                .grammarWeakVerbCount(grammarWeakVerbCount)
                .grammarPassiveCount(grammarPassiveCount)
                .grammarIssues(grammarIssues)
                .targetJobTitle(targetJobTitle)
                .matchedJobSkills(matchedJobSkills)
                .missingJobSkills(missingJobSkills)
                .aiProfessionalSummary(aiProfSummary)
                .aiImprovedExperience(aiImprovedExperience)
                .aiImprovedProjects(aiImprovedProjects)
                .aiImprovedSkills(aiImprovedSkills)
                .aiKeywordRecommendations(aiKeywordRecommendations)
                .aiAtsRecommendations(aiAtsRecommendations)
                .aiGrammarRecommendations(aiGrammarRecommendations)
                .aiInterviewPreparation(aiInterviewPrep)
                .aiCareerSuggestions(aiCareerSuggestions)
                .aiOverallFeedback(aiOverallFeedback)
                .build();
    }

    private boolean isNonEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private List<String> parseList(String text) {
        if (text == null || text.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(text.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
