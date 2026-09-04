package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.AIRecommendationDto;
import com.airesume.analyzer.entity.AIRecommendation;
import org.springframework.stereotype.Component;

@Component
public class AIRecommendationMapper {

    public AIRecommendationDto toDto(AIRecommendation entity) {
        if (entity == null) return null;

        return AIRecommendationDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .resumeFileName(entity.getResume() != null ? entity.getResume().getOriginalFileName() : null)
                .professionalSummary(entity.getProfessionalSummary())
                .improvedExperience(entity.getImprovedExperience())
                .improvedProjects(entity.getImprovedProjects())
                .improvedSkills(entity.getImprovedSkills())
                .keywordRecommendations(entity.getKeywordRecommendations())
                .atsRecommendations(entity.getAtsRecommendations())
                .grammarRecommendations(entity.getGrammarRecommendations())
                .interviewPreparation(entity.getInterviewPreparation())
                .careerSuggestions(entity.getCareerSuggestions())
                .overallFeedback(entity.getOverallFeedback())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
