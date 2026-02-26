package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - update nulls")
class StrictValidationStrategyUpdateNullsTest {

    @Test
    @DisplayName("validateForUpdate accepts all null fields")
    void validateForUpdate_acceptsAllNullFields() {
        StrictValidationStrategy strategy = new StrictValidationStrategy();
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder().build();

        assertDoesNotThrow(() -> strategy.validateForUpdate(request));
    }
}

