package com.airesume.analyzer.service.dashboard;

import com.airesume.analyzer.dto.DashboardDto;

public interface DashboardService {

    DashboardDto getAggregatedDashboardData(String currentUserEmail);

    DashboardDto.QuickStats getQuickStats(String currentUserEmail);
}
