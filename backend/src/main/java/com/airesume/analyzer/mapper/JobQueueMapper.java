package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.JobQueueDto;
import com.airesume.analyzer.entity.JobQueue;
import org.springframework.stereotype.Component;

@Component
public class JobQueueMapper {

    public JobQueueDto toDto(JobQueue entity, String resumeFileName) {
        if (entity == null) return null;

        return JobQueueDto.builder()
                .id(entity.getId())
                .jobType(entity.getJobType())
                .jobStatus(entity.getJobStatus())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .errorMessage(entity.getErrorMessage())
                .createdBy(entity.getCreatedBy())
                .resumeId(entity.getResumeId())
                .resumeFileName(resumeFileName != null ? resumeFileName : "resume.pdf")
                .build();
    }
}
