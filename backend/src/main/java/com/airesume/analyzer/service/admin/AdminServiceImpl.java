package com.airesume.analyzer.service.admin;

import com.airesume.analyzer.dto.AdminStatsDto;
import com.airesume.analyzer.dto.CandidateSearchDto;
import com.airesume.analyzer.dto.RoleChangeRequest;
import com.airesume.analyzer.dto.UserManagementDto;
import com.airesume.analyzer.entity.AnalysisReport;
import com.airesume.analyzer.entity.GrammarAnalysis;
import com.airesume.analyzer.entity.MatchAnalysis;
import com.airesume.analyzer.entity.Recruiter;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.Role;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final ResumeRepository resumeRepository;
    private final ReportRepository reportRepository;
    private final AnalysisReportRepository analysisReportRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDto getAdminStats() {
        long totalUsers = userRepository.count();
        long totalRecruiters = recruiterRepository.count();
        long totalResumes = resumeRepository.count();
        long totalReports = reportRepository.count();

        List<AnalysisReport> allAts = analysisReportRepository.findAll();
        int avgAts = allAts.isEmpty() ? 0 : (int) Math.round(allAts.stream().mapToInt(AnalysisReport::getOverallScore).average().orElse(0));

        List<GrammarAnalysis> allGrammar = grammarAnalysisRepository.findAll();
        int avgGrammar = allGrammar.isEmpty() ? 0 : (int) Math.round(allGrammar.stream().mapToInt(GrammarAnalysis::getGrammarScore).average().orElse(0));

        List<MatchAnalysis> allMatch = matchAnalysisRepository.findAll();
        int avgMatch = allMatch.isEmpty() ? 0 : (int) Math.round(allMatch.stream().mapToInt(MatchAnalysis::getOverallMatchPercentage).average().orElse(0));

        return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .totalRecruiters(totalRecruiters)
                .totalResumes(totalResumes)
                .totalReports(totalReports)
                .averageAtsScore(avgAts)
                .averageJobMatchPercentage(avgMatch)
                .averageGrammarScore(avgGrammar)
                .totalAiRequests(totalResumes * 2)
                .storageUsed("48.5 MB")
                .build();
    }

    private int getUserAtsAverage(Long userId) {
        List<AnalysisReport> reports = analysisReportRepository.findByResumeUserIdOrderByAnalysisDateDesc(userId);
        if (reports.isEmpty()) return 0;
        return (int) Math.round(reports.stream().mapToInt(AnalysisReport::getOverallScore).average().orElse(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserManagementDto> list = new ArrayList<>();

        for (User u : users) {
            List<Resume> userResumes = resumeRepository.findByUserId(u.getId());
            list.add(UserManagementDto.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .role(u.getRole() != null ? u.getRole().name() : "ROLE_USER")
                    .joinedDate(u.getCreatedAt())
                    .status(u.getStatus() != null ? u.getStatus() : "ACTIVE")
                    .atsAverage(getUserAtsAverage(u.getId()))
                    .resumeCount(userResumes.size())
                    .build());
        }

        return list;
    }

    @Override
    @Transactional
    public UserManagementDto updateUserStatus(Long userId, String status, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(status);
        User saved = userRepository.save(user);

        activityLogService.logActivity(userId, user.getEmail(), "USER_STATUS_CHANGE",
                "Admin " + adminEmail + " changed status of " + user.getEmail() + " to " + status, "127.0.0.1");

        List<Resume> userResumes = resumeRepository.findByUserId(saved.getId());

        return UserManagementDto.builder()
                .id(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole() != null ? saved.getRole().name() : "ROLE_USER")
                .joinedDate(saved.getCreatedAt())
                .status(saved.getStatus())
                .atsAverage(getUserAtsAverage(saved.getId()))
                .resumeCount(userResumes.size())
                .build();
    }

    @Override
    @Transactional
    public UserManagementDto changeUserRole(RoleChangeRequest request, String adminEmail) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        String oldRole = user.getRole() != null ? user.getRole().name() : "ROLE_USER";
        user.setRole(Role.valueOf(request.getNewRole()));
        User saved = userRepository.save(user);

        // If promoted to ROLE_RECRUITER, ensure Recruiter entity exists
        if ("ROLE_RECRUITER".equals(request.getNewRole())) {
            Optional<Recruiter> recOpt = recruiterRepository.findByUserId(user.getId());
            if (recOpt.isEmpty()) {
                Recruiter rec = Recruiter.builder()
                        .user(user)
                        .companyName("Enterprise Partner Inc.")
                        .companyEmail(user.getEmail())
                        .industry("Technology")
                        .website("https://company.example.com")
                        .status("ACTIVE")
                        .build();
                recruiterRepository.save(rec);
            }
        }

        activityLogService.logActivity(user.getId(), user.getEmail(), "ROLE_CHANGE",
                "Admin " + adminEmail + " changed role of " + user.getEmail() + " from " + oldRole + " to " + request.getNewRole(), "127.0.0.1");

        List<Resume> userResumes = resumeRepository.findByUserId(saved.getId());

        return UserManagementDto.builder()
                .id(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .role(saved.getRole() != null ? saved.getRole().name() : "ROLE_USER")
                .joinedDate(saved.getCreatedAt())
                .status(saved.getStatus())
                .atsAverage(getUserAtsAverage(saved.getId()))
                .resumeCount(userResumes.size())
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        activityLogService.logActivity(userId, user.getEmail(), "USER_DELETED",
                "Admin " + adminEmail + " deleted user account: " + user.getEmail(), "127.0.0.1");

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recruiter> getAllRecruiters() {
        return recruiterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateSearchDto> searchCandidates(String skill, Integer minAtsScore, String adminEmail) {
        List<Resume> allResumes = resumeRepository.findAll();
        List<CandidateSearchDto> candidates = new ArrayList<>();

        for (Resume r : allResumes) {
            AnalysisReport ats = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(r.getId()).orElse(null);
            MatchAnalysis match = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(r.getId()).orElse(null);

            int atsScore = ats != null ? ats.getOverallScore() : 0;
            int matchPct = match != null ? match.getOverallMatchPercentage() : 0;
            List<String> topSkills = ats != null && ats.getTopSkills() != null
                    ? Arrays.stream(ats.getTopSkills().split("[,;\\n]+")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                    : Collections.emptyList();

            if (minAtsScore != null && atsScore < minAtsScore) {
                continue;
            }

            candidates.add(CandidateSearchDto.builder()
                    .resumeId(r.getId())
                    .candidateName(r.getUser() != null ? r.getUser().getFullName() : "Candidate")
                    .email(r.getUser() != null ? r.getUser().getEmail() : "candidate@example.com")
                    .fileName(r.getOriginalFileName())
                    .atsScore(atsScore)
                    .matchPercentage(matchPct)
                    .topSkills(topSkills)
                    .uploadedAt(r.getUploadedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .build());
        }

        return candidates;
    }
}
