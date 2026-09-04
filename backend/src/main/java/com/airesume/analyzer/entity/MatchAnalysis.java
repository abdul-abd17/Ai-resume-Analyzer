package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id", nullable = false)
    @JsonIgnore
    private JobDescription jobDescription;

    @Column(name = "overall_match_percentage", nullable = false)
    private Integer overallMatchPercentage;

    @Column(name = "skill_match_percentage", nullable = false)
    private Integer skillMatchPercentage;

    @Column(name = "experience_match_percentage", nullable = false)
    private Integer experienceMatchPercentage;

    @Column(name = "education_match_percentage", nullable = false)
    private Integer educationMatchPercentage;

    @Column(name = "project_match_percentage", nullable = false)
    private Integer projectMatchPercentage;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "extra_skills", columnDefinition = "TEXT")
    private String extraSkills;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    @Column(name = "priority_recommendations", columnDefinition = "TEXT")
    private String priorityRecommendations;

    @Column(name = "hiring_recommendation")
    private String hiringRecommendation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
