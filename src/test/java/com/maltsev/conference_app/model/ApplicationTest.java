package com.maltsev.conference_app.model;

import com.maltsev.conference_app.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    private Participant participant;
    private Direction direction;

    @BeforeEach
    void setUp() {
        participant = TestData.participant(1L);
        direction = TestData.openDirection(10L);
    }

    private Application newApplication() {
        return new Application(participant, direction, "Title", "Abstract", "Content");
    }

    /** Небольшая пауза, чтобы время "до" и "после" гарантированно различались. */
    private static void pause() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- конструктор ----------

    @Test
    void constructor_setsParticipantAndDirection() {
        Application application = newApplication();

        assertSame(participant, application.getParticipant());
        assertSame(direction, application.getDirection());
    }

    @Test
    void constructor_setsTitleAbstractAndContent() {
        Application application = newApplication();

        assertEquals("Title", application.getTitle());
        assertEquals("Abstract", application.getAbstractText());
        assertEquals("Content", application.getContent());
    }

    @Test
    void constructor_setsStatusSubmitted() {
        assertEquals(ApplicationStatus.SUBMITTED, newApplication().getStatus());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(newApplication().getId());
    }

    @Test
    void constructor_setsCreatedAtToCurrentMoment() {
        OffsetDateTime before = OffsetDateTime.now();
        Application application = newApplication();
        OffsetDateTime after = OffsetDateTime.now();

        assertNotNull(application.getCreatedAt());
        assertFalse(application.getCreatedAt().isBefore(before));
        assertFalse(application.getCreatedAt().isAfter(after));
    }

    @Test
    void constructor_setsUpdatedAtNotEarlierThanCreatedAt() {
        Application application = newApplication();

        assertNotNull(application.getUpdatedAt());
        assertFalse(application.getUpdatedAt().isBefore(application.getCreatedAt()));
    }

    // ---------- edit ----------

    @Test
    void edit_changesTitleAbstractAndContent() {
        Application application = newApplication();

        application.edit("New title", "New abstract", "New content");

        assertEquals("New title", application.getTitle());
        assertEquals("New abstract", application.getAbstractText());
        assertEquals("New content", application.getContent());
    }

    @Test
    void edit_doesNotChangeStatus() {
        Application application = newApplication();

        application.edit("New title", "New abstract", "New content");

        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
    }

    @Test
    void edit_doesNotChangeParticipantAndDirection() {
        Application application = newApplication();

        application.edit("New title", "New abstract", "New content");

        assertSame(participant, application.getParticipant());
        assertSame(direction, application.getDirection());
    }

    @Test
    void edit_doesNotChangeCreatedAt() {
        Application application = newApplication();
        OffsetDateTime createdAt = application.getCreatedAt();
        pause();

        application.edit("New title", "New abstract", "New content");

        assertEquals(createdAt, application.getCreatedAt());
    }

    @Test
    void edit_movesUpdatedAtForward() {
        Application application = newApplication();
        OffsetDateTime updatedBefore = application.getUpdatedAt();
        pause();

        application.edit("New title", "New abstract", "New content");

        assertTrue(application.getUpdatedAt().isAfter(updatedBefore));
    }

    @Test
    void edit_withSameValues_keepsValues() {
        Application application = newApplication();

        application.edit("Title", "Abstract", "Content");

        assertEquals("Title", application.getTitle());
        assertEquals("Abstract", application.getAbstractText());
        assertEquals("Content", application.getContent());
    }

    @Test
    void edit_canBeCalledSeveralTimes_lastValuesWin() {
        Application application = newApplication();

        application.edit("First", "First", "First");
        application.edit("Second", "Second", "Second");

        assertEquals("Second", application.getTitle());
        assertEquals("Second", application.getAbstractText());
        assertEquals("Second", application.getContent());
    }

    // ---------- withdraw ----------

    @Test
    void withdraw_setsStatusWithdrawn() {
        Application application = newApplication();

        application.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    }

    @Test
    void withdraw_doesNotChangeTexts() {
        Application application = newApplication();

        application.withdraw();

        assertEquals("Title", application.getTitle());
        assertEquals("Abstract", application.getAbstractText());
        assertEquals("Content", application.getContent());
    }

    @Test
    void withdraw_doesNotChangeParticipantAndDirection() {
        Application application = newApplication();

        application.withdraw();

        assertSame(participant, application.getParticipant());
        assertSame(direction, application.getDirection());
    }

    @Test
    void withdraw_doesNotChangeCreatedAt() {
        Application application = newApplication();
        OffsetDateTime createdAt = application.getCreatedAt();
        pause();

        application.withdraw();

        assertEquals(createdAt, application.getCreatedAt());
    }

    @Test
    void withdraw_movesUpdatedAtForward() {
        Application application = newApplication();
        OffsetDateTime updatedBefore = application.getUpdatedAt();
        pause();

        application.withdraw();

        assertTrue(application.getUpdatedAt().isAfter(updatedBefore));
    }

    @Test
    void withdraw_calledTwice_keepsStatusWithdrawn() {
        Application application = newApplication();

        application.withdraw();
        application.withdraw();

        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    }
}
