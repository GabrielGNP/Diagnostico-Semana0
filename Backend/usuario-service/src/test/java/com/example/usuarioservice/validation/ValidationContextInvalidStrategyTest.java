package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationContext - invalid strategy handling")
class ValidationContextInvalidStrategyTest {

    @Test
    @DisplayName("missing strategy throws IllegalArgumentException with available list")
    void missingStrategy_throwsWithAvailableList() {
        Map<String, IValidationStrategy> strategies = new HashMap<>();
        ValidationContext context = new ValidationContext(strategies);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> context.validateForCreation(CreateUsuarioRequest.builder().build(), ValidationStrategyType.STRICT));

        assertTrue(ex.getMessage().contains("Disponibles"));
    }
}

