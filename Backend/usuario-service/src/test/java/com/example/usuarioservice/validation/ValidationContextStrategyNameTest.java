package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ValidationContext - strategy names")
class ValidationContextStrategyNameTest {

    @Test
    @DisplayName("strategy name is used in logging")
    void strategyName_isAvailable() {
        IValidationStrategy strategy = mock(IValidationStrategy.class);
        when(strategy.getStrategyName()).thenReturn("LENIENT");
        assertEquals("LENIENT", strategy.getStrategyName());
    }
}

