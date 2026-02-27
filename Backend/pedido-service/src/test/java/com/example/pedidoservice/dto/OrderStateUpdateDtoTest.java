package com.example.pedidoservice.dto;

import com.example.pedidoservice.model.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para OrderStateUpdateDto.
 * Cubre escenarios DTO-01 y DTO-02 del TEST_PLAN.md
 */
class OrderStateUpdateDtoTest {

    // ========== DTO-01: Constructor y Getters ==========

    @Test
    @DisplayName("DTO-01: Constructor con state DELIVERED debe retornar DELIVERED en getState()")
    void constructor_withDeliveredState_shouldReturnDelivered() {
        // Given
        State expectedState = State.DELIVERED;

        // When
        OrderStateUpdateDto dto = new OrderStateUpdateDto(expectedState);

        // Then
        assertThat(dto.getState()).isEqualTo(State.DELIVERED);
    }

    @Test
    @DisplayName("DTO-01: Constructor con state PROCESSING debe retornar PROCESSING en getState()")
    void constructor_withProcessingState_shouldReturnProcessing() {
        // Given
        State expectedState = State.PROCESSING;

        // When
        OrderStateUpdateDto dto = new OrderStateUpdateDto(expectedState);

        // Then
        assertThat(dto.getState()).isEqualTo(State.PROCESSING);
    }

    @Test
    @DisplayName("DTO-01: Constructor por defecto debe inicializar state como null")
    void defaultConstructor_shouldHaveNullState() {
        // When
        OrderStateUpdateDto dto = new OrderStateUpdateDto();

        // Then
        assertThat(dto.getState()).isNull();
    }

    // ========== DTO-02: Setter ==========

    @Test
    @DisplayName("DTO-02: setState(TRAVELING_TO_WAREHOUSE) debe actualizar el estado correctamente")
    void setState_withTravelingToWarehouse_shouldUpdateState() {
        // Given
        OrderStateUpdateDto dto = new OrderStateUpdateDto(State.PROCESSING);

        // When
        dto.setState(State.TRAVELING_TO_WAREHOUSE);

        // Then
        assertThat(dto.getState()).isEqualTo(State.TRAVELING_TO_WAREHOUSE);
    }

    @Test
    @DisplayName("DTO-02: setState debe permitir cambiar de cualquier estado a DELIVERED")
    void setState_toDelivered_shouldUpdateState() {
        // Given
        OrderStateUpdateDto dto = new OrderStateUpdateDto(State.IN_WAREHOUSE);

        // When
        dto.setState(State.DELIVERED);

        // Then
        assertThat(dto.getState()).isEqualTo(State.DELIVERED);
    }

    @Test
    @DisplayName("DTO-02: setState(null) debe establecer el estado como null")
    void setState_withNull_shouldSetNullState() {
        // Given
        OrderStateUpdateDto dto = new OrderStateUpdateDto(State.PROCESSING);

        // When
        dto.setState(null);

        // Then
        assertThat(dto.getState()).isNull();
    }
}
