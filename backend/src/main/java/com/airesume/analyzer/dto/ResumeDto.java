package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDto {

    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String formattedFileSize;
    private LocalDateTime uploadedAt;
    private String status;
    private Long userId;
    private String ownerName;
    private String ownerEmail;
}
