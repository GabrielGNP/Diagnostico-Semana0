package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - password validation")
class StrictValidationStrategyPasswordTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("rejects password without uppercase")
    void rejectsPasswordWithoutUppercase() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("strongpass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("rejects password without digit")
    void rejectsPasswordWithoutDigit() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("StrongPass!!!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

