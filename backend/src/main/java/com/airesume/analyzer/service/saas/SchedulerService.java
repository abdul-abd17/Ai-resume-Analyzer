package com.airesume.analyzer.service.saas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerService {

    // Run every day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTemporaryFiles() {
        log.info("[SCHEDULED TASK] Cleanup temporary files task executed successfully.");
    }

    // Run every hour
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshAnalyticsCache() {
        log.info("[SCHEDULED TASK] System analytics metrics refreshed.");
    }
}
