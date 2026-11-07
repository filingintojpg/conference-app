package com.maltsev.conference_app.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthCookiesTest {
    @Test
    void write_setsHttpOnlySameSiteCookieWithLifetime() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthCookies(false, "Strict").write(response, "jwt-token", 3600);

        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.startsWith("access_token=jwt-token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(cookie.contains("Path=/"));
        assertTrue(cookie.contains("Max-Age=3600"));
        assertFalse(cookie.contains("Secure"));
    }

    @Test
    void write_addsSecureFlagWhenEnabled() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthCookies(true, "Lax").write(response, "jwt-token", 60);

        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    void clear_expiresCookieImmediately() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthCookies(false, "Strict").clear(response);

        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.startsWith("access_token=;"));
        assertTrue(cookie.contains("Max-Age=0"));
    }
}
