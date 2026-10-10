package com.maltsev.conference_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Базы данных в модульных тестах нет, поэтому проверяем только то, что можно увидеть
 * в аннотациях: имена таблиц и колонок должны совпадать с миграцией V1.
 */
class EntityMappingTest {

    @Test
    void entities_areMarkedAsJpaEntities() {
        assertNotNull(Application.class.getAnnotation(Entity.class));
        assertNotNull(Participant.class.getAnnotation(Entity.class));
        assertNotNull(Direction.class.getAnnotation(Entity.class));
    }

    @Test
    void entities_useTableNamesFromMigration() {
        assertEquals("applications", Application.class.getAnnotation(Table.class).name());
        assertEquals("participants", Participant.class.getAnnotation(Table.class).name());
        assertEquals("directions", Direction.class.getAnnotation(Table.class).name());
    }

    @Test
    void applicationStatus_isStoredAsString() throws NoSuchFieldException {
        Enumerated enumerated = Application.class.getDeclaredField("status").getAnnotation(Enumerated.class);

        assertNotNull(enumerated);
        assertEquals(EnumType.STRING, enumerated.value());
    }

    @Test
    void applicationTitle_columnLengthIs300() throws NoSuchFieldException {
        Column column = Application.class.getDeclaredField("title").getAnnotation(Column.class);

        assertEquals(300, column.length());
    }

    @Test
    void directionDeadline_usesColumnDeadline() throws NoSuchFieldException {
        Column column = Direction.class.getDeclaredField("deadline").getAnnotation(Column.class);

        assertEquals("deadline", column.name());
    }
}
