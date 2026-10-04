package com.maltsev.conference_app.dto;

public record UpdateApplicationRequest(
        String title,
        String abstractText,
        String content
) {}
