package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - valid scenarios")
class StrictValidationStrategyValidTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("valid strict password passes")
    void validStrictPassword_passes() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("VeryStrong123!")
            .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }
}

