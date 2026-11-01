package com.maltsev.conference_app;

import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

/**
 * Готовые объекты для тестов.
 * <p>
 * Настоящие id выдаёт база данных, а в модульных тестах базы нет,
 * поэтому id выставляем вручную через ReflectionTestUtils.
 */
public final class TestData {
    public static final String TITLE = "Spring Boot";
    public static final String ABSTRACT_TEXT = "Application abstract";
    public static final String CONTENT = "Application content";

    private TestData() {}

    public static Application application(Long id, User participant, Direction direction) {
        Application application = new Application(participant, direction, TITLE, ABSTRACT_TEXT, CONTENT);
        ReflectionTestUtils.setField(application, "id", id);
        return application;
    }

    public static User participant(Long id) {
        User participant = new User("Ivanov", "Ivan", "Ivanovich", "ivan", "example.com", Role.PARTICIPANT);
        ReflectionTestUtils.setField(participant, "id", id);
        return participant;
    }

    public static User organizer(Long id) {
        User organizer = new User("Sidorova", "Maria", "Alexandrovna", "maria", "example.com", Role.ORGANIZER);
        ReflectionTestUtils.setField(organizer, "id", id);
        return organizer;
    }

    public static User expert(Long id) {
        User expert = new User("Kuznetsov", "Alexey", "Viktorovich", "alexey", "example.com", Role.EXPERT);
        ReflectionTestUtils.setField(expert, "id", id);
        return expert;
    }

    public static Direction direction(Long id, OffsetDateTime deadline) {
        return direction(id, deadline, organizer(100L));
    }

    /** Направление с заданным организатором. */
    public static Direction direction(Long id, OffsetDateTime deadline, User organizer) {
        return direction(id, deadline, organizer, null);
    }

    /** Направление с заданными организатором и экспертом. */
    public static Direction direction(Long id, OffsetDateTime deadline, User organizer, User expert) {
        Direction direction = new Direction("Programming", deadline, organizer, expert);
        ReflectionTestUtils.setField(direction, "id", id);
        return direction;
    }

    /** Направление, в котором дедлайн наступит только через 30 дней. */
    public static Direction openDirection(Long id) {
        return direction(id, OffsetDateTime.now().plusDays(30));
    }

    /** Направление, в котором дедлайн прошёл вчера. */
    public static Direction closedDirection(Long id) {
        return direction(id, OffsetDateTime.now().minusDays(1));
    }
}
