package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void constructor_setsFullName() {
        assertEquals("Ivan Ivanov", new User("Ivan Ivanov", "ivan@example.com", Role.PARTICIPANT).getFullName());
    }

    @Test
    void constructor_setsEmail() {
        assertEquals("ivan@example.com", new User("Ivan Ivanov", "ivan@example.com", Role.PARTICIPANT).getEmail());
    }

    @Test
    void constructor_setsRole() {
        assertEquals(Role.ORGANIZER, new User("Ivan Ivanov", "ivan@example.com", Role.ORGANIZER).getRole());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(new User("Ivan Ivanov", "ivan@example.com", Role.PARTICIPANT).getId());
    }
}
