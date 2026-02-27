package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LenientValidationStrategy - update nulls")
class LenientValidationStrategyUpdateNullsTest {

    @Test
    @DisplayName("validateForUpdate accepts all null fields")
    void validateForUpdate_acceptsAllNullFields() {
        LenientValidationStrategy strategy = new LenientValidationStrategy();
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder().build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }
}

