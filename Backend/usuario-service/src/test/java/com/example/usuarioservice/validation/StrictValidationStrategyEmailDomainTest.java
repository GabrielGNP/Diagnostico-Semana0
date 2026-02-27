package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - email domain rules")
class StrictValidationStrategyEmailDomainTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("rejects temp email domain")
    void rejectsTempEmailDomain() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@tempmail.com")
            .contrasena("StrongPass123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

