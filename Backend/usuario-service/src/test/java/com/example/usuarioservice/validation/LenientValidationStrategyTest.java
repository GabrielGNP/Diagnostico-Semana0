package com.example.usuarioservice.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.CreateUsuarioRequest;

@DisplayName("LenientValidationStrategy - validation rules")
class LenientValidationStrategyTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("validateForCreation accepts valid data")
    void validateForCreation_acceptsValidData() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("Juan Perez")
                .email("juan@test.com")
                .contrasena("Pass1234")
                .build();

        assertDoesNotThrow(() -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects blank name")
    void validateForCreation_rejectsBlankName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre(" ")
                .email("juan@test.com")
                .contrasena("Pass1234")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects short name")
    void validateForCreation_rejectsShortName() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("A")
                .email("juan@test.com")
                .contrasena("Pass1234")
                .build();

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> strategy.validateForCreation(request)
        );

        assertTrue(ex.getMessage().contains("al menos"));
    }

    @Test
    @DisplayName("validateForCreation rejects short password")
    void validateForCreation_rejectsShortPassword() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("Juan")
                .email("juan@test.com")
                .contrasena("Pa1")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects password without uppercase")
    void validateForCreation_rejectsPasswordWithoutUppercase() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("Juan")
                .email("juan@test.com")
                .contrasena("pass1234")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects password without lowercase")
    void validateForCreation_rejectsPasswordWithoutLowercase() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("Juan")
                .email("juan@test.com")
                .contrasena("PASS1234")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForCreation rejects password without digit")
    void validateForCreation_rejectsPasswordWithoutDigit() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
                .nombre("Juan")
                .email("juan@test.com")
                .contrasena("Password")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }

    @Test
    @DisplayName("validateForUpdate validates only provided fields")
    void validateForUpdate_validatesOnlyProvidedFields() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
                .nombre("Juan")
                .contrasena("Pass1234")
                .build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }

    @Test
    @DisplayName("validateForUpdate rejects invalid password")
    void validateForUpdate_rejectsInvalidPassword() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
                .contrasena("short")
                .build();

        assertThrows(ValidationException.class, () -> strategy.validateForUpdate(request));
    }

    @Test
    @DisplayName("strategy name is LENIENT")
    void strategyName_isLenient() {
        assertEquals("LENIENT", strategy.getStrategyName());
    }
}