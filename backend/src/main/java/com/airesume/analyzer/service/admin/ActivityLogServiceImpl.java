package com.airesume.analyzer.service.admin;

import com.airesume.analyzer.entity.ActivityLog;
import com.airesume.analyzer.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    @Transactional
    public void logActivity(Long userId, String userEmail, String action, String description, String ipAddress) {
        ActivityLog log = ActivityLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .description(description)
                .timestamp(LocalDateTime.now())
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .build();
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }
}
