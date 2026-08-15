package br.com.fiapx.videoapi.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email) {
}
