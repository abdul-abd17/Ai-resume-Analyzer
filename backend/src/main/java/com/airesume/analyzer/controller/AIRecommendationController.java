package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.AIRecommendationDto;
import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.service.ai.AIRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final AIRecommendationService aiRecommendationService;

    @PostMapping("/analyze/{resumeId}")
    public ResponseEntity<AIRecommendationDto> generateAIRecommendations(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AIRecommendationDto dto = aiRecommendationService.generateAIRecommendations(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<AIRecommendationDto> getAIRecommendations(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AIRecommendationDto dto = aiRecommendationService.getAIRecommendationsByResumeId(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAIRecommendation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        aiRecommendationService.deleteAIRecommendation(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("AI Recommendation deleted successfully")
                .success(true)
                .build());
    }
}
