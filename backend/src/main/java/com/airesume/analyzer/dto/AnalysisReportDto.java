package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReportDto {

    private Long id;
    private Long resumeId;
    private String resumeFileName;
    private Integer overallScore;
    private Integer contactScore;
    private Integer summaryScore;
    private Integer skillsScore;
    private Integer experienceScore;
    private Integer educationScore;
    private Integer projectsScore;
    private Integer certificationScore;
    private Integer keywordCoverage;
    private Integer missingKeywordCount;
    private Integer duplicateKeywordCount;

    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private List<Map<String, Object>> topSkills;
    private List<String> missingKeywords;
    private List<String> duplicateKeywords;
    private Map<String, Integer> categorySkillCounts;

    private LocalDateTime analysisDate;
}
