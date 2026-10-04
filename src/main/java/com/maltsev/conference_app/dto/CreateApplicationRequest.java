package com.maltsev.conference_app.dto;

public record CreateApplicationRequest(
        Long participantId,
        Long directionId,
        String title,
        String abstractText,
        String content
) {}
