package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.dto.ReportDto;
import com.airesume.analyzer.dto.ReportPreviewDto;
import com.airesume.analyzer.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate/{resumeId}")
    public ResponseEntity<ReportDto> generateReport(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReportDto dto = reportService.generateReport(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> getReportsForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReportDto> list = reportService.getReportsForCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportPreviewDto> getReportPreview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReportPreviewDto dto = reportService.getReportPreview(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadReportPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdfData = reportService.downloadReportPdf(id, userDetails.getUsername());
        ReportDto dto = reportService.getReportById(id, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getReportName().replaceAll("[^a-zA-Z0-9_.-]", "_") + ".pdf\"")
                .body(pdfData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        reportService.deleteReport(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Report deleted successfully")
                .success(true)
                .build());
    }
}
