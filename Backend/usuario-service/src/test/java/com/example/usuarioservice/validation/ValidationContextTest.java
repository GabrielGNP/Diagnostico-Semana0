package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ValidationContext - strategy selection")
class ValidationContextTest {

    @Test
    @DisplayName("validateForCreation delegates to selected strategy")
    void validateForCreation_delegatesToStrategy() {
        IValidationStrategy lenient = mock(IValidationStrategy.class);
        when(lenient.getStrategyName()).thenReturn("LENIENT");

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);

        ValidationContext context = new ValidationContext(strategies);
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@test.com")
            .contrasena("Pass1234")
            .build();

        context.validateForCreation(request, ValidationStrategyType.LENIENT);

        verify(lenient).validateForCreation(request);
    }

    @Test
    @DisplayName("validateForUpdate delegates to selected strategy")
    void validateForUpdate_delegatesToStrategy() {
        IValidationStrategy strict = mock(IValidationStrategy.class);
        when(strict.getStrategyName()).thenReturn("STRICT");

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("STRICT", strict);

        ValidationContext context = new ValidationContext(strategies);
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Carlos")
            .build();

        context.validateForUpdate(request, ValidationStrategyType.STRICT);

        verify(strict).validateForUpdate(request);
    }

    @Test
    @DisplayName("getStrategy throws IllegalArgumentException when missing")
    void getStrategy_throwsWhenMissing() {
        Map<String, IValidationStrategy> strategies = new HashMap<>();
        ValidationContext context = new ValidationContext(strategies);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> context.validateForCreation(CreateUsuarioRequest.builder().build(), ValidationStrategyType.LENIENT));

        assertTrue(ex.getMessage().contains("Estrategia de valid"));
    }

    @Test
    @DisplayName("getAvailableStrategies returns immutable copy")
    void getAvailableStrategies_returnsCopy() {
        IValidationStrategy lenient = mock(IValidationStrategy.class);
        when(lenient.getStrategyName()).thenReturn("LENIENT");

        Map<String, IValidationStrategy> strategies = new HashMap<>();
        strategies.put("LENIENT", lenient);

        ValidationContext context = new ValidationContext(strategies);
        Map<String, IValidationStrategy> available = context.getAvailableStrategies();

        assertEquals(1, available.size());
        assertTrue(available.containsKey("LENIENT"));
        assertThrows(UnsupportedOperationException.class, () -> available.put("STRICT", lenient));
    }
}

