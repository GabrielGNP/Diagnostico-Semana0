package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - whitespace rules")
class StrictValidationStrategyWhitespaceTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("rejects email with whitespace")
    void rejectsEmailWithWhitespace() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@ test.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

