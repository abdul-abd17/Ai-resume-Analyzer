package com.airesume.analyzer.service.ai;

import com.airesume.analyzer.dto.AIRecommendationDto;

public interface AIRecommendationService {

    AIRecommendationDto generateAIRecommendations(Long resumeId, String currentUserEmail);

    AIRecommendationDto getAIRecommendationsByResumeId(Long resumeId, String currentUserEmail);

    void deleteAIRecommendation(Long id, String currentUserEmail);
}
