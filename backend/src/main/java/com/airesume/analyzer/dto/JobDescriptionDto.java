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
public class JobDescriptionDto {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private String employmentType;
    private String description;
    private String requiredSkills;
    private String preferredSkills;
    private Integer minimumExperience;
    private String educationRequirement;
    private LocalDateTime createdAt;
    private String status;
    private Long userId;
}
