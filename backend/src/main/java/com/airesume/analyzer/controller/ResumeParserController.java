package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.ParsedResumeDto;
import com.airesume.analyzer.service.parser.ResumeParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ResumeParserController {

    private final ResumeParserService resumeParserService;

    @PostMapping("/parse/{resumeId}")
    public ResponseEntity<ParsedResumeDto> parseResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ParsedResumeDto parsedResume = resumeParserService.parseResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(parsedResume);
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ParsedResumeDto> getParsedResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ParsedResumeDto parsedResume = resumeParserService.getParsedResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(parsedResume);
    }

    @GetMapping("/raw/{resumeId}")
    public ResponseEntity<Map<String, String>> getRawText(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String rawText = resumeParserService.getRawText(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(Collections.singletonMap("rawText", rawText));
    }
}
