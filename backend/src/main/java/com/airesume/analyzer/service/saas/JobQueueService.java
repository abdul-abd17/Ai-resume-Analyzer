package com.airesume.analyzer.service.saas;

import com.airesume.analyzer.dto.JobQueueDto;

import java.util.List;

public interface JobQueueService {

    JobQueueDto enqueueJob(String jobType, Long resumeId, String userEmail);

    List<JobQueueDto> getUserJobs(String userEmail);

    JobQueueDto getJobById(Long id, String userEmail);

    JobQueueDto retryJob(Long id, String userEmail);

    JobQueueDto cancelJob(Long id, String userEmail);
}
