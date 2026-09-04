package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSearchDto {

    private Long resumeId;
    private String candidateName;
    private String email;
    private String fileName;
    private int atsScore;
    private int matchPercentage;
    private List<String> topSkills;
    private String uploadedAt;
}
