package br.com.fiapx.auth.security;

import br.com.fiapx.auth.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @Test
    void shouldGenerateAndValidateToken() {
        JwtTokenService service = new JwtTokenService(new JwtProperties(SECRET, 7_200_000L));
        User user = new User();
        user.setName("User");
        user.setEmail("user@fiapx.com");
        setId(user, "6c6a814e-0fea-4483-9f99-0f6a347f2e7a");

        String token = service.generateToken(user);

        assertThat(service.validateToken(token)).isTrue();
        assertThat(service.getClaims(token).get("email", String.class)).isEqualTo("user@fiapx.com");
        assertThat(service.getClaims(token).get("userId", String.class)).isEqualTo("6c6a814e-0fea-4483-9f99-0f6a347f2e7a");
        assertThat(service.getExpirationInSeconds()).isEqualTo(7200L);
    }

    private void setId(User user, String id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, java.util.UUID.fromString(id));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
