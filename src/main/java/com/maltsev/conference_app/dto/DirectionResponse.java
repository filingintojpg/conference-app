package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.Direction;

import java.time.OffsetDateTime;

public record DirectionResponse(
        Long id,
        String name,
        OffsetDateTime deadline,
        Long organizerId,
        String organizerFullName
) {
    public static DirectionResponse from(Direction direction) {
        return new DirectionResponse(
                direction.getId(),
                direction.getName(),
                direction.getDeadline(),
                direction.getOrganizer().getId(),
                direction.getOrganizer().getFullName()
        );
    }
}
