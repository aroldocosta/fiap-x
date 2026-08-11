package br.com.fiapx.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        UUID videoId,
        String status,
        String subject,
        String message,
        LocalDateTime createdAt
) {
}
