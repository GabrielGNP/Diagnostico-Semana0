package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationException - constructors")
class ValidationExceptionTest {

    @Test
    @DisplayName("constructor with message sets message")
    void constructor_withMessage_setsMessage() {
        ValidationException ex = new ValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    @DisplayName("constructor with message and cause sets both")
    void constructor_withMessageAndCause_setsBoth() {
        Throwable cause = new IllegalArgumentException("Cause");
        ValidationException ex = new ValidationException("Validation failed", cause);
        assertEquals("Validation failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

