package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendationDto {

    private Long id;
    private Long resumeId;
    private String resumeFileName;

    private String professionalSummary;
    private String improvedExperience;
    private String improvedProjects;
    private String improvedSkills;
    private String keywordRecommendations;
    private String atsRecommendations;
    private String grammarRecommendations;
    private String interviewPreparation;
    private String careerSuggestions;
    private String overallFeedback;

    private LocalDateTime createdAt;
}
