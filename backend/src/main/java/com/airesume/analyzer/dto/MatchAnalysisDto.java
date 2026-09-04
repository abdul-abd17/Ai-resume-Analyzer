package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAnalysisDto {

    private Long id;
    private Long resumeId;
    private String resumeFileName;
    private Long jobDescriptionId;
    private String jobTitle;
    private String companyName;

    private Integer overallMatchPercentage;
    private Integer skillMatchPercentage;
    private Integer experienceMatchPercentage;
    private Integer educationMatchPercentage;
    private Integer projectMatchPercentage;
    private Integer keywordCoveragePercentage;

    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> extraSkills;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<String> priorityRecommendations;
    private String hiringRecommendation;

    private LocalDateTime createdAt;
}
