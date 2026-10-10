package com.maltsev.conference_app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CreateApplicationRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private static Set<ConstraintViolation<CreateApplicationRequest>> validate(
            Long directionId, String title, String abstractText, String content) {
        return validator.validate(new CreateApplicationRequest(directionId, title, abstractText, content));
    }

    private static void assertSingleViolation(Set<? extends ConstraintViolation<?>> violations,
                                              String property, String message) {
        assertEquals(1, violations.size());
        ConstraintViolation<?> violation = violations.iterator().next();
        assertEquals(property, violation.getPropertyPath().toString());
        assertEquals(message, violation.getMessage());
    }

    // ---------- корректные данные ----------

    @Test
    void validRequest_hasNoViolations() {
        assertTrue(validate(10L, "Spring Boot", "Abstract", "Content").isEmpty());
    }

    @Test
    void title_ofOneCharacter_isValid() {
        assertTrue(validate(10L, "a", "Abstract", "Content").isEmpty());
    }

    @Test
    void title_ofExactly300Characters_isValid() {
        assertTrue(validate(10L, "a".repeat(300), "Abstract", "Content").isEmpty());
    }

    @Test
    void russianText_isValid() {
        assertTrue(validate(10L, "Нейросеть для распознавания дефектов", "Аннотация", "Текст").isEmpty());
    }

    @Test
    void abstractAndContent_haveNoLengthLimit() {
        assertTrue(validate(10L, "Title", "a".repeat(100_000), "a".repeat(100_000)).isEmpty());
    }

    // ---------- directionId ----------

    @Test
    void directionId_null_isRejected() {
        assertSingleViolation(validate(null, "Title", "Abstract", "Content"),
                "directionId", "directionId is required");
    }

    // ---------- title ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void title_nullEmptyOrBlank_isRejected(String title) {
        assertSingleViolation(validate(10L, title, "Abstract", "Content"),
                "title", "title must not be blank");
    }

    @Test
    void title_of301Characters_isRejected() {
        assertSingleViolation(validate(10L, "a".repeat(301), "Abstract", "Content"),
                "title", "title must be at most 300 characters");
    }

    // ---------- abstractText ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void abstractText_nullEmptyOrBlank_isRejected(String abstractText) {
        assertSingleViolation(validate(10L, "Title", abstractText, "Content"),
                "abstractText", "abstractText must not be blank");
    }

    // ---------- content ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void content_nullEmptyOrBlank_isRejected(String content) {
        assertSingleViolation(validate(10L, "Title", "Abstract", content),
                "content", "content must not be blank");
    }

    // ---------- несколько ошибок сразу ----------

    @Test
    void allFieldsNull_reportsAllFourFields() {
        Set<ConstraintViolation<CreateApplicationRequest>> violations = validate(null, null, null, null);

        Set<String> properties = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(4, violations.size());
        assertEquals(Set.of("directionId", "title", "abstractText", "content"), properties);
    }

    @Test
    void twoInvalidFields_reportOnlyThoseTwo() {
        Set<ConstraintViolation<CreateApplicationRequest>> violations = validate(10L, "", "Abstract", " ");

        Set<String> properties = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(2, violations.size());
        assertEquals(Set.of("title", "content"), properties);
    }
}
