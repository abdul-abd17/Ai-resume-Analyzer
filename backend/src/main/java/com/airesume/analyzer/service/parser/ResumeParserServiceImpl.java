package com.airesume.analyzer.service.parser;

import com.airesume.analyzer.dto.ParsedResumeDto;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.FileStorageException;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ParsedResumeMapper;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeParserServiceImpl implements ResumeParserService {

    private final ResumeRepository resumeRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final UserRepository userRepository;
    private final DocumentExtractor documentExtractor;
    private final TextCleanerService textCleanerService;
    private final RegexExtractor regexExtractor;
    private final SectionDetector sectionDetector;
    private final ParsedResumeMapper parsedResumeMapper;
    private final ParsedResumePersistenceHelper parsedResumePersistenceHelper;

    @Override
    @Transactional
    public ParsedResumeDto parseResume(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Use pessimistic lock to serialize concurrent parsing for the same resume
        Resume resume = resumeRepository.findByIdWithLock(resumeId)
                .orElseGet(() -> resumeRepository.findById(resumeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId)));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to parse this resume.");
        }

        Path filePath = Paths.get(resume.getStoragePath()).normalize();
        File file = filePath.toFile();
        if (!file.exists()) {
            throw new FileStorageException("Resume file missing from storage path: " + resume.getOriginalFileName());
        }

        // 1. Extract raw text from PDF/DOCX (done before DB save to ensure failure won't partially persist)
        String extractedRawText = documentExtractor.extractRawText(file, resume.getOriginalFileName());

        // 2. Clean and normalize text
        String cleanedText = textCleanerService.cleanText(extractedRawText);

        // 3. Extract Regex Contact & Info Fields
        String email = regexExtractor.extractEmail(cleanedText);
        String phone = regexExtractor.extractPhone(cleanedText);
        String linkedinUrl = regexExtractor.extractLinkedin(cleanedText);
        String githubUrl = regexExtractor.extractGithub(cleanedText);
        String portfolioUrl = regexExtractor.extractPortfolio(cleanedText);
        String location = regexExtractor.extractLocation(cleanedText);
        String fullName = regexExtractor.extractFullName(cleanedText, email);

        // 4. Detect and Split Sections
        Map<SectionDetector.SectionType, String> sections = sectionDetector.detectAndSplitSections(cleanedText);

        String summary = sections.get(SectionDetector.SectionType.SUMMARY);
        String skills = sections.get(SectionDetector.SectionType.SKILLS);
        String education = sections.get(SectionDetector.SectionType.EDUCATION);
        String experience = sections.get(SectionDetector.SectionType.EXPERIENCE);
        String projects = sections.get(SectionDetector.SectionType.PROJECTS);
        String certifications = sections.get(SectionDetector.SectionType.CERTIFICATIONS);
        String languages = sections.get(SectionDetector.SectionType.LANGUAGES);
        String achievements = sections.get(SectionDetector.SectionType.ACHIEVEMENTS);

        // 5. Idempotent Create or Update
        Optional<ParsedResume> existingOpt = parsedResumeRepository.findByResumeId(resumeId);
        ParsedResume savedParsedResume;

        if (existingOpt.isPresent()) {
            ParsedResume parsedResume = existingOpt.get();
            populateParsedResume(parsedResume, fullName, email, phone, linkedinUrl, githubUrl, portfolioUrl,
                    location, summary, skills, education, experience, projects, certifications,
                    languages, achievements, cleanedText, user);
            savedParsedResume = parsedResumeRepository.saveAndFlush(parsedResume);
        } else {
            ParsedResume newParsedResume = ParsedResume.builder().resume(resume).build();
            populateParsedResume(newParsedResume, fullName, email, phone, linkedinUrl, githubUrl, portfolioUrl,
                    location, summary, skills, education, experience, projects, certifications,
                    languages, achievements, cleanedText, user);

            try {
                savedParsedResume = parsedResumePersistenceHelper.saveInNewTransaction(newParsedResume);
            } catch (DataIntegrityViolationException | ConstraintViolationException | JpaSystemException ex) {
                // Safely catch concurrent INSERT race condition, re-query existing row and update in place
                ParsedResume existingFromDb = parsedResumeRepository.findByResumeId(resumeId)
                        .orElseThrow(() -> ex);
                populateParsedResume(existingFromDb, fullName, email, phone, linkedinUrl, githubUrl, portfolioUrl,
                        location, summary, skills, education, experience, projects, certifications,
                        languages, achievements, cleanedText, user);
                savedParsedResume = parsedResumeRepository.saveAndFlush(existingFromDb);
            }
        }

        // Update Resume status consistently
        resume.setStatus("PARSED");
        resumeRepository.save(resume);

        return parsedResumeMapper.toDto(savedParsedResume);
    }

    @Override
    @Transactional
    public ParsedResume getOrParseEntity(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this resume.");
        }

        Optional<ParsedResume> existingOpt = parsedResumeRepository.findByResumeId(resumeId);
        if (existingOpt.isPresent()) {
            return existingOpt.get();
        }

        parseResume(resumeId, currentUserEmail);
        return parsedResumeRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Parsed resume not found after parsing for ID: " + resumeId));
    }

    @Override
    @Transactional(readOnly = true)
    public ParsedResumeDto getParsedResume(Long resumeId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to view this parsed resume.");
        }

        ParsedResume parsedResume = parsedResumeRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume has not been parsed yet. Click 'Parse Resume' to parse."));

        return parsedResumeMapper.toDto(parsedResume);
    }

    @Override
    @Transactional(readOnly = true)
    public String getRawText(Long resumeId, String currentUserEmail) {
        ParsedResumeDto dto = getParsedResume(resumeId, currentUserEmail);
        return dto.getRawText() != null ? dto.getRawText() : "";
    }

    private void populateParsedResume(ParsedResume parsedResume, String fullName, String email, String phone,
                                     String linkedinUrl, String githubUrl, String portfolioUrl, String location,
                                     String summary, String skills, String education, String experience,
                                     String projects, String certifications, String languages, String achievements,
                                     String cleanedText, User user) {
        parsedResume.setFullName(fullName != null ? fullName : user.getFullName());
        parsedResume.setEmail(email != null ? email : user.getEmail());
        parsedResume.setPhone(phone);
        parsedResume.setLinkedinUrl(linkedinUrl);
        parsedResume.setGithubUrl(githubUrl);
        parsedResume.setPortfolioUrl(portfolioUrl);
        parsedResume.setLocation(location);
        parsedResume.setSummary(summary);
        parsedResume.setSkills(skills);
        parsedResume.setEducation(education);
        parsedResume.setExperience(experience);
        parsedResume.setProjects(projects);
        parsedResume.setCertifications(certifications);
        parsedResume.setLanguages(languages);
        parsedResume.setAchievements(achievements);
        parsedResume.setRawText(cleanedText);
        parsedResume.setParsedAt(LocalDateTime.now());
    }
}
