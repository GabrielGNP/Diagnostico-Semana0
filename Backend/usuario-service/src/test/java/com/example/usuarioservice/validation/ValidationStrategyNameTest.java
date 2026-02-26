package com.example.usuarioservice.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation strategies - names")
class ValidationStrategyNameTest {

    @Test
    @DisplayName("lenient strategy name matches constant")
    void lenient_strategyName_matches() {
        LenientValidationStrategy strategy = new LenientValidationStrategy();
        assertEquals("LENIENT", strategy.getStrategyName());
    }

    @Test
    @DisplayName("strict strategy name matches constant")
    void strict_strategyName_matches() {
        StrictValidationStrategy strategy = new StrictValidationStrategy();
        assertEquals("STRICT", strategy.getStrategyName());
    }
}

