package com.airesume.analyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {

    @JsonProperty("professionalSummary")
    private String professionalSummary;

    @JsonProperty("improvedExperience")
    private String improvedExperience;

    @JsonProperty("improvedProjects")
    private String improvedProjects;

    @JsonProperty("improvedSkills")
    private String improvedSkills;

    @JsonProperty("keywordRecommendations")
    private String keywordRecommendations;

    @JsonProperty("atsRecommendations")
    private String atsRecommendations;

    @JsonProperty("grammarRecommendations")
    private String grammarRecommendations;

    @JsonProperty("interviewPreparation")
    private String interviewPreparation;

    @JsonProperty("careerSuggestions")
    private String careerSuggestions;

    @JsonProperty("overallFeedback")
    private String overallFeedback;

    /**
     * Validates that all 10 required keys are present and non-blank.
     */
    public boolean isValid() {
        return isNonEmpty(professionalSummary) &&
               isNonEmpty(improvedExperience) &&
               isNonEmpty(improvedProjects) &&
               isNonEmpty(improvedSkills) &&
               isNonEmpty(keywordRecommendations) &&
               isNonEmpty(atsRecommendations) &&
               isNonEmpty(grammarRecommendations) &&
               isNonEmpty(interviewPreparation) &&
               isNonEmpty(careerSuggestions) &&
               isNonEmpty(overallFeedback);
    }

    private boolean isNonEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
