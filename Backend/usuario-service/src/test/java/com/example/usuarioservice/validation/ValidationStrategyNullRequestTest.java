package com.example.usuarioservice.validation;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;

@DisplayName("ValidationContext - null request handling")
class ValidationStrategyNullRequestTest {

    @Test
    @DisplayName("null request is delegated to strategy")
    void nullRequest_isDelegated() {

        IValidationStrategy lenient = mock(IValidationStrategy.class);
        when(lenient.getStrategyName()).thenReturn("LENIENT");

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);

        ValidationContext context = new ValidationContext(strategies);

        context.validateForCreation(null, ValidationStrategyType.LENIENT);

        verify(lenient).validateForCreation(null);
    }
}