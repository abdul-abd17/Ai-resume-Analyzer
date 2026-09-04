package com.airesume.analyzer.service.job;

import com.airesume.analyzer.dto.JobDescriptionDto;
import com.airesume.analyzer.dto.MatchAnalysisDto;
import com.airesume.analyzer.entity.JobDescription;
import com.airesume.analyzer.entity.MatchAnalysis;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.FileStorageException;
import com.airesume.analyzer.exception.InvalidFileTypeException;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.JobDescriptionMapper;
import com.airesume.analyzer.mapper.MatchAnalysisMapper;
import com.airesume.analyzer.repository.JobDescriptionRepository;
import com.airesume.analyzer.repository.MatchAnalysisRepository;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.SkillKeywordRepository;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.service.parser.DocumentExtractor;
import com.airesume.analyzer.service.parser.ResumeParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final MatchAnalysisRepository matchAnalysisRepository;
    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final UserRepository userRepository;
    private final SkillKeywordRepository skillKeywordRepository;
    private final ResumeParserService resumeParserService;
    private final DocumentExtractor documentExtractor;
    private final JobMatchingEngine jobMatchingEngine;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final MatchAnalysisMapper matchAnalysisMapper;

    @Override
    @Transactional
    public JobDescriptionDto uploadJobDescription(JobDescriptionDto dto, MultipartFile file, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        String descriptionText = dto.getDescription();

        // Extract text if a file is uploaded (TXT or PDF)
        if (file != null && !file.isEmpty()) {
            String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.txt";
            try {
                if (originalFileName.toLowerCase().endsWith(".pdf")) {
                    File tempFile = File.createTempFile("jd_", ".pdf");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(file.getBytes());
                    }
                    descriptionText = documentExtractor.extractRawText(tempFile, originalFileName);
                    tempFile.delete();
                } else if (originalFileName.toLowerCase().endsWith(".txt")) {
                    descriptionText = new String(file.getBytes(), StandardCharsets.UTF_8);
                } else {
                    throw new InvalidFileTypeException("Unsupported job description file format. Please upload a TXT or PDF file.");
                }
            } catch (Exception e) {
                throw new FileStorageException("Failed to read uploaded job description file: " + e.getMessage());
            }
        }

        if (!StringUtils.hasText(descriptionText)) {
            throw new IllegalArgumentException("Job description content cannot be empty.");
        }

        String title = StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : "Target Job Role";

        JobDescription jd = JobDescription.builder()
                .title(title)
                .companyName(dto.getCompanyName() != null ? dto.getCompanyName() : "Target Company")
                .location(dto.getLocation())
                .employmentType(dto.getEmploymentType() != null ? dto.getEmploymentType() : "Full-time")
                .description(descriptionText)
                .requiredSkills(dto.getRequiredSkills())
                .preferredSkills(dto.getPreferredSkills())
                .minimumExperience(dto.getMinimumExperience() != null ? dto.getMinimumExperience() : 2)
                .educationRequirement(dto.getEducationRequirement() != null ? dto.getEducationRequirement() : "Bachelor's Degree")
                .createdAt(LocalDateTime.now())
                .status("ACTIVE")
                .user(user)
                .build();

        JobDescription savedJd = jobDescriptionRepository.save(jd);
        return jobDescriptionMapper.toDto(savedJd);
    }

    @Override
    @Transactional
    public MatchAnalysisDto compareResume(Long resumeId, Long jobDescriptionId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to compare this resume.");
        }

        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found with ID: " + jobDescriptionId));

        if (!jd.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this job description.");
        }

        // Auto-parse if parsed resume does not exist (centralized and thread-safe)
        ParsedResume parsedResume = resumeParserService.getOrParseEntity(resumeId, currentUserEmail);

        List<SkillKeyword> seedKeywords = skillKeywordRepository.findByStatus("ACTIVE");

        // Execute Java DSA Matching Engine
        JobMatchingEngine.MatchEngineResult result = jobMatchingEngine.compare(parsedResume, jd, seedKeywords);

        MatchAnalysis matchAnalysis = MatchAnalysis.builder()
                .resume(resume)
                .jobDescription(jd)
                .overallMatchPercentage(result.getOverallMatchPercentage())
                .skillMatchPercentage(result.getSkillMatchPercentage())
                .experienceMatchPercentage(result.getExperienceMatchPercentage())
                .educationMatchPercentage(result.getEducationMatchPercentage())
                .projectMatchPercentage(result.getProjectMatchPercentage())
                .matchedSkills(matchAnalysisMapper.toJson(result.getMatchedSkills()))
                .missingSkills(matchAnalysisMapper.toJson(result.getMissingSkills()))
                .extraSkills(matchAnalysisMapper.toJson(result.getExtraSkills()))
                .matchedKeywords(matchAnalysisMapper.toJson(result.getMatchedKeywords()))
                .missingKeywords(matchAnalysisMapper.toJson(result.getMissingKeywords()))
                .priorityRecommendations(matchAnalysisMapper.toJson(result.getPriorityRecommendations()))
                .hiringRecommendation(result.getHiringRecommendation())
                .createdAt(LocalDateTime.now())
                .build();

        MatchAnalysis savedMatch = matchAnalysisRepository.save(matchAnalysis);
        return matchAnalysisMapper.toDto(savedMatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobDescriptionDto> getJobDescriptionsByUser(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        List<JobDescription> jds = jobDescriptionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return jds.stream().map(jobDescriptionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobDescriptionDto getJobDescriptionById(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        JobDescription jd = jobDescriptionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found with ID: " + id));

        return jobDescriptionMapper.toDto(jd);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchAnalysisDto getMatchAnalysisByResume(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this match analysis.");
        }

        MatchAnalysis matchAnalysis = matchAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("No match analysis found for this resume."));

        return matchAnalysisMapper.toDto(matchAnalysis);
    }

    @Override
    @Transactional
    public void deleteJobDescription(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        JobDescription jd = jobDescriptionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found with ID: " + id));

        matchAnalysisRepository.deleteByJobDescriptionId(id);
        jobDescriptionRepository.delete(jd);
    }
}
