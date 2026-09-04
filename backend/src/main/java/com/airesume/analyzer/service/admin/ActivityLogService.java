package com.airesume.analyzer.service.admin;

import com.airesume.analyzer.entity.ActivityLog;

import java.util.List;

public interface ActivityLogService {

    void logActivity(Long userId, String userEmail, String action, String description, String ipAddress);

    List<ActivityLog> getAllLogs();
}
