package br.com.fiapx.videoapi.dto;

import br.com.fiapx.videoapi.domain.VideoStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoResponseDTO(
        UUID id,
        String title,
        VideoStatus status,
        String zipStoragePath,
        LocalDateTime createdAt
) {
}
