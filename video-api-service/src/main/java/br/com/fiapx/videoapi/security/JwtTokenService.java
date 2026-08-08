package br.com.fiapx.videoapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(parseSecret(jwtProperties.secret()));
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        Claims claims = parse(token).getPayload();
        String userId = claims.get("userId", String.class);
        String email = claims.get("email", String.class);
        return new AuthenticatedUser(UUID.fromString(userId), email);
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    private byte[] parseSecret(String secret) {
        if (secret != null && secret.matches("^[0-9a-fA-F]+$") && secret.length() % 2 == 0) {
            byte[] bytes = new byte[secret.length() / 2];
            for (int i = 0; i < secret.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(secret.substring(i, i + 2), 16);
            }
            return bytes;
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
