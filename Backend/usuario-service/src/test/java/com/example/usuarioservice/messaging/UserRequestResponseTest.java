package com.example.usuarioservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRequest and UserResponse DTOs.
 * Tests serialization/deserialization and data integrity.
 */
@DisplayName("UserRequest and UserResponse - messaging DTOs")
class UserRequestResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("UserRequest serializes and deserializes correctly")
    void userRequest_shouldSerializeAndDeserialize() throws Exception {
        // Given
        UserRequest original = new UserRequest(42);

        // When
        String json = objectMapper.writeValueAsString(original);
        UserRequest deserialized = objectMapper.readValue(json, UserRequest.class);

        // Then
        assertEquals(original.getUserId(), deserialized.getUserId());
        assertTrue(json.contains("userId"));
        assertTrue(json.contains("42"));
    }

    @Test
    @DisplayName("UserResponse serializes and deserializes correctly")
    void userResponse_shouldSerializeAndDeserialize() throws Exception {
        // Given
        UserResponse original = new UserResponse(1, "Juan", "juan@test.com", true);

        // When
        String json = objectMapper.writeValueAsString(original);
        UserResponse deserialized = objectMapper.readValue(json, UserResponse.class);

        // Then
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getMail(), deserialized.getMail());
        assertEquals(original.isActive(), deserialized.isActive());
    }

    @Test
    @DisplayName("UserRequest default constructor works")
    void userRequest_defaultConstructor_shouldWork() {
        // When
        UserRequest request = new UserRequest();
        request.setUserId(10);

        // Then
        assertEquals(10, request.getUserId());
    }

    @Test
    @DisplayName("UserResponse default constructor works")
    void userResponse_defaultConstructor_shouldWork() {
        // When
        UserResponse response = new UserResponse();
        response.setId(5);
        response.setName("Test");
        response.setMail("test@test.com");
        response.setActive(false);

        // Then
        assertEquals(5, response.getId());
        assertEquals("Test", response.getName());
        assertEquals("test@test.com", response.getMail());
        assertFalse(response.isActive());
    }

    @Test
    @DisplayName("UserRequest toString includes userId")
    void userRequest_toString_shouldIncludeUserId() {
        // Given
        UserRequest request = new UserRequest(123);

        // When
        String toString = request.toString();

        // Then
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("userId"));
    }

    @Test
    @DisplayName("UserResponse toString includes all fields")
    void userResponse_toString_shouldIncludeAllFields() {
        // Given
        UserResponse response = new UserResponse(1, "Juan", "juan@test.com", true);

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("juan@test.com"));
        assertTrue(toString.contains("true"));
    }

    @Test
    @DisplayName("UserRequest handles zero userId")
    void userRequest_shouldHandleZeroUserId() {
        // Given
        UserRequest request = new UserRequest(0);

        // Then
        assertEquals(0, request.getUserId());
    }

    @Test
    @DisplayName("UserRequest handles negative userId")
    void userRequest_shouldHandleNegativeUserId() {
        // Given
        UserRequest request = new UserRequest(-1);

        // Then
        assertEquals(-1, request.getUserId());
    }

    @Test
    @DisplayName("UserResponse handles null values")
    void userResponse_shouldHandleNullValues() {
        // Given
        UserResponse response = new UserResponse(null, null, null, false);

        // Then
        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getMail());
        assertFalse(response.isActive());
    }

    @Test
    @DisplayName("UserResponse JSON uses correct property names")
    void userResponse_shouldUseCorrectJsonPropertyNames() throws Exception {
        // Given
        UserResponse response = new UserResponse(1, "Juan", "juan@test.com", true);

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertTrue(json.contains("\"id\":"));
        assertTrue(json.contains("\"name\":"));
        assertTrue(json.contains("\"mail\":"));
        assertTrue(json.contains("\"active\":"));
    }

    @Test
    @DisplayName("UserRequest JSON uses correct property name")
    void userRequest_shouldUseCorrectJsonPropertyName() throws Exception {
        // Given
        UserRequest request = new UserRequest(42);

        // When
        String json = objectMapper.writeValueAsString(request);

        // Then
        assertTrue(json.contains("\"userId\":"));
    }
}

