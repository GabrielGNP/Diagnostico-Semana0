package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationContext - empty strategies")
class ValidationContextEmptyStrategiesTest {

    @Test
    @DisplayName("empty strategies throws when validating")
    void emptyStrategies_throwsWhenValidating() {
        Map<String, IValidationStrategy> strategies = new HashMap<>();
        ValidationContext context = new ValidationContext(strategies);

        assertThrows(IllegalArgumentException.class, () ->
            context.validateForUpdate(UpdateUsuarioRequest.builder().build(), ValidationStrategyType.LENIENT));
    }
}
