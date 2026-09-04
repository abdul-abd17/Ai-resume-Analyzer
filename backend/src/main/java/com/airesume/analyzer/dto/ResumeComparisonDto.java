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
public class ResumeComparisonDto {

    private Long id;
    private ResumeVersionDto oldVersion;
    private ResumeVersionDto newVersion;

    private Integer atsDifference;
    private Integer grammarDifference;
    private Integer jobMatchDifference;

    private List<String> addedSkills;
    private List<String> removedSkills;
    private List<String> addedProjects;
    private List<String> removedProjects;
    private List<String> addedCertifications;
    private List<String> removedCertifications;

    private String summary;
    private LocalDateTime comparisonDate;
}
