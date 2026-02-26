package com.example.pedidoservice.service;

import com.example.pedidoservice.messaging.IUserInfoClient;
import com.example.pedidoservice.messaging.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEnrichmentServiceTest {

    @Mock
    private IUserInfoClient userInfoClient;

    @InjectMocks
    private UserEnrichmentService userEnrichmentService;

    @Test
    @DisplayName("US-01 - Obtener información de usuario exitosamente")
    @Tag("high")
    void fetchUserInfo_shouldReturnUserResponse_whenClientReturns() {
        // Arrange
        UserResponse expected = new UserResponse(5, "Alice", "alice@test.com", true);
        when(userInfoClient.fetchUserInfo(eq(5), anyLong())).thenReturn(expected);

        // Act
        UserResponse result = userEnrichmentService.fetchUserInfo(5);

        // Assert
        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getName(), result.getName());
        verify(userInfoClient).fetchUserInfo(eq(5), anyLong());
    }

    @Test
    @DisplayName("US-02 - Manejar error al obtener información de usuario")
    @Tag("high")
    void fetchUserInfo_shouldReturnNull_whenClientThrows() {
        // Arrange
        doThrow(new RuntimeException("down")).when(userInfoClient).fetchUserInfo(eq(5), anyLong());

        // Act
        UserResponse result = userEnrichmentService.fetchUserInfo(5);

        // Assert
        assertNull(result, "Should return null when client throws");
        verify(userInfoClient).fetchUserInfo(eq(5), anyLong());
    }
}
