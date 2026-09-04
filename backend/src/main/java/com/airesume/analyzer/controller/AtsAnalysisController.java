package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.AnalysisReportDto;
import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.service.ats.AtsAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AtsAnalysisController {

    private final AtsAnalysisService atsAnalysisService;

    @PostMapping("/analyze/{resumeId}")
    public ResponseEntity<AnalysisReportDto> analyzeResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalysisReportDto report = atsAnalysisService.analyzeResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<AnalysisReportDto> getAnalysisByResumeId(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalysisReportDto report = atsAnalysisService.getAnalysisByResumeId(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisReportDto>> getAnalysisHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AnalysisReportDto> history = atsAnalysisService.getAnalysisHistory(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAnalysisReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        atsAnalysisService.deleteAnalysisReport(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("ATS Analysis Report deleted successfully")
                .success(true)
                .build());
    }
}
