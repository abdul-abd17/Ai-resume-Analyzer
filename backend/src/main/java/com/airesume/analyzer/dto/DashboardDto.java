package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class QuickStats {
        private int averageAtsScore;
        private int averageGrammarScore;
        private int latestJobMatchPercentage;
        private int overallResumeStrengthScore;
        private int totalAnalysesCount;
        private int totalUploadedResumes;
        private Long latestResumeId;
        private String latestResumeFileName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RecentActivityItem {
        private String id;
        private String title;
        private String type; // RESUME_UPLOAD, ATS_ANALYSIS, GRAMMAR_ANALYSIS, JOB_MATCH, AI_RECOMMENDATION
        private String timestamp;
        private String statusBadge;
        private Long resumeId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class NotificationItem {
        private String id;
        private String title;
        private String message;
        private String timestamp;
        private boolean read;
        private String type; // INFO, SUCCESS, WARNING
    }

    private QuickStats quickStats;
    private Map<String, Object> chartsData;
    private Map<String, Object> skillAnalytics;
    private Map<String, Object> grammarAnalytics;
    private Map<String, Object> jobMatchAnalytics;
    private Map<String, Object> aiAnalytics;
    private List<RecentActivityItem> recentActivity;
    private List<NotificationItem> notifications;
}
