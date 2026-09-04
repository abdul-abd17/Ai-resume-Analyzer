package com.airesume.analyzer.service.report;

import com.airesume.analyzer.dto.ReportDto;
import com.airesume.analyzer.dto.ReportPreviewDto;

import java.util.List;

public interface ReportService {

    ReportDto generateReport(Long resumeId, String currentUserEmail);

    List<ReportDto> getReportsForCurrentUser(String currentUserEmail);

    ReportDto getReportById(Long id, String currentUserEmail);

    ReportPreviewDto getReportPreview(Long id, String currentUserEmail);

    byte[] downloadReportPdf(Long id, String currentUserEmail);

    void deleteReport(Long id, String currentUserEmail);
}
