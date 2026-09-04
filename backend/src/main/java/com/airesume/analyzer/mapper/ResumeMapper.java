package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.ResumeDto;
import com.airesume.analyzer.entity.Resume;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class ResumeMapper {

    public ResumeDto toDto(Resume resume) {
        if (resume == null) {
            return null;
        }

        return ResumeDto.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .originalFileName(resume.getOriginalFileName())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .formattedFileSize(formatFileSize(resume.getFileSize()))
                .uploadedAt(resume.getUploadedAt())
                .status(resume.getStatus())
                .userId(resume.getUser() != null ? resume.getUser().getId() : null)
                .ownerName(resume.getUser() != null ? resume.getUser().getFullName() : null)
                .ownerEmail(resume.getUser() != null ? resume.getUser().getEmail() : null)
                .build();
    }

    public String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null || sizeInBytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(sizeInBytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
