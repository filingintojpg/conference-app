package com.maltsev.conference_app;

import com.maltsev.conference_app.model.Application;
import com.maltsev.conference_app.model.Direction;
import com.maltsev.conference_app.model.Participant;
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

    public static Application application(Long id, Participant participant, Direction direction) {
        Application application = new Application(participant, direction, TITLE, ABSTRACT_TEXT, CONTENT);
        ReflectionTestUtils.setField(application, "id", id);
        return application;
    }

    public static Participant participant(Long id) {
        Participant participant = new Participant("Ivan Ivanov", "ivan@example.com");
        ReflectionTestUtils.setField(participant, "id", id);
        return participant;
    }

    public static Direction direction(Long id, OffsetDateTime deadline) {
        Direction direction = new Direction("Programming", deadline);
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
