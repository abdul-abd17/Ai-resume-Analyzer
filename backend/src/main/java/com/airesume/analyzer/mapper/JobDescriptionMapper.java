package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.JobDescriptionDto;
import com.airesume.analyzer.entity.JobDescription;
import org.springframework.stereotype.Component;

@Component
public class JobDescriptionMapper {

    public JobDescriptionDto toDto(JobDescription entity) {
        if (entity == null) return null;

        return JobDescriptionDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .companyName(entity.getCompanyName())
                .location(entity.getLocation())
                .employmentType(entity.getEmploymentType())
                .description(entity.getDescription())
                .requiredSkills(entity.getRequiredSkills())
                .preferredSkills(entity.getPreferredSkills())
                .minimumExperience(entity.getMinimumExperience())
                .educationRequirement(entity.getEducationRequirement())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .build();
    }
}
