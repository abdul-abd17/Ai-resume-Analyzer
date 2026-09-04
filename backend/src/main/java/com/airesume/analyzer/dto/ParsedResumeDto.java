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
public class ParsedResumeDto {

    private Long id;
    private Long resumeId;
    private String fullName;
    private String email;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String location;
    private String summary;
    private List<String> skillsList;
    private String skills;
    private String education;
    private String experience;
    private String projects;
    private String certifications;
    private String languages;
    private String achievements;
    private String rawText;
    private LocalDateTime parsedAt;
}
