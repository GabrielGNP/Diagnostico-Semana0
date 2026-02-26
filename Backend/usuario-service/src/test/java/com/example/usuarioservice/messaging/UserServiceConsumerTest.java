package com.example.usuarioservice.messaging;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceConsumer.
 * Tests RabbitMQ message consumption and user lookup logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceConsumer - RabbitMQ message handling")
class UserServiceConsumerTest {

    @Mock
    private UserServiceProducer producer;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceConsumer consumer;

    @BeforeEach
    void setUp() {
        // Ensure producer is injected correctly
        assertNotNull(consumer);
    }

    @Test
    @DisplayName("receiveUserRequest finds user by ID and sends response")
    void receiveUserRequest_shouldFindUserAndSendResponse() {
        // Given
        UserRequest request = new UserRequest(1);
        User user = new User(1, "Juan", "pass123", "juan@test.com", true);

        when(userRepository.findById(1)).thenReturn(user);

        // When
        consumer.receiveUserRequest(request);

        // Then
        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);
        verify(producer).sendUserResponse(responseCaptor.capture());

        UserResponse response = responseCaptor.getValue();
        assertEquals(1, response.getId());
        assertEquals("Juan", response.getName());
        assertEquals("juan@test.com", response.getMail());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("receiveUserRequest does not send response when user not found")
    void receiveUserRequest_shouldNotSendResponse_whenUserNotFound() {
        // Given
        UserRequest request = new UserRequest(999);
        when(userRepository.findById(999)).thenReturn(null);

        // When
        consumer.receiveUserRequest(request);

        // Then
        verify(producer, never()).sendUserResponse(any());
    }

    @Test
    @DisplayName("receiveUserRequest handles null producer gracefully")
    void receiveUserRequest_shouldHandleNullProducer() {
        // Given
        UserServiceConsumer consumerWithNullProducer = new UserServiceConsumer();
        UserRequest request = new UserRequest(1);

        // When - Should not throw exception
        assertDoesNotThrow(() -> consumerWithNullProducer.receiveUserRequest(request));
    }

    @Test
    @DisplayName("receiveUserRequest handles user ID zero")
    void receiveUserRequest_shouldHandleUserIdZero() {
        // Given
        UserRequest request = new UserRequest(0);
        when(userRepository.findById(0)).thenReturn(null);

        // When
        consumer.receiveUserRequest(request);

        // Then
        verify(userRepository).findById(0);
        verify(producer, never()).sendUserResponse(any());
    }

    @Test
    @DisplayName("receiveUserRequest handles negative user ID")
    void receiveUserRequest_shouldHandleNegativeUserId() {
        // Given
        UserRequest request = new UserRequest(-1);
        when(userRepository.findById(-1)).thenReturn(null);

        // When
        consumer.receiveUserRequest(request);

        // Then
        verify(userRepository).findById(-1);
        verify(producer, never()).sendUserResponse(any());
    }

    @Test
    @DisplayName("receiveUserRequest sends response for inactive user")
    void receiveUserRequest_shouldSendResponse_forInactiveUser() {
        // Given
        UserRequest request = new UserRequest(2);
        User inactiveUser = new User(2, "Maria", "pass456", "maria@test.com", false);

        when(userRepository.findById(2)).thenReturn(inactiveUser);

        // When
        consumer.receiveUserRequest(request);

        // Then
        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);
        verify(producer).sendUserResponse(responseCaptor.capture());

        UserResponse response = responseCaptor.getValue();
        assertEquals(2, response.getId());
        assertFalse(response.isActive());
    }

    @Test
    @DisplayName("receiveUserRequest preserves all user fields in response")
    void receiveUserRequest_shouldPreserveAllFields() {
        // Given
        UserRequest request = new UserRequest(3);
        User user = new User(3, "Carlos García", "secure", "carlos.garcia@example.com", true);

        when(userRepository.findById(3)).thenReturn(user);

        // When
        consumer.receiveUserRequest(request);

        // Then
        ArgumentCaptor<UserResponse> responseCaptor = ArgumentCaptor.forClass(UserResponse.class);
        verify(producer).sendUserResponse(responseCaptor.capture());

        UserResponse response = responseCaptor.getValue();
        assertEquals(3, response.getId());
        assertEquals("Carlos García", response.getName());
        assertEquals("carlos.garcia@example.com", response.getMail());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("receiveUserRequest calls repository exactly once")
    void receiveUserRequest_shouldCallRepositoryOnce() {
        // Given
        UserRequest request = new UserRequest(1);
        User user = new User(1, "Test", "pass", "test@test.com", true);

        when(userRepository.findById(1)).thenReturn(user);

        // When
        consumer.receiveUserRequest(request);

        // Then
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("receiveUserRequest handles repository exception")
    void receiveUserRequest_shouldPropagateRepositoryException() {
        // Given
        UserRequest request = new UserRequest(1);
        when(userRepository.findById(1)).thenThrow(new RuntimeException("DB error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> consumer.receiveUserRequest(request));
        verify(producer, never()).sendUserResponse(any());
    }

    @Test
    @DisplayName("receiveUserRequest handles large user ID")
    void receiveUserRequest_shouldHandleLargeUserId() {
        // Given
        UserRequest request = new UserRequest(Integer.MAX_VALUE);
        when(userRepository.findById(Integer.MAX_VALUE)).thenReturn(null);

        // When
        consumer.receiveUserRequest(request);

        // Then
        verify(userRepository).findById(Integer.MAX_VALUE);
        verify(producer, never()).sendUserResponse(any());
    }
}

