package com.maltsev.conference_app.dto;

import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.ApplicationStatus;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.ReviewStatus;
import java.time.OffsetDateTime;

public record ApplicationResponse(
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
        ReviewStatus reviewStatus,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long transferredFromDirectionId,
        String transferredFromDirectionName,
        Long transferToDirectionId,
        String transferToDirectionName
) {
    public ApplicationResponse(Long id, Long directionId, String directionName,
                               Long participantId, String participantFullName, String participantEmail,
                               String title, String abstractText, String content,
                               ApplicationStatus status, ReviewStatus reviewStatus,
                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this(id, directionId, directionName, participantId, participantFullName, participantEmail,
                title, abstractText, content, status, reviewStatus, createdAt, updatedAt,
                null, null, null, null);
    }

    public static ApplicationResponse from(Application application) {
        Direction from = application.getTransferredFrom();
        Direction to = application.getTransferTo();
        return new ApplicationResponse(
                application.getId(),
                application.getDirection().getId(),
                application.getDirection().getName(),
                application.getParticipant().getId(),
                application.getParticipant().getFullName(),
                application.getParticipant().getEmail(),
                application.getTitle(),
                application.getAbstractText(),
                application.getContent(),
                application.getStatus(),
                application.getReviewStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                from == null ? null : from.getId(),
                from == null ? null : from.getName(),
                to == null ? null : to.getId(),
                to == null ? null : to.getName()
        );
    }
}
