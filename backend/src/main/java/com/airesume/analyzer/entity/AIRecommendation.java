package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "professional_summary", columnDefinition = "TEXT")
    private String professionalSummary;

    @Column(name = "improved_experience", columnDefinition = "TEXT")
    private String improvedExperience;

    @Column(name = "improved_projects", columnDefinition = "TEXT")
    private String improvedProjects;

    @Column(name = "improved_skills", columnDefinition = "TEXT")
    private String improvedSkills;

    @Column(name = "keyword_recommendations", columnDefinition = "TEXT")
    private String keywordRecommendations;

    @Column(name = "ats_recommendations", columnDefinition = "TEXT")
    private String atsRecommendations;

    @Column(name = "grammar_recommendations", columnDefinition = "TEXT")
    private String grammarRecommendations;

    @Column(name = "interview_preparation", columnDefinition = "TEXT")
    private String interviewPreparation;

    @Column(name = "career_suggestions", columnDefinition = "TEXT")
    private String careerSuggestions;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
