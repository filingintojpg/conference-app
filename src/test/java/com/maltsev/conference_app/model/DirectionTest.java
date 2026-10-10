package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DirectionTest {

    @Test
    void constructor_setsName() {
        Direction direction = new Direction("Programming", OffsetDateTime.now());

        assertEquals("Programming", direction.getName());
    }

    @Test
    void constructor_setsDeadline() {
        OffsetDateTime deadline = OffsetDateTime.parse("2026-03-31T23:59:00+03:00");

        Direction direction = new Direction("Programming", deadline);

        assertEquals(deadline, direction.getDeadline());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(new Direction("Programming", OffsetDateTime.now()).getId());
    }
}
