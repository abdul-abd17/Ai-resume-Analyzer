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
public class ReportDto {

    private Long id;
    private Long resumeId;
    private String resumeFileName;
    private String reportName;
    private String reportType;
    private String generatedBy;
    private LocalDateTime generatedDate;
    private Long fileSize;
    private String status;
    private Integer downloadCount;
    private Integer sharedCount;

    private Integer atsScore;
    private Integer grammarScore;
    private Integer jobMatchScore;
}
