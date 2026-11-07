package com.maltsev.conference_app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
public class AuthCookies {
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final boolean secure;
    private final String sameSite;

    public AuthCookies(@Value("${app.cookie.secure:false}") boolean secure,
                       @Value("${app.cookie.same-site:Strict}") String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void write(HttpServletResponse response, String token, long maxAgeSeconds) {
        add(response, build(token, Duration.ofSeconds(maxAgeSeconds)));
    }

    public void clear(HttpServletResponse response) {
        add(response, build("", Duration.ZERO));
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private static void add(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
