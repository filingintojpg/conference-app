package com.maltsev.conference_app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Эти строки записываются в БД и проверяются ограничением applications_review_status_chk
 * в миграции V3. Если добавите или переименуете статус, нужна новая миграция.
 */
class ReviewStatusTest {

    @Test
    void hasExactlyThreeStatusesInDeclaredOrder() {
        assertArrayEquals(
                new ReviewStatus[]{ReviewStatus.PENDING, ReviewStatus.APPROVED, ReviewStatus.REJECTED},
                ReviewStatus.values());
    }
}
