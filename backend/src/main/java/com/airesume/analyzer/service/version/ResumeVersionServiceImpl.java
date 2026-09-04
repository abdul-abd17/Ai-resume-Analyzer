package com.airesume.analyzer.service.version;

import com.airesume.analyzer.dto.ResumeVersionDto;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.ResumeVersion;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ResumeVersionMapper;
import com.airesume.analyzer.repository.ResumeVersionRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.airesume.analyzer.entity.AnalysisReport;
import com.airesume.analyzer.entity.GrammarAnalysis;
import com.airesume.analyzer.entity.MatchAnalysis;
import com.airesume.analyzer.repository.AnalysisReportRepository;
import com.airesume.analyzer.repository.GrammarAnalysisRepository;
import com.airesume.analyzer.repository.MatchAnalysisRepository;

@Service
@RequiredArgsConstructor
public class ResumeVersionServiceImpl implements ResumeVersionService {

    private final ResumeVersionRepository resumeVersionRepository;
    private final UserRepository userRepository;
    private final ResumeVersionMapper resumeVersionMapper;
    private final AnalysisReportRepository analysisReportRepository;
    private final GrammarAnalysisRepository grammarAnalysisRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;

    @Override
    @Transactional
    public ResumeVersionDto createNextVersion(Resume resume, String currentUserEmail) {
        Long userId = resume.getUser().getId();
        Integer maxVer = resumeVersionRepository.findMaxVersionNumberByUserId(userId);
        int nextVerNum = (maxVer != null ? maxVer : 0) + 1;

        // Mark previous versions as isCurrent = false
        List<ResumeVersion> existingVers = resumeVersionRepository.findByResumeUserIdOrderByVersionNumberDesc(userId);
        for (ResumeVersion v : existingVers) {
            v.setIsCurrent(false);
            resumeVersionRepository.save(v);
        }

        AnalysisReport ats = analysisReportRepository.findTopByResumeIdOrderByAnalysisDateDesc(resume.getId()).orElse(null);
        GrammarAnalysis grammar = grammarAnalysisRepository.findTopByResumeIdOrderByAnalysisDateDesc(resume.getId()).orElse(null);
        MatchAnalysis match = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resume.getId()).orElse(null);

        ResumeVersion newVer = ResumeVersion.builder()
                .resume(resume)
                .versionNumber(nextVerNum)
                .versionName("Version " + nextVerNum + " - " + resume.getOriginalFileName())
                .fileName(resume.getFileName())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUserEmail)
                .changeSummary("Uploaded new resume file: " + resume.getOriginalFileName())
                .atsScore(ats != null ? ats.getOverallScore() : null)
                .grammarScore(grammar != null ? grammar.getGrammarScore() : null)
                .jobMatchScore(match != null ? match.getOverallMatchPercentage() : null)
                .status("ACTIVE")
                .isCurrent(true)
                .build();

        ResumeVersion saved = resumeVersionRepository.save(newVer);
        return resumeVersionMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeVersionDto> getVersionsForCurrentUser(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        List<ResumeVersion> versions = resumeVersionRepository.findByResumeUserIdOrderByVersionNumberDesc(user.getId());
        return versions.stream().map(resumeVersionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeVersionDto getVersionById(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        ResumeVersion ver = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!ver.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this version.");
        }

        return resumeVersionMapper.toDto(ver);
    }

    @Override
    @Transactional
    public ResumeVersionDto renameVersion(Long id, String newName, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        ResumeVersion ver = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!ver.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to rename this version.");
        }

        ver.setVersionName(newName);
        ResumeVersion saved = resumeVersionRepository.save(ver);
        return resumeVersionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ResumeVersionDto restoreVersion(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        ResumeVersion targetVer = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!targetVer.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to restore this version.");
        }

        // Set all other versions to isCurrent = false
        List<ResumeVersion> existingVers = resumeVersionRepository.findByResumeUserIdOrderByVersionNumberDesc(user.getId());
        for (ResumeVersion v : existingVers) {
            v.setIsCurrent(v.getId().equals(id));
            resumeVersionRepository.save(v);
        }

        return resumeVersionMapper.toDto(targetVer);
    }

    @Override
    @Transactional
    public void deleteVersion(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        ResumeVersion ver = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!ver.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this version.");
        }

        if (Boolean.TRUE.equals(ver.getIsCurrent())) {
            throw new IllegalArgumentException("Cannot delete the current active version. Please restore another version first.");
        }

        resumeVersionRepository.delete(ver);
    }
}
