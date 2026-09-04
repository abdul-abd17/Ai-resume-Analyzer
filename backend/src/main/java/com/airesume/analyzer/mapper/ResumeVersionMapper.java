package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.ResumeVersionDto;
import com.airesume.analyzer.entity.ResumeVersion;
import org.springframework.stereotype.Component;

@Component
public class ResumeVersionMapper {

    public ResumeVersionDto toDto(ResumeVersion entity) {
        if (entity == null) return null;

        return ResumeVersionDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .versionNumber(entity.getVersionNumber())
                .versionName(entity.getVersionName())
                .fileName(entity.getFileName())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .changeSummary(entity.getChangeSummary())
                .atsScore(entity.getAtsScore())
                .grammarScore(entity.getGrammarScore())
                .jobMatchScore(entity.getJobMatchScore())
                .status(entity.getStatus())
                .isCurrent(entity.getIsCurrent())
                .build();
    }
}
