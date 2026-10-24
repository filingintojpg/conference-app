package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    private static User user(String middleName, Role role) {
        return new User("Ivanov", "Ivan", middleName, "ivan", "example.com", role);
    }

    @Test
    void constructor_setsNameParts() {
        User user = user("Ivanovich", Role.PARTICIPANT);

        assertEquals("Ivanov", user.getLastName());
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Ivanovich", user.getMiddleName());
    }

    @Test
    void constructor_setsEmailParts() {
        User user = user("Ivanovich", Role.PARTICIPANT);

        assertEquals("ivan", user.getEmailLocal());
        assertEquals("example.com", user.getEmailDomain());
    }

    @Test
    void getFullName_joinsLastFirstAndMiddleName() {
        assertEquals("Ivanov Ivan Ivanovich", user("Ivanovich", Role.PARTICIPANT).getFullName());
    }

    @Test
    void getFullName_withoutMiddleName_skipsIt() {
        assertEquals("Ivanov Ivan", user(null, Role.PARTICIPANT).getFullName());
    }

    @Test
    void getFullName_withBlankMiddleName_skipsIt() {
        assertEquals("Ivanov Ivan", user("  ", Role.PARTICIPANT).getFullName());
    }

    @Test
    void getEmail_joinsLocalAndDomainWithAt() {
        assertEquals("ivan@example.com", user("Ivanovich", Role.PARTICIPANT).getEmail());
    }

    @Test
    void constructor_setsRole() {
        assertEquals(Role.ORGANIZER, user("Ivanovich", Role.ORGANIZER).getRole());
    }

    @Test
    void constructor_leavesIdEmptyUntilSavedToDatabase() {
        assertNull(user("Ivanovich", Role.PARTICIPANT).getId());
    }
}
