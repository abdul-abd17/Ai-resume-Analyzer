package com.airesume.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompareVersionsRequest {

    @NotNull(message = "Old version ID is required")
    private Long oldVersionId;

    @NotNull(message = "New version ID is required")
    private Long newVersionId;
}
