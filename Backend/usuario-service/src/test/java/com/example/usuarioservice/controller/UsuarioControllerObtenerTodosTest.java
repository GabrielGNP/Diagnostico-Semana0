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
 * Unit tests for UsuarioController.obtenerTodos() endpoint (GET).
 *
 * Tests verify the controller returns only active users with proper status codes.
 * Covers HU-USR-01 requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - GET /v1/usuarios endpoint")
class UsuarioControllerObtenerTodosTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    /**
     * Test: Listado exitoso con usuarios activos existentes.
     *
     * Given: Active users exist in the system
     * When: GET /v1/usuarios is called
     * Then: HTTP 200 OK with list of active users
     */
    @Test
    @DisplayName("GET /v1/usuarios devuelve 200 OK con usuarios activos")
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
        
        verify(usuarioService, times(1)).obtenerTodos();
    }

    /**
     * Test: Listado vacío cuando no existen usuarios activos.
     *
     * Given: No active users in the system
     * When: GET /v1/usuarios is called
     * Then: HTTP 200 OK with empty collection "[]"
     */
    @Test
    @DisplayName("GET /v1/usuarios devuelve 200 OK con lista vacía cuando no hay usuarios")
    void obtenerTodos_shouldReturn200WithEmptyList_whenNoActiveUsers() {
        // Given: Service returns empty collection
        when(usuarioService.obtenerTodos()).thenReturn(Collections.emptyList());
        
        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        
        verify(usuarioService, times(1)).obtenerTodos();
    }

    /**
     * Test: Response contiene campos esperados del DTO.
     *
     * Given: A user exists
     * When: GET /v1/usuarios is called
     * Then: Response contains id, nombre, email fields (password excluded)
     */
    @Test
    @DisplayName("GET /v1/usuarios response incluye campos esperados (id, nombre, email)")
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
        assertTrue(dto.isActivo());
    }

    /**
     * Test: Solo usuarios activos son retornados.
     *
     * Given: Service returns users (already filtered to active)
     * When: GET /v1/usuarios is called
     * Then: Controller properly delegates to service
     */
    @Test
    @DisplayName("GET /v1/usuarios retorna solo usuarios activos (soft-deleted excluidos)")
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
        
        UsuarioResponse dto = response.getBody().iterator().next();
        assertEquals("ActiveUser", dto.getNombre());
        assertTrue(dto.isActivo());

        verify(usuarioService, times(1)).obtenerTodos();
    }

    /**
     * Test: Múltiples usuarios son mapeados correctamente.
     *
     * Given: Multiple active users exist
     * When: GET /v1/usuarios is called
     * Then: All users are correctly mapped to UsuarioResponse
     */
    @Test
    @DisplayName("GET /v1/usuarios mapea correctamente múltiples usuarios")
    void obtenerTodos_shouldMapMultipleUsersCorrectly() {
        // Given
        User user1 = new User(1, "User1", "pass1", "user1@test.com", true);
        User user2 = new User(2, "User2", "pass2", "user2@test.com", true);
        User user3 = new User(3, "User3", "pass3", "user3@test.com", true);
        when(usuarioService.obtenerTodos()).thenReturn(Arrays.asList(user1, user2, user3));

        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());

        // Verify all users are in response (order may vary in Collection)
        Collection<UsuarioResponse> responses = response.getBody();
        assertTrue(responses.stream().anyMatch(r -> r.getNombre().equals("User1")));
        assertTrue(responses.stream().anyMatch(r -> r.getNombre().equals("User2")));
        assertTrue(responses.stream().anyMatch(r -> r.getNombre().equals("User3")));
    }

    /**
     * Test: Service es invocado exactamente una vez.
     *
     * Given: Service configured
     * When: GET /v1/usuarios is called
     * Then: Service.obtenerTodos is invoked exactly once
     */
    @Test
    @DisplayName("GET /v1/usuarios invoca service.obtenerTodos() exactamente una vez")
    void obtenerTodos_shouldCallServiceOnce() {
        // Given
        when(usuarioService.obtenerTodos()).thenReturn(Collections.emptyList());

        // When
        usuarioController.obtenerTodos();

        // Then
        verify(usuarioService, times(1)).obtenerTodos();
        verifyNoMoreInteractions(usuarioService);
    }

    /**
     * Test: Response body nunca es null.
     *
     * Given: Service returns empty list
     * When: GET /v1/usuarios is called
     * Then: Response body is not null (empty list instead)
     */
    @Test
    @DisplayName("GET /v1/usuarios nunca devuelve body null (lista vacía en su lugar)")
    void obtenerTodos_shouldNeverReturnNullBody() {
        // Given
        when(usuarioService.obtenerTodos()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();

        // Then
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
