package com.airesume.analyzer.service.version;

import com.airesume.analyzer.dto.CompareVersionsRequest;
import com.airesume.analyzer.dto.ResumeComparisonDto;

public interface ComparisonService {

    ResumeComparisonDto compareVersions(CompareVersionsRequest request, String currentUserEmail);

    ResumeComparisonDto getComparison(Long oldVersionId, Long newVersionId, String currentUserEmail);
}
