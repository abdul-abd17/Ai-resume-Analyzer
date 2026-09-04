package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.MatchAnalysisDto;
import com.airesume.analyzer.entity.MatchAnalysis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchAnalysisMapper {

    private final ObjectMapper objectMapper;

    public MatchAnalysisDto toDto(MatchAnalysis entity) {
        if (entity == null) return null;

        return MatchAnalysisDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .resumeFileName(entity.getResume() != null ? entity.getResume().getOriginalFileName() : null)
                .jobDescriptionId(entity.getJobDescription() != null ? entity.getJobDescription().getId() : null)
                .jobTitle(entity.getJobDescription() != null ? entity.getJobDescription().getTitle() : null)
                .companyName(entity.getJobDescription() != null ? entity.getJobDescription().getCompanyName() : null)
                .overallMatchPercentage(entity.getOverallMatchPercentage())
                .skillMatchPercentage(entity.getSkillMatchPercentage())
                .experienceMatchPercentage(entity.getExperienceMatchPercentage())
                .educationMatchPercentage(entity.getEducationMatchPercentage())
                .projectMatchPercentage(entity.getProjectMatchPercentage())
                .keywordCoveragePercentage(entity.getSkillMatchPercentage())
                .matchedSkills(parseJsonList(entity.getMatchedSkills()))
                .missingSkills(parseJsonList(entity.getMissingSkills()))
                .extraSkills(parseJsonList(entity.getExtraSkills()))
                .matchedKeywords(parseJsonList(entity.getMatchedKeywords()))
                .missingKeywords(parseJsonList(entity.getMissingKeywords()))
                .priorityRecommendations(parseJsonList(entity.getPriorityRecommendations()))
                .hiringRecommendation(entity.getHiringRecommendation())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
