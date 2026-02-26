package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ValidationContext - available strategies")
class ValidationContextAvailableStrategiesTest {

    @Test
    @DisplayName("available strategies returns copy")
    void availableStrategies_returnsCopy() {
        IValidationStrategy lenient = mock(IValidationStrategy.class);
        IValidationStrategy strict = mock(IValidationStrategy.class);

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);
        strategies.put("STRICT", strict);

        ValidationContext context = new ValidationContext(strategies);
        Map<String, IValidationStrategy> available = context.getAvailableStrategies();

        assertEquals(2, available.size());
        assertThrows(UnsupportedOperationException.class, () -> available.put("X", lenient));
    }
}

