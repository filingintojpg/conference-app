package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.Application;

import java.time.OffsetDateTime;

/** Заявка для члена экспертной комиссии: без данных об авторе. */
public record ExpertApplicationResponse(
        Long id,
        Long directionId,
        String directionName,
        String title,
        String abstractText,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ExpertApplicationResponse from(Application application) {
        return new ExpertApplicationResponse(
                application.getId(),
                application.getDirection().getId(),
                application.getDirection().getName(),
                application.getTitle(),
                application.getAbstractText(),
                application.getContent(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
