package com.maltsev.conference_app.security;

import com.maltsev.conference_app.TestData;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Токен, выпущенный TokenService, должен читаться тем же декодером, что стоит в SecurityConfig. */
class TokenServiceTest {
    private static final String ISSUER = "https://conference-app";

    private static SecretKey key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static NimbusJwtDecoder decoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return decoder;
    }

    @Test
    void issuedToken_containsSubjectRoleAndIssuer() {
        SecretKey key = key("0123456789abcdef0123456789abcdef");
        TokenService service = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(key)), ISSUER, 60);

        String token = service.issue(TestData.organizer(5L));
        Jwt jwt = decoder(key).decode(token);

        assertEquals("5", jwt.getSubject());
        assertEquals("ORGANIZER", jwt.getClaimAsString(TokenService.ROLE_CLAIM));
        assertEquals(ISSUER, jwt.getIssuer().toString());
        assertEquals(3600L, service.expiresInSeconds());
    }

    @Test
    void tokenSignedWithAnotherKey_isRejected() {
        SecretKey signing = key("0123456789abcdef0123456789abcdef");
        SecretKey other = key("fedcba9876543210fedcba9876543210");
        TokenService service = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(signing)), ISSUER, 60);

        String token = service.issue(TestData.participant(1L));

        assertThrows(RuntimeException.class, () -> decoder(other).decode(token));
    }

    @Test
    void expiredToken_isRejected() {
        SecretKey key = key("0123456789abcdef0123456789abcdef");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("1")
                .issuedAt(now.minus(2, ChronoUnit.HOURS))
                .expiresAt(now.minus(1, ChronoUnit.HOURS))
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThrows(RuntimeException.class, () -> decoder(key).decode(token));
    }
}
