package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportPreviewDto {

    private ReportDto reportInfo;
    private String candidateName;
    private String candidateEmail;
    private String summary;
    private Integer atsScore;
    private Integer grammarScore;
    private Integer jobMatchPercentage;

    private List<String> topSkills;
    private List<String> missingKeywords;
    private List<String> grammarIssues;
    private String aiProfessionalSummary;
    private String aiImprovedExperience;
    private String aiInterviewPreparation;
    private String htmlContent;
}
