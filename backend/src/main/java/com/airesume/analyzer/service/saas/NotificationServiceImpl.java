package com.airesume.analyzer.service.saas;

import com.airesume.analyzer.dto.NotificationDto;
import com.airesume.analyzer.entity.Notification;
import com.airesume.analyzer.exception.ResourceNotFoundException;
import com.airesume.analyzer.exception.UnauthorizedAccessException;
import com.airesume.analyzer.mapper.NotificationMapper;
import com.airesume.analyzer.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public NotificationDto createAndSendNotification(Long userId, String title, String message, String category, String priority) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .category(category != null ? category : "SYSTEM")
                .priority(priority != null ? priority : "MEDIUM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = notificationMapper.toDto(saved);

        // Real-time STOMP WebSocket broadcast
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
            } catch (Exception e) {
                log.warn("WebSocket broadcast failed: {}", e.getMessage());
            }
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(notificationMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!n.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized notification access");
        }

        n.setIsRead(true);
        Notification saved = notificationRepository.save(n);
        return notificationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!n.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized notification access");
        }

        notificationRepository.delete(n);
    }

    @Override
    @Transactional
    public void clearAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
