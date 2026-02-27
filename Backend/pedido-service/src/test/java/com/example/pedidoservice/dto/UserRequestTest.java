package com.example.pedidoservice.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para UserRequest.
 * Cubre escenario DTO-03 del TEST_PLAN.md
 */
class UserRequestTest {

    // ========== DTO-03: Constructor, Getter y toString ==========

    @Test
    @DisplayName("DTO-03: Constructor con userId=25 debe retornar 25 en getUserId()")
    void constructor_withUserId25_shouldReturn25() {
        // Given
        int expectedUserId = 25;

        // When
        UserRequest request = new UserRequest(expectedUserId);

        // Then
        assertThat(request.getUserId()).isEqualTo(25);
    }

    @Test
    @DisplayName("DTO-03: Constructor con userId=1 debe retornar valor límite inferior")
    void constructor_withUserId1_shouldReturnMinimumValidValue() {
        // Given
        int expectedUserId = 1;

        // When
        UserRequest request = new UserRequest(expectedUserId);

        // Then
        assertThat(request.getUserId()).isEqualTo(1);
    }

    @Test
    @DisplayName("DTO-03: Constructor por defecto debe inicializar userId en 0")
    void defaultConstructor_shouldHaveZeroUserId() {
        // When
        UserRequest request = new UserRequest();

        // Then
        assertThat(request.getUserId()).isZero();
    }

    @Test
    @DisplayName("DTO-03: toString() debe contener 'userId=25' para userId=25")
    void toString_withUserId25_shouldContainUserId25() {
        // Given
        UserRequest request = new UserRequest(25);

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("userId=25");
    }

    @Test
    @DisplayName("DTO-03: toString() debe seguir el formato 'UserRequest{userId=X}'")
    void toString_shouldFollowExpectedFormat() {
        // Given
        UserRequest request = new UserRequest(100);

        // When
        String result = request.toString();

        // Then
        assertThat(result).isEqualTo("UserRequest{userId=100}");
    }

    @Test
    @DisplayName("DTO-03: setUserId debe actualizar el userId correctamente")
    void setUserId_shouldUpdateUserId() {
        // Given
        UserRequest request = new UserRequest(10);

        // When
        request.setUserId(50);

        // Then
        assertThat(request.getUserId()).isEqualTo(50);
    }
}
