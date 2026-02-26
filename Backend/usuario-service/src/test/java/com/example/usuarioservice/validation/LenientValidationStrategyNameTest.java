package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - name validation")
class LenientValidationStrategyNameTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("accepts trimmed name with spaces")
    void acceptsTrimmedName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("  Juan  ")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("rejects blank name")
    void rejectsBlankName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre(" ")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

