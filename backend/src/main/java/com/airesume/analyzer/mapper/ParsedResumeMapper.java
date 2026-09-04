package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.ParsedResumeDto;
import com.airesume.analyzer.entity.ParsedResume;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParsedResumeMapper {

    public ParsedResumeDto toDto(ParsedResume entity) {
        if (entity == null) {
            return null;
        }

        List<String> skillsList = Collections.emptyList();
        if (StringUtils.hasText(entity.getSkills())) {
            skillsList = Arrays.stream(entity.getSkills().split("[,•|;\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }

        return ParsedResumeDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .linkedinUrl(entity.getLinkedinUrl())
                .githubUrl(entity.getGithubUrl())
                .portfolioUrl(entity.getPortfolioUrl())
                .location(entity.getLocation())
                .summary(entity.getSummary())
                .skillsList(skillsList)
                .skills(entity.getSkills())
                .education(entity.getEducation())
                .experience(entity.getExperience())
                .projects(entity.getProjects())
                .certifications(entity.getCertifications())
                .languages(entity.getLanguages())
                .achievements(entity.getAchievements())
                .rawText(entity.getRawText())
                .parsedAt(entity.getParsedAt())
                .build();
    }
}
