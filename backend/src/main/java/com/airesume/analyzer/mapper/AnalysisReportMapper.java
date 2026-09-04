package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.AnalysisReportDto;
import com.airesume.analyzer.entity.AnalysisReport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalysisReportMapper {

    private final ObjectMapper objectMapper;

    public AnalysisReportDto toDto(AnalysisReport entity) {
        if (entity == null) {
            return null;
        }

        return AnalysisReportDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .resumeFileName(entity.getResume() != null ? entity.getResume().getOriginalFileName() : null)
                .overallScore(entity.getOverallScore())
                .contactScore(entity.getContactScore())
                .summaryScore(entity.getSummaryScore())
                .skillsScore(entity.getSkillsScore())
                .experienceScore(entity.getExperienceScore())
                .educationScore(entity.getEducationScore())
                .projectsScore(entity.getProjectsScore())
                .certificationScore(entity.getCertificationScore())
                .keywordCoverage(entity.getKeywordCoverage())
                .missingKeywordCount(entity.getMissingKeywordCount())
                .duplicateKeywordCount(entity.getDuplicateKeywordCount())
                .strengths(parseJsonList(entity.getResumeStrength()))
                .weaknesses(parseJsonList(entity.getResumeWeakness()))
                .suggestions(parseJsonList(entity.getSuggestions()))
                .topSkills(parseJsonMapList(entity.getTopSkills()))
                .missingKeywords(parseJsonList(entity.getMissingKeywords()))
                .duplicateKeywords(parseJsonList(entity.getDuplicateKeywords()))
                .analysisDate(entity.getAnalysisDate())
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

    private List<Map<String, Object>> parseJsonMapList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
