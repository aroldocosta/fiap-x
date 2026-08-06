package br.com.fiapx.auth.security;

import br.com.fiapx.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(parseSecret(jwtProperties.secret()));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.expiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return parse(token).getPayload();
    }

    public long getExpirationInSeconds() {
        return jwtProperties.expiration() / 1000;
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
