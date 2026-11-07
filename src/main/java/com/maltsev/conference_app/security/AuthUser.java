package com.maltsev.conference_app.security;

import com.maltsev.conference_app.exception.UnauthorizedException;

import java.security.Principal;

public final class AuthUser {
    private AuthUser() {}

    public static Long id(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid token");
        }
    }
}
