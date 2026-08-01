package br.com.fiapx.common.event;

import java.util.UUID;

public record VideoNotificationEvent(
        UUID videoId,
        UUID userId,
        String status,
        String message
) {
}
