package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - update rules")
class LenientValidationStrategyUpdateTest {

    private final LenientValidationStrategy strategy = new LenientValidationStrategy();

    @Test
    @DisplayName("update with valid name passes")
    void updateWithValidName_passes() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan")
            .build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }
}

