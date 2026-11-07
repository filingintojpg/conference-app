package com.maltsev.conference_app.dto;

public record AuthResult(
        String accessToken,
        long expiresIn,
        UserResponse user
) {}
