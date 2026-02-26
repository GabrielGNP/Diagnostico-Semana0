package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - validation rules")
class StrictValidationStrategyTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("validateForCreation accepts valid strict data")
    void validateForCreation_acceptsValidStrictData() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("StrongPass123!")
            .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects name with leading space")
    void validateForCreation_rejectsNameWithSpaces() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre(" Carlos")
            .email("carlos@test.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects invalid email domain")
    void validateForCreation_rejectsInvalidEmailDomain() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@tempmail.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects short password")
    void validateForCreation_rejectsShortPassword() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("Short1!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects password without special char")
    void validateForCreation_rejectsPasswordWithoutSpecialChar() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("StrongPass123")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects common password")
    void validateForCreation_rejectsCommonPassword() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("Password123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForUpdate validates only provided fields")
    void validateForUpdate_validatesOnlyProvidedFields() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("valid@test.com")
            .build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }

    @Test
    @DisplayName("validateForUpdate rejects invalid email format")
    void validateForUpdate_rejectsInvalidEmail() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("invalid-email")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForUpdate(request));
    }

    @Test
    @DisplayName("validateForUpdate rejects password without uppercase")
    void validateForUpdate_rejectsPasswordWithoutUppercase() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .contrasena("weakpass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForUpdate(request));
    }

    @Test
    @DisplayName("strategy name is STRICT")
    void strategyName_isStrict() {
        assertEquals("STRICT", strategy.getStrategyName());
    }
}

