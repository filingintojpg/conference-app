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
    private Participant participant;

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

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Application(Participant participant, Direction direction, String title,
                        String abstractText, String content) {
        this.participant = participant;
        this.direction = direction;
        this.title = title;
        this.abstractText = abstractText;
        this.content = content;
        this.status = ApplicationStatus.SUBMITTED;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void edit(String title, String abstractText, String content) {
        this.title = title;
        this.abstractText = abstractText;
        this.content = content;
        this.updatedAt = OffsetDateTime.now();
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
        this.updatedAt = OffsetDateTime.now();
    }
}
