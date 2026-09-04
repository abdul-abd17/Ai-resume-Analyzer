package com.airesume.analyzer.service.version;

import com.airesume.analyzer.dto.CompareVersionsRequest;
import com.airesume.analyzer.dto.ResumeComparisonDto;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.ResumeComparison;
import com.airesume.analyzer.entity.ResumeVersion;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ResumeComparisonMapper;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeComparisonRepository;
import com.airesume.analyzer.repository.ResumeVersionRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeComparisonRepository resumeComparisonRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final UserRepository userRepository;
    private final ComparisonEngine comparisonEngine;
    private final ResumeComparisonMapper resumeComparisonMapper;

    @Override
    @Transactional
    public ResumeComparisonDto compareVersions(CompareVersionsRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (request.getOldVersionId().equals(request.getNewVersionId())) {
            throw new IllegalArgumentException("Cannot compare a resume version to itself.");
        }

        ResumeVersion oldVer = resumeVersionRepository.findById(request.getOldVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Old resume version not found with ID: " + request.getOldVersionId()));

        ResumeVersion newVer = resumeVersionRepository.findById(request.getNewVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("New resume version not found with ID: " + request.getNewVersionId()));

        if (!oldVer.getResume().getUser().getId().equals(user.getId()) || !newVer.getResume().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to compare these versions.");
        }

        // Check if comparison exists
        Optional<ResumeComparison> existing = resumeComparisonRepository.findByOldVersionIdAndNewVersionId(oldVer.getId(), newVer.getId());
        if (existing.isPresent()) {
            return resumeComparisonMapper.toDto(existing.get());
        }

        ParsedResume oldParsed = parsedResumeRepository.findByResumeId(oldVer.getResume().getId()).orElse(null);
        ParsedResume newParsed = parsedResumeRepository.findByResumeId(newVer.getResume().getId()).orElse(null);

        ComparisonEngine.EngineDiffResult diff = comparisonEngine.compare(oldVer, newVer, oldParsed, newParsed);

        ResumeComparison comparison = ResumeComparison.builder()
                .oldVersion(oldVer)
                .newVersion(newVer)
                .atsDifference(diff.atsDelta)
                .grammarDifference(diff.grammarDelta)
                .jobMatchDifference(diff.jobMatchDelta)
                .addedSkills(resumeComparisonMapper.toJson(diff.addedSkills))
                .removedSkills(resumeComparisonMapper.toJson(diff.removedSkills))
                .addedProjects(resumeComparisonMapper.toJson(diff.addedProjects))
                .removedProjects(resumeComparisonMapper.toJson(diff.removedProjects))
                .addedCertifications(resumeComparisonMapper.toJson(diff.addedCertifications))
                .removedCertifications(resumeComparisonMapper.toJson(diff.removedCertifications))
                .summary(diff.summary)
                .comparisonDate(LocalDateTime.now())
                .build();

        ResumeComparison saved = resumeComparisonRepository.save(comparison);
        return resumeComparisonMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeComparisonDto getComparison(Long oldVersionId, Long newVersionId, String currentUserEmail) {
        return compareVersions(new CompareVersionsRequest() {{
            setOldVersionId(oldVersionId);
            setNewVersionId(newVersionId);
        }}, currentUserEmail);
    }
}
