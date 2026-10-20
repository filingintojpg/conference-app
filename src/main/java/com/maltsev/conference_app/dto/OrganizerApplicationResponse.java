package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.ApplicationStatus;
import java.time.OffsetDateTime;

public record OrganizerApplicationResponse(
        Long id,
        Long directionId,
        String directionName,
        Long participantId,
        String participantFullName,
        String participantEmail,
        String title,
        String abstractText,
        String content,
        ApplicationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
