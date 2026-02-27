package com.example.usuarioservice.dto;

import com.example.usuarioservice.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UsuarioResponse DTO.
 * Tests factory methods, null-safety, and serialization.
 */
@DisplayName("UsuarioResponse - DTO factory methods")
class UsuarioResponseTest {

    @Test
    @DisplayName("from() converts User to Response successfully")
    void from_shouldConvertUserToResponse() {
        // Given
        User user = new User(1, "Juan", "pass", "juan@test.com", true);

        // When
        UsuarioResponse response = UsuarioResponse.from(user);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Juan", response.getNombre());
        assertEquals("juan@test.com", response.getEmail());
        assertTrue(response.isActivo());
    }

    @Test
    @DisplayName("from() throws IllegalArgumentException when User is null")
    void from_shouldThrowException_whenUserIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> UsuarioResponse.from(null));
    }

    @Test
    @DisplayName("from() handles null nombre field")
    void from_shouldHandleNullNombre() {
        // Given
        User user = new User(1, null, "pass", "juan@test.com", true);

        // When
        UsuarioResponse response = UsuarioResponse.from(user);

        // Then
        assertEquals("", response.getNombre());
    }

    @Test
    @DisplayName("from() handles null email field")
    void from_shouldHandleNullEmail() {
        // Given
        User user = new User(1, "Juan", "pass", null, true);

        // When
        UsuarioResponse response = UsuarioResponse.from(user);

        // Then
        assertEquals("", response.getEmail());
    }

    @Test
    @DisplayName("from() preserves active field")
    void from_shouldPreserveActiveField() {
        // Given
        User activeUser = new User(1, "Juan", "pass", "juan@test.com", true);
        User inactiveUser = new User(2, "Maria", "pass", "maria@test.com", false);

        // When
        UsuarioResponse activeResponse = UsuarioResponse.from(activeUser);
        UsuarioResponse inactiveResponse = UsuarioResponse.from(inactiveUser);

        // Then
        assertTrue(activeResponse.isActivo());
        assertFalse(inactiveResponse.isActivo());
    }

    @Test
    @DisplayName("from() creates separate instances for different users")
    void from_shouldCreateDifferentInstances() {
        // Given
        User user1 = new User(1, "Juan", "pass", "juan@test.com", true);
        User user2 = new User(2, "Maria", "pass", "maria@test.com", true);

        // When
        UsuarioResponse response1 = UsuarioResponse.from(user1);
        UsuarioResponse response2 = UsuarioResponse.from(user2);

        // Then
        assertNotEquals(response1.getId(), response2.getId());
        assertNotEquals(response1.getNombre(), response2.getNombre());
    }

    @Test
    @DisplayName("fromOptional() returns Optional with Response when User is valid")
    void fromOptional_shouldReturnOptionalWithResponse_whenUserIsValid() {
        // Given
        User user = new User(1, "Juan", "pass", "juan@test.com", true);

        // When
        Optional<UsuarioResponse> result = UsuarioResponse.fromOptional(user);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Juan", result.get().getNombre());
    }

    @Test
    @DisplayName("fromOptional() returns empty Optional when User is null")
    void fromOptional_shouldReturnEmptyOptional_whenUserIsNull() {
        // When
        Optional<UsuarioResponse> result = UsuarioResponse.fromOptional(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("UsuarioResponse has all required fields")
    void usuarioResponse_shouldHaveAllRequiredFields() {
        // Given
        User user = new User(1, "Juan", "pass", "juan@test.com", true);

        // When
        UsuarioResponse response = UsuarioResponse.from(user);

        // Then
        assertNotNull(response.getId());
        assertNotNull(response.getNombre());
        assertNotNull(response.getEmail());
        // activo is primitive boolean, always has value
    }

    @Test
    @DisplayName("UsuarioResponse builder works correctly")
    void usuarioResponse_builderShouldWorkCorrectly() {
        // When
        UsuarioResponse response = UsuarioResponse.builder()
            .id(5)
            .nombre("Test User")
            .email("test@example.com")
            .activo(true)
            .build();

        // Then
        assertEquals(5, response.getId());
        assertEquals("Test User", response.getNombre());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isActivo());
    }
}

