package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - name validation")
class StrictValidationStrategyNameTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("rejects name with leading or trailing spaces")
    void rejectsNameWithSpaces() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre(" Carlos ")
            .email("carlos@test.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("rejects short name")
    void rejectsShortName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Al")
            .email("carlos@test.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

