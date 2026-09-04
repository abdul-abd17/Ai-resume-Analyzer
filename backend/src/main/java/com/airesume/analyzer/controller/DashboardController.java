package com.airesume.analyzer.controller;

import com.airesume.analyzer.dto.DashboardDto;
import com.airesume.analyzer.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboardData(
            @AuthenticationPrincipal UserDetails userDetails) {
        DashboardDto dto = dashboardService.getAggregatedDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardDto.QuickStats> getQuickStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        DashboardDto.QuickStats stats = dashboardService.getQuickStats(userDetails.getUsername());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/charts")
    public ResponseEntity<Map<String, Object>> getChartsData(
            @AuthenticationPrincipal UserDetails userDetails) {
        DashboardDto dto = dashboardService.getAggregatedDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(dto.getChartsData());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<DashboardDto.RecentActivityItem>> getRecentActivity(
            @AuthenticationPrincipal UserDetails userDetails) {
        DashboardDto dto = dashboardService.getAggregatedDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(dto.getRecentActivity());
    }
}
