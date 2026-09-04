package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_comparisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_version_id", nullable = false)
    private ResumeVersion oldVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_version_id", nullable = false)
    private ResumeVersion newVersion;

    @Column(name = "ats_difference")
    private Integer atsDifference;

    @Column(name = "grammar_difference")
    private Integer grammarDifference;

    @Column(name = "job_match_difference")
    private Integer jobMatchDifference;

    @Column(name = "added_skills", columnDefinition = "TEXT")
    private String addedSkills;

    @Column(name = "removed_skills", columnDefinition = "TEXT")
    private String removedSkills;

    @Column(name = "added_projects", columnDefinition = "TEXT")
    private String addedProjects;

    @Column(name = "removed_projects", columnDefinition = "TEXT")
    private String removedProjects;

    @Column(name = "added_certifications", columnDefinition = "TEXT")
    private String addedCertifications;

    @Column(name = "removed_certifications", columnDefinition = "TEXT")
    private String removedCertifications;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "comparison_date", nullable = false)
    private LocalDateTime comparisonDate;

    @PrePersist
    protected void onCreate() {
        if (this.comparisonDate == null) {
            this.comparisonDate = LocalDateTime.now();
        }
    }
}
