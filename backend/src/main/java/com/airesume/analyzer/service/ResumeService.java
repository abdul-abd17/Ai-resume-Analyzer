package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.PagedResumeResponse;
import com.airesume.analyzer.dto.ResumeDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeDto uploadResume(MultipartFile file, String currentUserEmail);

    PagedResumeResponse getUserResumes(String currentUserEmail, int page, int size, String search, String sortBy, String sortDir);

    ResumeDto getResumeById(Long id, String currentUserEmail);

    void deleteResume(Long id, String currentUserEmail);

    Resource downloadResume(Long id, String currentUserEmail);
}
