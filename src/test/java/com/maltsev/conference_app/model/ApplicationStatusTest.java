package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Эти строки записываются в БД и проверяются ограничением applications_status_chk
 * в миграции V1. Если добавите или переименуете статус, нужна новая миграция,
 * а этот тест напомнит об этом.
 */
class ApplicationStatusTest {

    @Test
    void hasExactlyThreeStatusesInDeclaredOrder() {
        assertArrayEquals(
                new ApplicationStatus[]{ApplicationStatus.SUBMITTED, ApplicationStatus.EDITED, ApplicationStatus.WITHDRAWN},
                ApplicationStatus.values());
    }

    @Test
    void edited_isStoredInDatabaseAsEDITED() {
        assertEquals("EDITED", ApplicationStatus.EDITED.name());
    }

    @Test
    void submitted_isStoredInDatabaseAsSUBMITTED() {
        assertEquals("SUBMITTED", ApplicationStatus.SUBMITTED.name());
    }

    @Test
    void withdrawn_isStoredInDatabaseAsWITHDRAWN() {
        assertEquals("WITHDRAWN", ApplicationStatus.WITHDRAWN.name());
    }

    @Test
    void valueOf_findsStatusByName() {
        assertEquals(ApplicationStatus.SUBMITTED, ApplicationStatus.valueOf("SUBMITTED"));
        assertEquals(ApplicationStatus.EDITED, ApplicationStatus.valueOf("EDITED"));
        assertEquals(ApplicationStatus.WITHDRAWN, ApplicationStatus.valueOf("WITHDRAWN"));
    }

    @Test
    void valueOf_unknownName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ApplicationStatus.valueOf("APPROVED"));
    }
}
