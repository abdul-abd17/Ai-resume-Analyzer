package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.JobQueueDto;
import com.airesume.analyzer.service.saas.JobQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobQueueController {

    private final JobQueueService jobQueueService;

    @GetMapping
    public ResponseEntity<List<JobQueueDto>> getUserJobs(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JobQueueDto> jobs = jobQueueService.getUserJobs(userDetails.getUsername());
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobQueueDto> getJobById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobQueueDto dto = jobQueueService.getJobById(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<JobQueueDto> retryJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobQueueDto dto = jobQueueService.retryJob(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobQueueDto> cancelJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        JobQueueDto dto = jobQueueService.cancelJob(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }
}
