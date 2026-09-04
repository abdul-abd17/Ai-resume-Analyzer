package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.CandidateSearchDto;
import com.airesume.analyzer.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final AdminService adminService;

    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateSearchDto>> searchCandidates(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minAtsScore,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CandidateSearchDto> list = adminService.searchCandidates(skill, minAtsScore, userDetails.getUsername());
        return ResponseEntity.ok(list);
    }
}
