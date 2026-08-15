package br.com.fiapx.videoapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Test
    void shouldValidateTokenAndExtractAuthenticatedUser() {
        JwtTokenService service = new JwtTokenService(new JwtProperties(SECRET));
        byte[] secret = hexToBytes(SECRET);
        String token = Jwts.builder()
                .subject("user")
                .claim("userId", "6c6a814e-0fea-4483-9f99-0f6a347f2e7a")
                .claim("email", "user@fiapx.com")
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();

        assertThat(service.validateToken(token)).isTrue();
        assertThat(service.getAuthenticatedUser(token)).isEqualTo(
                new AuthenticatedUser(UUID.fromString("6c6a814e-0fea-4483-9f99-0f6a347f2e7a"), "user@fiapx.com")
        );
    }

    private byte[] hexToBytes(String value) {
        if (value.matches("^[0-9a-fA-F]+$") && value.length() % 2 == 0) {
            byte[] bytes = new byte[value.length() / 2];
            for (int i = 0; i < value.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
            }
            return bytes;
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
