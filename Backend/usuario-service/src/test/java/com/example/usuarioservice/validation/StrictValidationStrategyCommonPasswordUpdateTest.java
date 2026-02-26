package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - update common password")
class StrictValidationStrategyCommonPasswordUpdateTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("update rejects common password")
    void updateRejectsCommonPassword() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .contrasena("Password123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForUpdate(request));
    }
}

