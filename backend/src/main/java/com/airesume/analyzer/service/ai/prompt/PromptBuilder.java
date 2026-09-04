package com.airesume.analyzer.service.ai.prompt;

import com.airesume.analyzer.entity.AnalysisReport;
import com.airesume.analyzer.entity.GrammarAnalysis;
import com.airesume.analyzer.entity.MatchAnalysis;
import com.airesume.analyzer.entity.ParsedResume;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(ParsedResume resume, AnalysisReport atsReport, GrammarAnalysis grammarReport, MatchAnalysis matchReport) {
        String name = resume != null && resume.getFullName() != null ? resume.getFullName() : "Candidate";
        String summary = resume != null && resume.getSummary() != null ? resume.getSummary() : "None provided";
        String skills = resume != null && resume.getSkills() != null ? resume.getSkills() : "None extracted";
        String experience = resume != null && resume.getExperience() != null ? resume.getExperience() : "None provided";
        String education = resume != null && resume.getEducation() != null ? resume.getEducation() : "None provided";
        String projects = resume != null && resume.getProjects() != null ? resume.getProjects() : "None provided";

        int atsScore = atsReport != null ? atsReport.getOverallScore() : 0;
        String topSkills = atsReport != null && atsReport.getTopSkills() != null ? atsReport.getTopSkills() : "Not analyzed";
        String missingKeywords = atsReport != null && atsReport.getMissingKeywords() != null ? atsReport.getMissingKeywords() : "Not analyzed";
        String overusedWords = atsReport != null && atsReport.getDuplicateKeywords() != null ? atsReport.getDuplicateKeywords() : "Not analyzed";

        int grammarScore = grammarReport != null ? grammarReport.getGrammarScore() : 0;
        String spellingErrors = grammarReport != null && grammarReport.getSpellingErrors() != null ? grammarReport.getSpellingErrors() : "Not analyzed";
        String weakVerbs = grammarReport != null && grammarReport.getWeakVerbs() != null ? grammarReport.getWeakVerbs() : "Not analyzed";
        int passiveCount = grammarReport != null ? grammarReport.getPassiveVoiceCount() : 0;

        String jobTitle = matchReport != null && matchReport.getJobDescription() != null ? matchReport.getJobDescription().getTitle() : "Not specified";
        int matchPct = matchReport != null ? matchReport.getOverallMatchPercentage() : 0;
        String matchedSkills = matchReport != null && matchReport.getMatchedSkills() != null ? matchReport.getMatchedSkills() : "Not analyzed";
        String missingJobSkills = matchReport != null && matchReport.getMissingSkills() != null ? matchReport.getMissingSkills() : "Not analyzed";

        return String.format(
                PromptTemplates.COMPREHENSIVE_RECOMMENDATION_PROMPT,
                name, summary, skills, experience, education, projects,
                atsScore, topSkills, missingKeywords, overusedWords,
                grammarScore, spellingErrors, weakVerbs, passiveCount,
                jobTitle, matchPct, matchedSkills, missingJobSkills
        );
    }
}
