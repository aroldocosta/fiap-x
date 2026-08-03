package br.com.fiapx.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoProcessingEvent(
        UUID videoId,
        UUID userId,
        String filePath,
        LocalDateTime createdAt
) {
}
