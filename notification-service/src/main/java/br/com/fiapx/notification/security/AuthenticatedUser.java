package br.com.fiapx.notification.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email) {
}
