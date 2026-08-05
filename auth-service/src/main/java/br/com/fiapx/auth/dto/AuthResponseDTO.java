package br.com.fiapx.auth.dto;

public record AuthResponseDTO(
        String token,
        String type,
        Long expiresIn
) {
}
