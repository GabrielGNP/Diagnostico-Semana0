package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ValidationContext - multiple strategies")
class ValidationStrategyMultipleStrategiesTest {

    @Test
    @DisplayName("available strategies include both keys")
    void availableStrategies_includeBothKeys() {
        IValidationStrategy lenient = mock(IValidationStrategy.class);
        IValidationStrategy strict = mock(IValidationStrategy.class);

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);
        strategies.put("STRICT", strict);

        ValidationContext context = new ValidationContext(strategies);
        Map<String, IValidationStrategy> available = context.getAvailableStrategies();

        assertEquals(2, available.size());
        assertTrue(available.containsKey("LENIENT"));
        assertTrue(available.containsKey("STRICT"));
    }
}

