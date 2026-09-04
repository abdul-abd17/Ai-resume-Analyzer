package com.airesume.analyzer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "report_type", nullable = false)
    private String reportType; // PDF, HTML

    @Column(name = "generated_by")
    private String generatedBy;

    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "status", nullable = false)
    private String status; // GENERATED, PROCESSING

    @Column(name = "download_count")
    private Integer downloadCount;

    @Column(name = "shared_count")
    private Integer sharedCount;

    @PrePersist
    protected void onCreate() {
        if (this.generatedDate == null) {
            this.generatedDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "GENERATED";
        }
        if (this.downloadCount == null) {
            this.downloadCount = 0;
        }
        if (this.sharedCount == null) {
            this.sharedCount = 0;
        }
    }
}
