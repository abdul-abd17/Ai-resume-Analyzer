package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.GrammarAnalysisDto;
import com.airesume.analyzer.dto.MessageResponse;
import com.airesume.analyzer.service.grammar.GrammarAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grammar")
@RequiredArgsConstructor
public class GrammarAnalysisController {

    private final GrammarAnalysisService grammarAnalysisService;

    @PostMapping("/analyze/{resumeId}")
    public ResponseEntity<GrammarAnalysisDto> analyzeGrammar(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        GrammarAnalysisDto dto = grammarAnalysisService.analyzeGrammar(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<GrammarAnalysisDto> getGrammarAnalysisByResumeId(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        GrammarAnalysisDto dto = grammarAnalysisService.getGrammarAnalysisByResumeId(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGrammarAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        grammarAnalysisService.deleteGrammarAnalysis(id, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Grammar analysis report deleted successfully")
                .success(true)
                .build());
    }
}
