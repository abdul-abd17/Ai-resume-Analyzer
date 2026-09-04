package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.JobQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobQueueRepository extends JpaRepository<JobQueue, Long> {

    List<JobQueue> findByCreatedByOrderByStartedAtDesc(String createdBy);

    List<JobQueue> findByJobStatus(String jobStatus);
}
