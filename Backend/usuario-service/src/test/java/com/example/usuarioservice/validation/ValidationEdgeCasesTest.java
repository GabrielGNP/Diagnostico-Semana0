package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation strategies - edge cases")
class ValidationEdgeCasesTest {

    private final LenientValidationStrategy lenient = new LenientValidationStrategy();
    private final StrictValidationStrategy strict = new StrictValidationStrategy();

    @Test
    @DisplayName("lenient allows trimmed name")
    void lenient_allowsTrimmedName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("  Juan  ")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        assertDoesNotThrow(() -> lenient.validateForCreation(request));
    }

    @Test
    @DisplayName("strict rejects email with leading space")
    void strict_rejectsEmailWithLeadingSpace() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email(" carlos@test.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strict.validateForCreation(request));
    }

    @Test
    @DisplayName("strict rejects empty update when email invalid")
    void strict_rejectsInvalidEmailOnUpdate() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("bad-email")
            .build();

        assertThrows(ValidationException.class, () -> strict.validateForUpdate(request));
    }

    @Test
    @DisplayName("lenient update with nulls passes")
    void lenient_updateWithNulls_passes() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder().build();
        assertDoesNotThrow(() -> lenient.validateForUpdate(request));
    }
}

