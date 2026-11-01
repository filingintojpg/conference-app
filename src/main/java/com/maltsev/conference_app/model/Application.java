package com.maltsev.conference_app.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String abstractText;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    private ReviewStatus reviewStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_from_direction_id")
    private Direction transferredFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_to_direction_id")
    private Direction transferTo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Application(User participant, Direction direction, String title,
                        String abstractText, String content) {
        this.participant = participant;
        this.direction = direction;
        this.title = title;
        this.abstractText = abstractText;
        this.content = content;
        this.status = ApplicationStatus.SUBMITTED;
        this.reviewStatus = ReviewStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void edit(String title, String abstractText, String content) {
        this.title = title;
        this.abstractText = abstractText;
        this.content = content;
        this.status = ApplicationStatus.EDITED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void approve() {
        this.reviewStatus = ReviewStatus.APPROVED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void reject() {
        this.reviewStatus = ReviewStatus.REJECTED;
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isReviewed() {
        return reviewStatus != ReviewStatus.PENDING;
    }

    public boolean isTransferPending() {
        return transferTo != null;
    }

    public void requestTransfer(Direction target) {
        this.transferredFrom = this.direction;
        this.transferTo = target;
        this.updatedAt = OffsetDateTime.now();
    }

    public void acceptTransfer() {
        this.direction = this.transferTo;
        this.transferTo = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void rejectTransfer() {
        this.transferTo = null;
        this.transferredFrom = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.updatedAt = OffsetDateTime.now();
    }
}
