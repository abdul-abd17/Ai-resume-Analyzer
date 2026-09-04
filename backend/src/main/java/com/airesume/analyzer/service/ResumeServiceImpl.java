package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.PagedResumeResponse;
import com.airesume.analyzer.dto.ResumeDto;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.FileStorageException;
import com.airesume.analyzer.exception.InvalidFileTypeException;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.ResumeMapper;
import com.airesume.analyzer.repository.ReportRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Path UPLOAD_DIR = Paths.get("uploads/resumes").toAbsolutePath().normalize();

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ResumeMapper resumeMapper;
    private final com.airesume.analyzer.service.version.ResumeVersionService resumeVersionService;
    private final com.airesume.analyzer.security.FileSecurityValidator fileSecurityValidator;

    @Override
    @Transactional
    public ResumeDto uploadResume(MultipartFile file, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Consolidate validation via authoritative FileSecurityValidator (Magic Bytes, MIME, Ext, Zip Bomb, Readability)
        fileSecurityValidator.validateFile(file);

        try {
            Files.createDirectories(UPLOAD_DIR);

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = getFileExtension(originalFilename);
            String safeBaseName = originalFilename.replaceAll("[^a-zA-Z0-9_.-]", "_");
            String uniqueFileName = UUID.randomUUID() + "_" + safeBaseName;

            Path targetLocation = fileSecurityValidator.resolveSafePath(UPLOAD_DIR, uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Resume resume = Resume.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(originalFilename)
                    .fileType(file.getContentType() != null ? file.getContentType() : extension)
                    .fileSize(file.getSize())
                    .storagePath(targetLocation.toString())
                    .uploadedAt(LocalDateTime.now())
                    .status("UPLOADED")
                    .user(user)
                    .build();

            Resume savedResume = resumeRepository.save(resume);
            
            try {
                resumeVersionService.createNextVersion(savedResume, currentUserEmail);
            } catch (Exception e) {
                // Log and continue gracefully
            }

            return resumeMapper.toDto(savedResume);

        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file. Please try again!", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResumeResponse getUserResumes(String currentUserEmail, int page, int size, String search, String sortBy, String sortDir) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Resume> resumePage;
        if (StringUtils.hasText(search)) {
            resumePage = resumeRepository.findByUserIdAndOriginalFileNameContainingIgnoreCase(user.getId(), search.trim(), pageable);
        } else {
            resumePage = resumeRepository.findByUserId(user.getId(), pageable);
        }

        List<ResumeDto> dtos = resumePage.getContent().stream()
                .map(resumeMapper::toDto)
                .collect(Collectors.toList());

        return PagedResumeResponse.builder()
                .content(dtos)
                .pageNumber(resumePage.getNumber())
                .pageSize(resumePage.getSize())
                .totalElements(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .last(resumePage.isLast())
                .first(resumePage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDto getResumeById(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this resume.");
        }

        return resumeMapper.toDto(resume);
    }

    @Override
    @Transactional
    public void deleteResume(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to delete this resume.");
        }

        // Delete associated report PDF files from disk
        try {
            List<com.airesume.analyzer.entity.Report> reports = reportRepository.findByResumeIdOrderByGeneratedDateDesc(id);
            for (com.airesume.analyzer.entity.Report report : reports) {
                if (report.getFilePath() != null) {
                    Files.deleteIfExists(Paths.get(report.getFilePath()));
                }
            }
        } catch (Exception ignored) {}

        // Delete from storage
        try {
            Path filePath = Paths.get(resume.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log warning but proceed to delete database record
        }

        resumeRepository.delete(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadResume(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to download this resume.");
        }

        try {
            Path filePath = fileSecurityValidator.resolveSafePath(UPLOAD_DIR, resume.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or unreadable: " + resume.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File path is invalid: " + resume.getOriginalFileName(), ex);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex + 1);
    }
}
