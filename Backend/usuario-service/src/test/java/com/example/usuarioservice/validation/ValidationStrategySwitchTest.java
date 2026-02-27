package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@DisplayName("ValidationContext - strategy switching")
class ValidationStrategySwitchTest {

    @Test
    @DisplayName("switches between lenient and strict")
    void switchesBetweenStrategies() {
        IValidationStrategy lenient = mock(IValidationStrategy.class);
        when(lenient.getStrategyName()).thenReturn("LENIENT");

        IValidationStrategy strict = mock(IValidationStrategy.class);
        when(strict.getStrategyName()).thenReturn("STRICT");

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);
        strategies.put("STRICT", strict);

        ValidationContext context = new ValidationContext(strategies);
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        context.validateForCreation(request, ValidationStrategyType.LENIENT);
        context.validateForCreation(request, ValidationStrategyType.STRICT);

        verify(lenient).validateForCreation(request);
        verify(strict).validateForCreation(request);
    }
}

