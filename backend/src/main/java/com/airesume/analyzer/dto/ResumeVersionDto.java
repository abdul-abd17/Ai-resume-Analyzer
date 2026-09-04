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
public class ResumeVersionDto {

    private Long id;
    private Long resumeId;
    private Integer versionNumber;
    private String versionName;
    private String fileName;
    private LocalDateTime createdAt;
    private String createdBy;
    private String changeSummary;
    private Integer atsScore;
    private Integer grammarScore;
    private Integer jobMatchScore;
    private String status;
    private Boolean isCurrent;
}
