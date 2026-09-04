package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.JobDescriptionDto;
import com.airesume.analyzer.dto.MatchAnalysisDto;
import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.service.job.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/job-description")
@RequiredArgsConstructor
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;

    @PostMapping("/upload")
    public ResponseEntity<JobDescriptionDto> uploadJobDescription(
            @RequestPart(value = "data", required = false) JobDescriptionDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "description", required = false) String descriptionText,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (dto == null) {
            dto = JobDescriptionDto.builder()
                    .title(title)
                    .companyName(companyName)
                    .description(descriptionText)
                    .build();
        }

        JobDescriptionDto createdJd = jobDescriptionService.uploadJobDescription(dto, file, userDetails.getUsername());
        return ResponseEntity.ok(createdJd);
    }

    @PostMapping("/compare/{resumeId}")
    public ResponseEntity<MatchAnalysisDto> compareResume(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        MatchAnalysisDto matchAnalysis = jobDescriptionService.compareResume(resumeId, jobDescriptionId, userDetails.getUsername());
        return ResponseEntity.ok(matchAnalysis);
    }

    @GetMapping
    public ResponseEntity<List<JobDescriptionDto>> getJobDescriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobDescriptionDto> list = jobDescriptionService.getJobDescriptionsByUser(userDetails.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescriptionDto> getJobDescriptionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobDescriptionDto dto = jobDescriptionService.getJobDescriptionById(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/match-analysis/{resumeId}")
    public ResponseEntity<MatchAnalysisDto> getMatchAnalysis(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        MatchAnalysisDto dto = jobDescriptionService.getMatchAnalysisByResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteJobDescription(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobDescriptionService.deleteJobDescription(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Job Description deleted successfully")
                .success(true)
                .build());
    }
}
