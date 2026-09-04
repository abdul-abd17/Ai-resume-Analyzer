package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.ReportDto;
import com.airesume.analyzer.entity.Report;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportDto toDto(Report entity) {
        if (entity == null) return null;

        return ReportDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .resumeFileName(entity.getResume() != null ? entity.getResume().getOriginalFileName() : null)
                .reportName(entity.getReportName())
                .reportType(entity.getReportType())
                .generatedBy(entity.getGeneratedBy())
                .generatedDate(entity.getGeneratedDate())
                .fileSize(entity.getFileSize())
                .status(entity.getStatus())
                .downloadCount(entity.getDownloadCount())
                .sharedCount(entity.getSharedCount())
                .atsScore(null)
                .grammarScore(null)
                .jobMatchScore(null)
                .build();
    }
}
