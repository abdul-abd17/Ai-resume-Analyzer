package com.airesume.analyzer.service.ats;

import com.airesume.analyzer.dto.AnalysisReportDto;

import java.util.List;

public interface AtsAnalysisService {

    AnalysisReportDto analyzeResume(Long resumeId, String currentUserEmail);

    AnalysisReportDto getAnalysisByResumeId(Long resumeId, String currentUserEmail);

    List<AnalysisReportDto> getAnalysisHistory(String currentUserEmail);

    void deleteAnalysisReport(Long id, String currentUserEmail);
}
