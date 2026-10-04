package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.ApplicationStatus;
import java.time.OffsetDateTime;

public record ApplicationResponse(
        Long id,
        Long participantId,
        Long directionId,
        String title,
        String abstractText,
        String content,
        ApplicationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
