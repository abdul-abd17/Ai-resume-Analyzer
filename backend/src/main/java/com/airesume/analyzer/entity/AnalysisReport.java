package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(name = "contact_score", nullable = false)
    private Integer contactScore;

    @Column(name = "summary_score", nullable = false)
    private Integer summaryScore;

    @Column(name = "skills_score", nullable = false)
    private Integer skillsScore;

    @Column(name = "experience_score", nullable = false)
    private Integer experienceScore;

    @Column(name = "education_score", nullable = false)
    private Integer educationScore;

    @Column(name = "projects_score", nullable = false)
    private Integer projectsScore;

    @Column(name = "certification_score", nullable = false)
    private Integer certificationScore;

    @Column(name = "keyword_coverage", nullable = false)
    private Integer keywordCoverage;

    @Column(name = "missing_keyword_count", nullable = false)
    private Integer missingKeywordCount;

    @Column(name = "duplicate_keyword_count", nullable = false)
    private Integer duplicateKeywordCount;

    @Column(name = "resume_strength", columnDefinition = "TEXT")
    private String resumeStrength;

    @Column(name = "resume_weakness", columnDefinition = "TEXT")
    private String resumeWeakness;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "top_skills", columnDefinition = "TEXT")
    private String topSkills;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    @Column(name = "duplicate_keywords", columnDefinition = "TEXT")
    private String duplicateKeywords;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    @PrePersist
    protected void onCreate() {
        if (this.analysisDate == null) {
            this.analysisDate = LocalDateTime.now();
        }
    }
}
