package com.maltsev.conference_app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CookieJwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder decoder;
    private final Converter<Jwt, ? extends AbstractAuthenticationToken> converter;

    public CookieJwtAuthenticationFilter(JwtDecoder decoder,
                                         Converter<Jwt, ? extends AbstractAuthenticationToken> converter) {
        this.decoder = decoder;
        this.converter = converter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION) == null ? cookieToken(request) : null;
        if (token != null && !token.isBlank()) {
            try {
                AbstractAuthenticationToken authentication = converter.convert(decoder.decode(token));
                if (authentication != null) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private static String cookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (AuthCookies.ACCESS_TOKEN_COOKIE.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
