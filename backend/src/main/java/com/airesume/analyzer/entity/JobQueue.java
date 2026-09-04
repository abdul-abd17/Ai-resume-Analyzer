package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_queues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_type", nullable = false)
    private String jobType; // RESUME_PARSING, ATS_ANALYSIS, GRAMMAR_ANALYSIS, AI_RECOMMENDATION, PDF_GENERATION

    @Column(name = "job_status", nullable = false)
    private String jobStatus; // QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "resume_id")
    private Long resumeId;

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        if (this.jobStatus == null) {
            this.jobStatus = "QUEUED";
        }
    }
}
