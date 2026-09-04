package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.ResumeComparisonDto;
import com.airesume.analyzer.entity.ResumeComparison;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeComparisonMapper {

    private final ResumeVersionMapper resumeVersionMapper;
    private final ObjectMapper objectMapper;

    public ResumeComparisonDto toDto(ResumeComparison entity) {
        if (entity == null) return null;

        return ResumeComparisonDto.builder()
                .id(entity.getId())
                .oldVersion(resumeVersionMapper.toDto(entity.getOldVersion()))
                .newVersion(resumeVersionMapper.toDto(entity.getNewVersion()))
                .atsDifference(entity.getAtsDifference())
                .grammarDifference(entity.getGrammarDifference())
                .jobMatchDifference(entity.getJobMatchDifference())
                .addedSkills(fromJsonList(entity.getAddedSkills()))
                .removedSkills(fromJsonList(entity.getRemovedSkills()))
                .addedProjects(fromJsonList(entity.getAddedProjects()))
                .removedProjects(fromJsonList(entity.getRemovedProjects()))
                .addedCertifications(fromJsonList(entity.getAddedCertifications()))
                .removedCertifications(fromJsonList(entity.getRemovedCertifications()))
                .summary(entity.getSummary())
                .comparisonDate(entity.getComparisonDate())
                .build();
    }

    public String toJson(List<String> list) {
        if (list == null) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
