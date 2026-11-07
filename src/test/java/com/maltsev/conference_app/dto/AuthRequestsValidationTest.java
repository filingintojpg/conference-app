package com.maltsev.conference_app.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRequestsValidationTest {
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

    private static RegisterRequest register(String email, String password) {
        return new RegisterRequest("Ivanov", "Ivan", null, email, password);
    }

    @Test
    void register_validRequest_hasNoViolations() {
        assertTrue(validator.validate(register("ivan@example.com", "secret-pass")).isEmpty());
    }

    @Test
    void register_invalidEmail_hasViolation() {
        assertFalse(validator.validate(register("nope", "secret-pass")).isEmpty());
    }

    @Test
    void register_shortPassword_hasViolation() {
        assertFalse(validator.validate(register("ivan@example.com", "short")).isEmpty());
    }

    @Test
    void register_passwordLongerThan72Chars_hasViolation() {
        assertFalse(validator.validate(register("ivan@example.com", "a".repeat(73))).isEmpty());
    }

    @Test
    void register_blankLastName_hasViolation() {
        RegisterRequest request = new RegisterRequest(" ", "Ivan", null, "ivan@example.com", "secret-pass");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void login_validRequest_hasNoViolations() {
        assertTrue(validator.validate(new LoginRequest("ivan@example.com", "secret-pass")).isEmpty());
    }

    @Test
    void login_blankFields_haveTwoViolations() {
        assertTrue(validator.validate(new LoginRequest("", "")).size() == 2);
    }
}
