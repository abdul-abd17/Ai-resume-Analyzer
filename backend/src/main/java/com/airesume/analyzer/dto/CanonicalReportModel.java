package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalReportModel {

    private Long reportId;
    private Long resumeId;
    private String reportName;
    private String originalFileName;
    private String generatedBy;
    private LocalDateTime generatedDate;

    // Candidate Info
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateLocation;
    private String summary;

    // Persisted Scores (null if missing/not analyzed)
    private Integer atsScore;
    private Integer grammarScore;
    private Integer jobMatchPercentage;
    private Integer overallStrength;

    // ATS Analysis Data
    private List<String> topSkills;
    private List<String> missingKeywords;
    private List<String> overusedWords;

    // Grammar Analysis Data
    private Integer grammarWeakVerbCount;
    private Integer grammarPassiveCount;
    private List<String> grammarIssues;

    // Job Match Data
    private String targetJobTitle;
    private List<String> matchedJobSkills;
    private List<String> missingJobSkills;

    // AI Recommendation Data
    private String aiProfessionalSummary;
    private String aiImprovedExperience;
    private String aiImprovedProjects;
    private String aiImprovedSkills;
    private String aiKeywordRecommendations;
    private String aiAtsRecommendations;
    private String aiGrammarRecommendations;
    private String aiInterviewPreparation;
    private String aiCareerSuggestions;
    private String aiOverallFeedback;
}
