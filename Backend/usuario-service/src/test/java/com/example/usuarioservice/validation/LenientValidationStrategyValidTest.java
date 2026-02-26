package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - valid scenarios")
class LenientValidationStrategyValidTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("valid password with special char passes")
    void validPasswordWithSpecialChar_passes() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@test.com")
            .contrasena("Pass1234!")
            .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }
}

