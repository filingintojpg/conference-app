package com.maltsev.conference_app.model;

import com.maltsev.conference_app.TestData;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DirectionTest {
    private static final User ORGANIZER = TestData.organizer(1L);

    @Test
    void constructor_setsName() {
        Direction direction = new Direction("Programming", OffsetDateTime.now(), ORGANIZER);

        assertEquals("Programming", direction.getName());
    }

    @Test
    void constructor_setsDeadline() {
        OffsetDateTime deadline = OffsetDateTime.parse("2026-03-31T23:59:00+03:00");

        Direction direction = new Direction("Programming", deadline, ORGANIZER);

        assertEquals(deadline, direction.getDeadline());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(new Direction("Programming", OffsetDateTime.now(), ORGANIZER).getId());
    }
}
