package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.CompareVersionsRequest;
import com.airesume.analyzer.dto.ResumeComparisonDto;
import com.airesume.analyzer.service.version.ComparisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    @PostMapping("/compare")
    public ResponseEntity<ResumeComparisonDto> compareVersions(
            @Valid @RequestBody CompareVersionsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeComparisonDto dto = comparisonService.compareVersions(request, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }
}
