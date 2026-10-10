package com.maltsev.conference_app.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionClassesTest {

    @Test
    void notFoundException_keepsMessage() {
        assertEquals("not found", new NotFoundException("not found").getMessage());
    }

    @Test
    void conflictException_keepsMessage() {
        assertEquals("conflict", new ConflictException("conflict").getMessage());
    }

    @Test
    void forbiddenException_keepsMessage() {
        assertEquals("forbidden", new ForbiddenException("forbidden").getMessage());
    }

    @Test
    void customExceptions_areUncheckedExceptions() {
        assertInstanceOf(RuntimeException.class, new NotFoundException("x"));
        assertInstanceOf(RuntimeException.class, new ConflictException("x"));
        assertInstanceOf(RuntimeException.class, new ForbiddenException("x"));
    }

    @Test
    void errorResponse_holdsMessage() {
        assertEquals("something went wrong", new ErrorResponse("something went wrong").error());
    }

    @Test
    void errorResponse_isComparedByValue() {
        assertEquals(new ErrorResponse("a"), new ErrorResponse("a"));
        assertNotEquals(new ErrorResponse("a"), new ErrorResponse("b"));
    }

    @Test
    void validationErrorResponse_holdsErrorAndFields() {
        ValidationErrorResponse response = new ValidationErrorResponse(
                "Validation failed", Map.of("title", "title must not be blank"));

        assertEquals("Validation failed", response.error());
        assertEquals(Map.of("title", "title must not be blank"), response.fields());
    }
}
