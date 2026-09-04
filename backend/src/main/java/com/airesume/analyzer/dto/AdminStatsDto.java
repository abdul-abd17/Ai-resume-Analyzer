package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDto {

    private long totalUsers;
    private long totalRecruiters;
    private long totalResumes;
    private long totalReports;

    private int averageAtsScore;
    private int averageJobMatchPercentage;
    private int averageGrammarScore;

    private long totalAiRequests;
    private String storageUsed;
}
