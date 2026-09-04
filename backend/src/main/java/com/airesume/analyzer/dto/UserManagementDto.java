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
public class UserManagementDto {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime joinedDate;
    private String status; // ACTIVE, SUSPENDED
    private Integer atsAverage;
    private int resumeCount;
}
