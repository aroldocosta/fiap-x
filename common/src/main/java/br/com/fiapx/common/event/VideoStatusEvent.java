package br.com.fiapx.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoStatusEvent(
        UUID videoId,
        UUID userId,
        String userEmail,
        String status,
        String zipStoragePath,
        String errorMessage,
        LocalDateTime updatedAt
) {
}
