package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - update rules")
class StrictValidationStrategyUpdateTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("update with valid email passes")
    void updateWithValidEmail_passes() {
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("valid@test.com")
            .build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }
}

