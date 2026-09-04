package com.airesume.analyzer.service.saas;

import com.airesume.analyzer.dto.NotificationDto;

import java.util.List;

public interface NotificationService {

    NotificationDto createAndSendNotification(Long userId, String title, String message, String category, String priority);

    List<NotificationDto> getUserNotifications(Long userId);

    NotificationDto markAsRead(Long notificationId, Long userId);

    void deleteNotification(Long notificationId, Long userId);

    void clearAllNotifications(Long userId);
}
