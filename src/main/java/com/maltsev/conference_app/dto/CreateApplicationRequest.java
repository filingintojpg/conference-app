package com.maltsev.conference_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
        @NotNull(message = "directionId is required")
        Long directionId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 300, message = "title must be at most 300 characters")
        String title,

        @NotBlank(message = "abstractText must not be blank")
        String abstractText,

        @NotBlank(message = "content must not be blank")
        String content
) {}
