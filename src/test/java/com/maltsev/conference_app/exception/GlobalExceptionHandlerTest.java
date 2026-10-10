package com.maltsev.conference_app.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Обработчик вызываем напрямую, как обычный метод. Тест лежит в том же пакете,
 * потому что методы обработчика не public.
 */
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @SuppressWarnings("unused")
    private void methodWithOneParameter(String parameter) {
    }

    private MethodArgumentNotValidException validationException(BeanPropertyBindingResult bindingResult)
            throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("methodWithOneParameter", String.class), 0);
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    private HttpStatus statusOf(String methodName, Class<?> exceptionType) throws NoSuchMethodException {
        ResponseStatus annotation = GlobalExceptionHandler.class
                .getDeclaredMethod(methodName, exceptionType)
                .getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        return annotation.value();
    }

    // ---------- тексты ответов ----------

    @Test
    void notFound_returnsMessageOfException() {
        assertEquals(new ErrorResponse("Application not found: 5"),
                handler.notFound(new NotFoundException("Application not found: 5")));
    }

    @Test
    void conflict_returnsMessageOfException() {
        assertEquals(new ErrorResponse("T1 has passed"),
                handler.conflict(new ConflictException("T1 has passed")));
    }

    @Test
    void forbidden_returnsMessageOfException() {
        assertEquals(new ErrorResponse("Application belongs to another participant"),
                handler.forbidden(new ForbiddenException("Application belongs to another participant")));
    }

    @Test
    void validation_returnsErrorTextAndMessageOfEachInvalidField() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "title", "title must not be blank"));
        bindingResult.addError(new FieldError("request", "content", "content must not be blank"));

        ValidationErrorResponse response = handler.validation(validationException(bindingResult));

        assertEquals("Validation failed", response.error());
        assertEquals(2, response.fields().size());
        assertEquals("title must not be blank", response.fields().get("title"));
        assertEquals("content must not be blank", response.fields().get("content"));
    }

    @Test
    void validation_severalMessagesForOneField_keepsOnlyTheFirst() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "title", "first message"));
        bindingResult.addError(new FieldError("request", "title", "second message"));

        ValidationErrorResponse response = handler.validation(validationException(bindingResult));

        assertEquals(1, response.fields().size());
        assertEquals("first message", response.fields().get("title"));
    }

    @Test
    void validation_noFieldErrors_returnsEmptyFields() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        ValidationErrorResponse response = handler.validation(validationException(bindingResult));

        assertEquals("Validation failed", response.error());
        assertTrue(response.fields().isEmpty());
    }

    // ---------- HTTP-статусы ----------

    @Test
    void notFound_hasStatus404() throws Exception {
        assertEquals(HttpStatus.NOT_FOUND, statusOf("notFound", NotFoundException.class));
    }

    @Test
    void conflict_hasStatus409() throws Exception {
        assertEquals(HttpStatus.CONFLICT, statusOf("conflict", ConflictException.class));
    }

    @Test
    void forbidden_hasStatus403() throws Exception {
        assertEquals(HttpStatus.FORBIDDEN, statusOf("forbidden", ForbiddenException.class));
    }

    @Test
    void validation_hasStatus400() throws Exception {
        assertEquals(HttpStatus.BAD_REQUEST, statusOf("validation", MethodArgumentNotValidException.class));
    }
}
