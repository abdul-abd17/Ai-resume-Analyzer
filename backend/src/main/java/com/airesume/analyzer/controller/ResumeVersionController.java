package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.dto.ResumeVersionDto;
import com.airesume.analyzer.service.version.ResumeVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class ResumeVersionController {

    private final ResumeVersionService resumeVersionService;

    @GetMapping
    public ResponseEntity<List<ResumeVersionDto>> getVersionsForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeVersionDto> list = resumeVersionService.getVersionsForCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeVersionDto> getVersionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto dto = resumeVersionService.getVersionById(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<ResumeVersionDto> renameVersion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String newName = body.getOrDefault("versionName", "Renamed Version");
        ResumeVersionDto dto = resumeVersionService.renameVersion(id, newName, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ResumeVersionDto> restoreVersion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeVersionDto dto = resumeVersionService.restoreVersion(id, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteVersion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeVersionService.deleteVersion(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Resume version deleted successfully")
                .success(true)
                .build());
    }
}
