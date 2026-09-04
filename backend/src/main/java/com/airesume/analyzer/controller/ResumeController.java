package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.dto.PagedResumeResponse;
import com.airesume.analyzer.dto.ResumeDto;
import com.airesume.analyzer.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeDto> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeDto uploadedResume = resumeService.uploadResume(file, userDetails.getUsername());
        return new ResponseEntity<>(uploadedResume, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResumeResponse> getUserResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        PagedResumeResponse resumes = resumeService.getUserResumes(
                userDetails.getUsername(), page, size, search, sortBy, sortDir
        );
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResumeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeDto resume = resumeService.getResumeById(id, userDetails.getUsername());
        return ResponseEntity.ok(resume);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Resume deleted successfully")
                .success(true)
                .build());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeDto resumeDto = resumeService.getResumeById(id, userDetails.getUsername());
        Resource resource = resumeService.downloadResume(id, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resumeDto.getFileType() != null ? resumeDto.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resumeDto.getOriginalFileName() + "\"")
                .body(resource);
    }
}
