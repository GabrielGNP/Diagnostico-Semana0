package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - whitespace rules")
class LenientValidationStrategyWhitespaceTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("accepts name with leading/trailing spaces")
    void acceptsNameWithSpaces() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre(" Juan ")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }
}

