package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "parsed_resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    @JsonIgnore
    private Resume resume;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "location")
    private String location;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "education", columnDefinition = "TEXT")
    private String education;

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "projects", columnDefinition = "TEXT")
    private String projects;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "parsed_at", nullable = false)
    private LocalDateTime parsedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (this.parsedAt == null) {
            this.parsedAt = LocalDateTime.now();
        }
    }

    public List<String> getSkillsList() {
        if (skills == null || skills.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(skills.split("[,;\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
