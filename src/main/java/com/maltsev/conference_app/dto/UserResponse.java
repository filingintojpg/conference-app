package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.model.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
