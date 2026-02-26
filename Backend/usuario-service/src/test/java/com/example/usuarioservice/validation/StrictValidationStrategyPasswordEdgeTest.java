package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrictValidationStrategy - password edge cases")
class StrictValidationStrategyPasswordEdgeTest {

    private final StrictValidationStrategy strategy = new StrictValidationStrategy();

    @Test
    @DisplayName("rejects password missing lowercase")
    void rejectsPasswordMissingLowercase() {
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos")
            .email("carlos@test.com")
            .contrasena("STRONG123!")
            .build();

        assertThrows(ValidationException.class, () -> strategy.validateForCreation(request));
    }
}

