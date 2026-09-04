package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grammar_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "grammar_score", nullable = false)
    private Integer grammarScore;

    @Column(name = "readability_score", nullable = false)
    private Integer readabilityScore;

    @Column(name = "spelling_error_count", nullable = false)
    private Integer spellingErrorCount;

    @Column(name = "grammar_error_count", nullable = false)
    private Integer grammarErrorCount;

    @Column(name = "passive_voice_count", nullable = false)
    private Integer passiveVoiceCount;

    @Column(name = "repeated_word_count", nullable = false)
    private Integer repeatedWordCount;

    @Column(name = "long_sentence_count", nullable = false)
    private Integer longSentenceCount;

    @Column(name = "weak_verb_count", nullable = false)
    private Integer weakVerbCount;

    @Column(name = "formatting_issue_count", nullable = false)
    private Integer formattingIssueCount;

    @Column(name = "spelling_errors", columnDefinition = "TEXT")
    private String spellingErrors;

    @Column(name = "grammar_errors", columnDefinition = "TEXT")
    private String grammarErrors;

    @Column(name = "weak_verbs", columnDefinition = "TEXT")
    private String weakVerbs;

    @Column(name = "passive_voice_instances", columnDefinition = "TEXT")
    private String passiveVoiceInstances;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    @PrePersist
    protected void onCreate() {
        if (this.analysisDate == null) {
            this.analysisDate = LocalDateTime.now();
        }
    }
}
