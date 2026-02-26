package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - password validation")
class LenientValidationStrategyPasswordTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("rejects password without digit")
    void rejectsPasswordWithoutDigit() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@test.com")
            .contrasena("Password")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("rejects password without uppercase")
    void rejectsPasswordWithoutUppercase() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@test.com")
            .contrasena("password1")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

