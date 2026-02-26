package com.example.usuarioservice.validation;

import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationStrategyType - enum values")
class ValidationStrategyTypeTest {

    @Test
    @DisplayName("enum has LENIENT and STRICT")
    void enum_hasExpectedValues() {
        assertNotNull(ValidationStrategyType.LENIENT);
        assertNotNull(ValidationStrategyType.STRICT);
        assertEquals("LENIENT", ValidationStrategyType.LENIENT.name());
        assertEquals("STRICT", ValidationStrategyType.STRICT.name());
    }
}

