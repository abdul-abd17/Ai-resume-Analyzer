package com.airesume.analyzer.service.saas;

import com.airesume.analyzer.dto.JobQueueDto;
import com.airesume.analyzer.entity.JobQueue;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.JobQueueMapper;
import com.airesume.analyzer.repository.JobQueueRepository;
import com.airesume.analyzer.repository.ResumeRepository;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobQueueServiceImpl implements JobQueueService {

    private final JobQueueRepository jobQueueRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobQueueMapper jobQueueMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public JobQueueDto enqueueJob(String jobType, Long resumeId, String userEmail) {
        JobQueue job = JobQueue.builder()
                .jobType(jobType)
                .jobStatus("QUEUED")
                .startedAt(LocalDateTime.now())
                .createdBy(userEmail)
                .resumeId(resumeId)
                .build();

        JobQueue saved = jobQueueRepository.save(job);

        // Execute job in background threadpool
        processJobAsynchronously(saved.getId(), userEmail);

        String fileName = getFileName(resumeId);
        return jobQueueMapper.toDto(saved, fileName);
    }

    @Async("saasTaskExecutor")
    public void processJobAsynchronously(Long jobId, String userEmail) {
        JobQueue job = jobQueueRepository.findById(jobId).orElse(null);
        if (job == null || "CANCELLED".equals(job.getJobStatus())) return;

        job.setJobStatus("RUNNING");
        jobQueueRepository.save(job);

        try {
            // Simulated async processing execution
            Thread.sleep(1500);

            job.setJobStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            jobQueueRepository.save(job);

            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                notificationService.createAndSendNotification(user.getId(),
                        job.getJobType() + " Completed",
                        "Background job #" + job.getId() + " finished processing successfully.",
                        "SYSTEM", "LOW");
            }

        } catch (Exception e) {
            job.setJobStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobQueueRepository.save(job);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobQueueDto> getUserJobs(String userEmail) {
        List<JobQueue> jobs = jobQueueRepository.findByCreatedByOrderByStartedAtDesc(userEmail);
        return jobs.stream()
                .map(j -> jobQueueMapper.toDto(j, getFileName(j.getResumeId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobQueueDto getJobById(Long id, String userEmail) {
        JobQueue job = jobQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        if (!job.getCreatedBy().equals(userEmail)) {
            throw new UnauthorizedAccessException("Unauthorized job access");
        }

        return jobQueueMapper.toDto(job, getFileName(job.getResumeId()));
    }

    @Override
    @Transactional
    public JobQueueDto retryJob(Long id, String userEmail) {
        JobQueue job = jobQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        if (!job.getCreatedBy().equals(userEmail)) {
            throw new UnauthorizedAccessException("Unauthorized job access");
        }

        job.setJobStatus("QUEUED");
        job.setErrorMessage(null);
        job.setStartedAt(LocalDateTime.now());
        job.setCompletedAt(null);

        JobQueue saved = jobQueueRepository.save(job);
        processJobAsynchronously(saved.getId(), userEmail);

        return jobQueueMapper.toDto(saved, getFileName(saved.getResumeId()));
    }

    @Override
    @Transactional
    public JobQueueDto cancelJob(Long id, String userEmail) {
        JobQueue job = jobQueueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        if (!job.getCreatedBy().equals(userEmail)) {
            throw new UnauthorizedAccessException("Unauthorized job access");
        }

        job.setJobStatus("CANCELLED");
        job.setCompletedAt(LocalDateTime.now());
        JobQueue saved = jobQueueRepository.save(job);

        return jobQueueMapper.toDto(saved, getFileName(saved.getResumeId()));
    }

    private String getFileName(Long resumeId) {
        if (resumeId == null) return "N/A";
        return resumeRepository.findById(resumeId)
                .map(Resume::getOriginalFileName)
                .orElse("resume.pdf");
    }
}
