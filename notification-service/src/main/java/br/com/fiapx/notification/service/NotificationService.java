package br.com.fiapx.notification.service;

import br.com.fiapx.common.event.VideoStatusEvent;
import br.com.fiapx.notification.config.NotificationProperties;
import br.com.fiapx.notification.domain.NotificationHistory;
import br.com.fiapx.notification.dto.NotificationResponseDTO;
import br.com.fiapx.notification.repository.NotificationHistoryRepository;
import br.com.fiapx.notification.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_ERROR = "ERROR";

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationEmailSender notificationEmailSender;

    public NotificationService(
            NotificationHistoryRepository notificationHistoryRepository,
            NotificationEmailSender notificationEmailSender) {
        this.notificationHistoryRepository = notificationHistoryRepository;
        this.notificationEmailSender = notificationEmailSender;
    }

    @Transactional
    public void process(VideoStatusEvent event) {
        String normalizedStatus = normalizeStatus(event.status());
        String subject = buildSubject(normalizedStatus);
        String message = buildMessage(normalizedStatus, event);

        NotificationHistory notificationHistory = new NotificationHistory();
        notificationHistory.setUserId(event.userId());
        notificationHistory.setUserEmail(event.userEmail());
        notificationHistory.setVideoId(event.videoId());
        notificationHistory.setStatus(normalizedStatus);
        notificationHistory.setSubject(subject);
        notificationHistory.setMessage(message);
        notificationHistory.setCreatedAt(event.updatedAt() != null ? event.updatedAt() : LocalDateTime.now());
        notificationHistoryRepository.save(notificationHistory);

        notificationEmailSender.send(event, normalizedStatus, subject, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listUserNotifications(AuthenticatedUser user) {
        return notificationHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.userId()).stream()
                .map(notification -> new NotificationResponseDTO(
                        notification.getId(),
                        notification.getVideoId(),
                        notification.getStatus(),
                        notification.getSubject(),
                        notification.getMessage(),
                        notification.getCreatedAt()
                ))
                .toList();
    }

    private String normalizeStatus(String status) {
        if (STATUS_FINISHED.equalsIgnoreCase(status)) {
            return STATUS_FINISHED;
        }
        return STATUS_ERROR;
    }

    private String buildSubject(String status) {
        if (STATUS_FINISHED.equals(status)) {
            return "Video processing finished";
        }
        return "Video processing failed";
    }

    private String buildMessage(String status, VideoStatusEvent event) {
        if (STATUS_FINISHED.equals(status)) {
            return "Your video has been processed successfully.";
        }

        if (event.errorMessage() != null && !event.errorMessage().isBlank()) {
            return event.errorMessage();
        }

        return "Your video processing failed.";
    }
}
