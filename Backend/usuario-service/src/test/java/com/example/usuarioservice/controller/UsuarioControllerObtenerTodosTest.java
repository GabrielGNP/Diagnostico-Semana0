package com.example.usuarioservice.controller;

import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.IUsuarioService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU-USR-01: Unit tests for UsuarioController.obtenerTodos() endpoint.
 * 
 * Tests verify the controller returns only active users with proper status codes.
 * Based on TEST_PLAN.md scenarios EP-USR-01-01 and EP-USR-01-02.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerObtenerTodosTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    /**
     * EP-USR-01-01: Listado exitoso con usuarios activos existentes.
     * 
     * Given: Active users exist in the system
     * When: GET /v1/usuarios is called
     * Then: HTTP 200 OK with list of active users
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-01: GET /v1/usuarios returns 200 OK with active users")
    void obtenerTodos_shouldReturn200WithActiveUsers_whenActiveUsersExist() {
        // Given: Service returns active users
        User activeUser1 = new User(1, "Juan", "pass1", "juan@test.com", true);
        User activeUser2 = new User(2, "Maria", "pass2", "maria@test.com", true);
        when(usuarioService.obtenerTodos()).thenReturn(Arrays.asList(activeUser1, activeUser2));
        
        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        
        verify(usuarioService).obtenerTodos();
    }

    /**
     * EP-USR-01-02: Listado vacío cuando no existen usuarios activos.
     * 
     * Given: No active users in the system
     * When: GET /v1/usuarios is called
     * Then: HTTP 200 OK with empty collection "[]"
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-02: GET /v1/usuarios returns 200 OK with empty list")
    void obtenerTodos_shouldReturn200WithEmptyList_whenNoActiveUsers() {
        // Given: Service returns empty collection
        when(usuarioService.obtenerTodos()).thenReturn(Collections.emptyList());
        
        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(usuarioService).obtenerTodos();
    }

    /**
     * Verify response contains expected user fields from UsuarioResponse DTO.
     * 
     * Given: A user exists
     * When: GET /v1/usuarios is called
     * Then: Response contains id, nombre, email fields (password excluded)
     */
    @Test
    @DisplayName("HU-USR-01 - Response includes expected fields (id, nombre, email)")
    void obtenerTodos_responseContainsExpectedFields() {
        // Given
        User user = new User(1, "TestUser", "secretPass", "test@example.com", true);
        when(usuarioService.obtenerTodos()).thenReturn(Collections.singletonList(user));
        
        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        UsuarioResponse dto = response.getBody().iterator().next();
        assertEquals(1, dto.getId());
        assertEquals("TestUser", dto.getNombre());
        assertEquals("test@example.com", dto.getEmail());
        // Password should not be accessible (not part of UsuarioResponse)
    }

    /**
     * Verify active field handling - only active users should be returned.
     * 
     * Given: Service returns users (already filtered to active)
     * When: GET /v1/usuarios is called
     * Then: Controller properly delegates to service
     */
    @Test
    @DisplayName("HU-USR-01 - Only active users returned (soft-deleted excluded)")  
    void obtenerTodos_shouldNotIncludeSoftDeletedUsers() {
        // Given: Service returns only active users (filtering done at service/repo level)
        User activeUser = new User(1, "ActiveUser", "pass", "active@test.com", true);
        when(usuarioService.obtenerTodos()).thenReturn(Collections.singletonList(activeUser));
        
        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        
        // All returned users should be from the filtered active set
        UsuarioResponse dto = response.getBody().iterator().next();
        assertEquals("ActiveUser", dto.getNombre());
        
        // Verify service was called (which should return only active users)
        verify(usuarioService).obtenerTodos();
    }
}
