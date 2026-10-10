package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParticipantTest {

    @Test
    void constructor_setsFullName() {
        assertEquals("Ivan Ivanov", new Participant("Ivan Ivanov", "ivan@example.com").getFullName());
    }

    @Test
    void constructor_setsEmail() {
        assertEquals("ivan@example.com", new Participant("Ivan Ivanov", "ivan@example.com").getEmail());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(new Participant("Ivan Ivanov", "ivan@example.com").getId());
    }
}
