package com.maltsev.conference_app.security;

import com.maltsev.conference_app.TestData;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Фильтр должен превращать JWT из cookie в аутентификацию, а плохой токен - молча игнорировать. */
class CookieJwtAuthenticationFilterTest {
    private static final String ISSUER = "https://conference-app";
    private static final SecretKey KEY =
            new SecretKeySpec("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    private CookieJwtAuthenticationFilter filter;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));

        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        filter = new CookieJwtAuthenticationFilter(decoder, converter);
        tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(KEY)), ISSUER, 60);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(MockHttpServletRequest request) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        Authentication[] seen = new Authentication[1];
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            seen[0] = SecurityContextHolder.getContext().getAuthentication();
            chain.doFilter(req, res);
        });
        return seen[0];
    }

    @Test
    void validCookie_authenticatesWithSubjectAndRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookies.ACCESS_TOKEN_COOKIE, tokenService.issue(TestData.organizer(5L))));

        Authentication authentication = run(request);

        assertNotNull(authentication);
        assertEquals("5", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER")));
    }

    @Test
    void garbageCookie_leavesRequestUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookies.ACCESS_TOKEN_COOKIE, "not-a-jwt"));

        assertNull(run(request));
    }

    @Test
    void noCookie_leavesRequestUnauthenticated() throws Exception {
        assertNull(run(new MockHttpServletRequest()));
    }

    @Test
    void authorizationHeader_takesPrecedenceAndCookieIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer something");
        request.setCookies(new Cookie(AuthCookies.ACCESS_TOKEN_COOKIE, tokenService.issue(TestData.participant(1L))));

        assertNull(run(request));
    }
}
