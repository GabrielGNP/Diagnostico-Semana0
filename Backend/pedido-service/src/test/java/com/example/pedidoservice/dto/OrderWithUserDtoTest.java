package com.example.pedidoservice.dto;

import com.example.pedidoservice.messaging.UserResponse;
import com.example.pedidoservice.model.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para OrderWithUserDto.
 * Cubre escenario DTO-04 del TEST_PLAN.md
 */
class OrderWithUserDtoTest {

    // ========== DTO-04: Setters y Getters ==========

    @Test
    @DisplayName("DTO-04: Constructor por defecto debe crear instancia con campos null")
    void defaultConstructor_shouldCreateEmptyInstance() {
        // When
        OrderWithUserDto dto = new OrderWithUserDto();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getIdUser()).isNull();
        assertThat(dto.getState()).isNull();
        assertThat(dto.isActive()).isNull();
        assertThat(dto.getUser()).isNull();
    }

    @Test
    @DisplayName("DTO-04: Constructor con todos los parámetros debe poblar todos los campos")
    void allArgsConstructor_shouldPopulateAllFields() {
        // Given
        UserResponse user = new UserResponse(10, "John Doe", "john@test.com", true);

        // When
        OrderWithUserDto dto = new OrderWithUserDto(
                1, "Test Order", "Test Description", 10, State.PROCESSING, true, user
        );

        // Then
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getName()).isEqualTo("Test Order");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getIdUser()).isEqualTo(10);
        assertThat(dto.getState()).isEqualTo(State.PROCESSING);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("DTO-04: setId debe actualizar el campo id correctamente")
    void setId_shouldUpdateIdField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setId(100);

        // Then
        assertThat(dto.getId()).isEqualTo(100);
    }

    @Test
    @DisplayName("DTO-04: setName debe actualizar el campo name correctamente")
    void setName_shouldUpdateNameField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setName("Updated Order Name");

        // Then
        assertThat(dto.getName()).isEqualTo("Updated Order Name");
    }

    @Test
    @DisplayName("DTO-04: setDescription debe actualizar el campo description correctamente")
    void setDescription_shouldUpdateDescriptionField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setDescription("Updated Description");

        // Then
        assertThat(dto.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("DTO-04: setIdUser debe actualizar el campo idUser correctamente")
    void setIdUser_shouldUpdateIdUserField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setIdUser(50);

        // Then
        assertThat(dto.getIdUser()).isEqualTo(50);
    }

    @Test
    @DisplayName("DTO-04: setState debe actualizar el campo state correctamente")
    void setState_shouldUpdateStateField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setState(State.DELIVERED);

        // Then
        assertThat(dto.getState()).isEqualTo(State.DELIVERED);
    }

    @Test
    @DisplayName("DTO-04: setActive debe actualizar el campo active correctamente")
    void setActive_shouldUpdateActiveField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();

        // When
        dto.setActive(false);

        // Then
        assertThat(dto.isActive()).isFalse();
    }

    @Test
    @DisplayName("DTO-04: setUser debe actualizar el campo user correctamente")
    void setUser_shouldUpdateUserField() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();
        UserResponse user = new UserResponse(20, "Jane Doe", "jane@test.com", true);

        // When
        dto.setUser(user);

        // Then
        assertThat(dto.getUser()).isEqualTo(user);
        assertThat(dto.getUser().getId()).isEqualTo(20);
        assertThat(dto.getUser().getName()).isEqualTo("Jane Doe");
    }

    @Test
    @DisplayName("DTO-04: Todos los setters deben funcionar en secuencia")
    void allSetters_shouldWorkInSequence() {
        // Given
        OrderWithUserDto dto = new OrderWithUserDto();
        UserResponse user = new UserResponse(30, "Test User", "test@mail.com", true);

        // When
        dto.setId(5);
        dto.setName("Sequential Test");
        dto.setDescription("Sequential Description");
        dto.setIdUser(30);
        dto.setState(State.IN_WAREHOUSE);
        dto.setActive(true);
        dto.setUser(user);

        // Then
        assertThat(dto.getId()).isEqualTo(5);
        assertThat(dto.getName()).isEqualTo("Sequential Test");
        assertThat(dto.getDescription()).isEqualTo("Sequential Description");
        assertThat(dto.getIdUser()).isEqualTo(30);
        assertThat(dto.getState()).isEqualTo(State.IN_WAREHOUSE);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("DTO-04: toString debe contener todos los campos")
    void toString_shouldContainAllFields() {
        // Given
        UserResponse user = new UserResponse(10, "John", "john@test.com", true);
        OrderWithUserDto dto = new OrderWithUserDto(
                1, "Test", "Desc", 10, State.PROCESSING, true, user
        );

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("id=1");
        assertThat(result).contains("name='Test'");
        assertThat(result).contains("description='Desc'");
        assertThat(result).contains("idUser=10");
        assertThat(result).contains("state=PROCESSING");
        assertThat(result).contains("active=true");
    }
}
