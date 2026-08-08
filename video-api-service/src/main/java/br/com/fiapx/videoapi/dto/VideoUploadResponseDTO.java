package br.com.fiapx.videoapi.dto;

import br.com.fiapx.videoapi.domain.VideoStatus;

import java.util.UUID;

public record VideoUploadResponseDTO(
        UUID id,
        String title,
        VideoStatus status,
        String message
) {
}
