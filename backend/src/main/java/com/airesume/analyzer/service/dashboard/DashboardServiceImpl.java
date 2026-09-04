package com.airesume.analyzer.service.dashboard;

import com.airesume.analyzer.dto.DashboardDto;
import com.airesume.analyzer.entity.*;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getAggregatedDashboardData(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        List<Resume> userResumes = resumeRepository.findByUserId(user.getId());
        List<AnalysisReport> atsReports = analysisReportRepository.findByResumeUserIdOrderByAnalysisDateDesc(user.getId());

        int totalUploadedResumes = userResumes.size();
        int totalAnalyses = atsReports.size();

        Resume latestResume = userResumes.isEmpty() ? null : userResumes.get(0);
        AnalysisReport latestAts = atsReports.isEmpty() ? null : atsReports.get(0);

        GrammarAnalysis latestGrammar = latestResume != null
                ? grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(latestResume.getId()).orElse(null)
                : null;

        MatchAnalysis latestMatch = latestResume != null
                ? matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(latestResume.getId()).orElse(null)
                : null;

        AIRecommendation latestAI = latestResume != null
                ? aiRecommendationRepository.findTopByResumeIdOrderByCreatedAtDesc(latestResume.getId()).orElse(null)
                : null;

        // Quick Stats
        int avgAts = atsReports.isEmpty() ? 0 : (int) Math.round(atsReports.stream().mapToInt(AnalysisReport::getOverallScore).average().orElse(0));
        int avgGrammar = latestGrammar != null ? latestGrammar.getGrammarScore() : 0;
        int latestMatchPct = latestMatch != null ? latestMatch.getOverallMatchPercentage() : 0;
        int strengthScore = (atsReports.isEmpty() && latestGrammar == null && latestMatch == null)
                ? 0
                : (int) Math.round((avgAts * 0.4) + (avgGrammar * 0.3) + (latestMatchPct * 0.3));

        DashboardDto.QuickStats stats = DashboardDto.QuickStats.builder()
                .averageAtsScore(avgAts)
                .averageGrammarScore(avgGrammar)
                .latestJobMatchPercentage(latestMatchPct)
                .overallResumeStrengthScore(Math.min(100, Math.max(0, strengthScore)))
                .totalAnalysesCount(totalAnalyses)
                .totalUploadedResumes(totalUploadedResumes)
                .latestResumeId(latestResume != null ? latestResume.getId() : null)
                .latestResumeFileName(latestResume != null ? latestResume.getOriginalFileName() : "No Resume Uploaded")
                .build();

        // Chart Data
        Map<String, Object> chartsData = new HashMap<>();

        // Pie Chart Skill Distribution
        List<Map<String, Object>> skillDistributionPie = Collections.emptyList();
        chartsData.put("skillDistributionPie", skillDistributionPie);

        // Bar Chart Grammar Errors
        List<Map<String, Object>> grammarErrorsBar = Arrays.asList(
                Map.of("type", "Spelling", "count", latestGrammar != null ? latestGrammar.getSpellingErrorCount() : 0),
                Map.of("type", "Grammar", "count", latestGrammar != null ? latestGrammar.getGrammarErrorCount() : 0),
                Map.of("type", "Passive Voice", "count", latestGrammar != null ? latestGrammar.getPassiveVoiceCount() : 0),
                Map.of("type", "Weak Verbs", "count", latestGrammar != null ? latestGrammar.getWeakVerbCount() : 0),
                Map.of("type", "Long Sentences", "count", latestGrammar != null ? latestGrammar.getLongSentenceCount() : 0)
        );
        chartsData.put("grammarErrorsBar", grammarErrorsBar);

        // Radar Chart Quality
        List<Map<String, Object>> qualityRadar = Arrays.asList(
                Map.of("subject", "ATS Keywords", "score", avgAts),
                Map.of("subject", "Grammar", "score", avgGrammar),
                Map.of("subject", "Job Alignment", "score", latestMatchPct),
                Map.of("subject", "Experience", "score", 0),
                Map.of("subject", "Projects", "score", 0)
        );
        chartsData.put("qualityRadar", qualityRadar);

        // Line Chart Score History
        List<Map<String, Object>> atsImprovementLine = new ArrayList<>();
        int count = 1;
        for (int i = atsReports.size() - 1; i >= 0; i--) {
            AnalysisReport r = atsReports.get(i);
            atsImprovementLine.add(Map.of(
                    "run", "Run " + count++,
                    "score", r.getOverallScore(),
                    "date", r.getAnalysisDate().format(DateTimeFormatter.ofPattern("MMM dd"))
            ));
        }
        chartsData.put("atsImprovementLine", atsImprovementLine);

        // Skill Analytics
        List<String> topSkillsList = latestAts != null && latestAts.getTopSkills() != null
                ? Arrays.stream(latestAts.getTopSkills().split("[,;\\n]+")).map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList())
                : Collections.emptyList();

        List<String> missingSkillsList = latestAts != null && latestAts.getMissingKeywords() != null
                ? Arrays.stream(latestAts.getMissingKeywords().split("[,;\\n]+")).map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList())
                : Collections.emptyList();

        Map<String, Object> skillAnalytics = Map.of(
                "topSkills", topSkillsList,
                "missingSkills", missingSkillsList,
                "keywordCoverage", latestAts != null ? latestAts.getKeywordCoverage() : 0,
                "trendingSkills", Collections.emptyList()
        );

        // Grammar Analytics
        Map<String, Object> grammarAnalytics = Map.of(
                "grammarScore", avgGrammar,
                "readabilityScore", latestGrammar != null ? latestGrammar.getReadabilityScore() : 0,
                "passiveVoiceCount", latestGrammar != null ? latestGrammar.getPassiveVoiceCount() : 0,
                "weakVerbCount", latestGrammar != null ? latestGrammar.getWeakVerbCount() : 0,
                "spellingErrorCount", latestGrammar != null ? latestGrammar.getSpellingErrorCount() : 0
        );

        // Job Match Analytics
        Map<String, Object> jobMatchAnalytics = Map.of(
                "matchPercentage", latestMatchPct,
                "matchedSkills", latestMatch != null && latestMatch.getMatchedSkills() != null ? latestMatch.getMatchedSkills() : "Not analyzed",
                "missingSkills", latestMatch != null && latestMatch.getMissingSkills() != null ? latestMatch.getMissingSkills() : "Not analyzed",
                "hiringRecommendation", latestMatch != null && latestMatch.getHiringRecommendation() != null ? latestMatch.getHiringRecommendation() : "Not analyzed"
        );

        // AI Analytics
        Map<String, Object> aiAnalytics = Map.of(
                "professionalSummary", latestAI != null ? latestAI.getProfessionalSummary() : "Not analyzed",
                "improvedExperience", latestAI != null ? latestAI.getImprovedExperience() : "Not analyzed",
                "interviewPrep", latestAI != null ? latestAI.getInterviewPreparation() : "Not analyzed"
        );

        // Recent Activity Timeline
        List<DashboardDto.RecentActivityItem> recentActivity = new ArrayList<>();
        if (latestResume != null) {
            recentActivity.add(DashboardDto.RecentActivityItem.builder()
                    .id("act-1")
                    .title("Uploaded Resume: " + latestResume.getOriginalFileName())
                    .type("RESUME_UPLOAD")
                    .timestamp(latestResume.getUploadedAt() != null ? latestResume.getUploadedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Recently")
                    .statusBadge("Uploaded")
                    .resumeId(latestResume.getId())
                    .build());
        }
        if (latestAts != null) {
            recentActivity.add(DashboardDto.RecentActivityItem.builder()
                    .id("act-2")
                    .title("ATS Analysis Score Calculated: " + latestAts.getOverallScore() + "/100")
                    .type("ATS_ANALYSIS")
                    .timestamp(latestAts.getAnalysisDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
                    .statusBadge("Score " + latestAts.getOverallScore())
                    .resumeId(latestAts.getResume().getId())
                    .build());
        }

        // Notifications Feed
        List<DashboardDto.NotificationItem> notifications = Arrays.asList(
                DashboardDto.NotificationItem.builder()
                        .id("notif-1")
                        .title("Resume Parsed Successfully")
                        .message("Apache PDFBox extracted text and sections for " + (latestResume != null ? latestResume.getOriginalFileName() : "resume.pdf"))
                        .timestamp("5 mins ago")
                        .read(false)
                        .type("SUCCESS")
                        .build(),
                DashboardDto.NotificationItem.builder()
                        .id("notif-2")
                        .title("ATS Analysis Ready")
                        .message("Java DSA Max-Heap evaluated your resume score: " + avgAts + "/100")
                        .timestamp("10 mins ago")
                        .read(false)
                        .type("INFO")
                        .build(),
                DashboardDto.NotificationItem.builder()
                        .id("notif-3")
                        .title("AI Recommendations Available")
                        .message("Pluggable AI provider synthesized custom bullet points and interview prep questions.")
                        .timestamp("1 hour ago")
                        .read(true)
                        .type("INFO")
                        .build()
        );

        return DashboardDto.builder()
                .quickStats(stats)
                .chartsData(chartsData)
                .skillAnalytics(skillAnalytics)
                .grammarAnalytics(grammarAnalytics)
                .jobMatchAnalytics(jobMatchAnalytics)
                .aiAnalytics(aiAnalytics)
                .recentActivity(recentActivity)
                .notifications(notifications)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto.QuickStats getQuickStats(String currentUserEmail) {
        DashboardDto dto = getAggregatedDashboardData(currentUserEmail);
        return dto.getQuickStats();
    }
}
