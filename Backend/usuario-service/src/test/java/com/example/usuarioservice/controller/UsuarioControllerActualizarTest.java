package com.example.usuarioservice.controller;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.exception.UsuarioNotFoundException;
import com.example.usuarioservice.exception.UsuarioYaExisteException;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.service.IUsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuarioController.actualizar() endpoint (PUT).
 *
 * Tests verify correct HTTP status codes for update operations.
 * Covers HU-USR-04 requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - PUT /v1/usuarios/{id} endpoint")
class UsuarioControllerActualizarTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    /**
     * Test: Actualizar usuario exitosamente
     *
     * Given: Valid UpdateUsuarioRequest y usuario existente
     * When: PUT /v1/usuarios/{id} is called
     * Then: HTTP 200 OK con usuario actualizado
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} actualiza usuario exitosamente y devuelve 200 OK")
    void actualizar_shouldReturn200OK_whenUpdateIsValid() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan Actualizado")
            .email("juan.nuevo@example.com")
            .contrasena("NewPass123")
            .activo(true)
            .build();

        User updatedUser = new User(userId, "Juan Actualizado", "NewPass123", "juan.nuevo@example.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("Juan Actualizado", response.getBody().getNombre());
        assertEquals("juan.nuevo@example.com", response.getBody().getEmail());

        verify(usuarioService, times(1)).actualizar(userId, request);
    }

    /**
     * Test: Usuario no encontrado
     *
     * Given: Usuario con ID no existe
     * When: PUT /v1/usuarios/{id} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} lanza UsuarioNotFoundException cuando usuario no existe")
    void actualizar_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Given
        int userId = 999;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@example.com")
            .contrasena("ValidPass123")
            .build();

        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.empty());

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.actualizar(userId, request)
        );

        assertEquals("Usuario no encontrado: 999", exception.getMessage());
        verify(usuarioService, times(1)).actualizar(userId, request);
    }

    /**
     * Test: Email duplicado
     *
     * Given: UpdateUsuarioRequest con email que ya existe (otro usuario)
     * When: PUT /v1/usuarios/{id} is called
     * Then: UsuarioYaExisteException (409 Conflict)
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} lanza UsuarioYaExisteException cuando email ya existe")
    void actualizar_shouldThrowUsuarioYaExisteException_whenEmailIsAlreadyUsed() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan")
            .email("otro@example.com")
            .contrasena("ValidPass123")
            .build();

        when(usuarioService.actualizar(userId, request))
            .thenThrow(new UsuarioYaExisteException("El email otro@example.com ya está registrado"));

        // When & Then
        UsuarioYaExisteException exception = assertThrows(
            UsuarioYaExisteException.class,
            () -> controller.actualizar(userId, request)
        );

        assertTrue(exception.getMessage().contains("otro@example.com"));
        verify(usuarioService, times(1)).actualizar(userId, request);
    }

    /**
     * Test: Actualizar solo nombre
     *
     * Given: UpdateUsuarioRequest con solo nombre
     * When: PUT /v1/usuarios/{id} is called
     * Then: HTTP 200 OK con usuario actualizado
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} actualiza solo el nombre cuando otros campos son null")
    void actualizar_shouldUpdateOnlyName_whenOtherFieldsAreNull() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Nuevo Nombre")
            .email(null)
            .contrasena(null)
            .activo(null)
            .build();

        User updatedUser = new User(userId, "Nuevo Nombre", "oldpass", "old@test.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nuevo Nombre", response.getBody().getNombre());
    }

    /**
     * Test: Cambiar email a uno no duplicado
     *
     * Given: Email nuevo no está en uso
     * When: PUT /v1/usuarios/{id} is called
     * Then: HTTP 200 OK con email actualizado
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} permite cambiar email a uno no duplicado")
    void actualizar_shouldAllowEmailChange_whenNewEmailIsNotUsed() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("nuevo@example.com")
            .build();

        User updatedUser = new User(userId, "Juan", "pass", "nuevo@example.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("nuevo@example.com", response.getBody().getEmail());
    }

    /**
     * Test: Desactivar usuario
     *
     * Given: UpdateUsuarioRequest con activo=false
     * When: PUT /v1/usuarios/{id} is called
     * Then: Usuario es desactivado (activo=false)
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} puede desactivar usuario")
    void actualizar_shouldDeactivateUser_whenActivoIsFalse() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .activo(false)
            .build();

        User deactivatedUser = new User(userId, "Juan", "pass", "juan@test.com", false);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(deactivatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActivo());
    }

    /**
     * Test: Mantener email igual es permitido
     *
     * Given: Email en request es igual al email actual
     * When: PUT /v1/usuarios/{id} is called
     * Then: HTTP 200 OK (no lanza excepción de duplicado)
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} permite mantener el mismo email")
    void actualizar_shouldAllowSameEmail() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan")
            .email("juan@example.com")  // mismo email actual
            .build();

        User unchangedUser = new User(userId, "Juan", "pass", "juan@example.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(unchangedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("juan@example.com", response.getBody().getEmail());
    }

    /**
     * Test: Response contiene todos los campos actualizados
     *
     * Given: Actualización exitosa
     * When: PUT /v1/usuarios/{id} is called
     * Then: Response contiene todos los campos
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} retorna response completo con campos actualizados")
    void actualizar_shouldReturnCompleteResponse() {
        // Given
        int userId = 42;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Carlos Actualizado")
            .email("carlos.nuevo@example.com")
            .activo(true)
            .build();

        User updatedUser = new User(userId, "Carlos Actualizado", "pass", "carlos.nuevo@example.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        UsuarioResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(userId, body.getId());
        assertEquals("Carlos Actualizado", body.getNombre());
        assertEquals("carlos.nuevo@example.com", body.getEmail());
        assertTrue(body.isActivo());
    }

    /**
     * Test: Service es invocado correctamente
     *
     * Given: UpdateUsuarioRequest válido
     * When: PUT /v1/usuarios/{id} is called
     * Then: Service.actualizar es invocado con id y request correctos
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} invoca service.actualizar() con parámetros correctos")
    void actualizar_shouldCallServiceWithCorrectParameters() {
        // Given
        int userId = 5;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Test")
            .build();

        User updatedUser = new User(userId, "Test", "pass", "test@test.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        controller.actualizar(userId, request);

        // Then
        verify(usuarioService, times(1)).actualizar(userId, request);
        verifyNoMoreInteractions(usuarioService);
    }

    /**
     * Test: Cambiar contraseña
     *
     * Given: UpdateUsuarioRequest con contraseña nueva
     * When: PUT /v1/usuarios/{id} is called
     * Then: Contraseña es actualizada
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} actualiza la contraseña correctamente")
    void actualizar_shouldUpdatePassword() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .contrasena("NewSecurePass123")
            .build();

        User updatedUser = new User(userId, "Juan", "NewSecurePass123", "juan@test.com", true);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(usuarioService, times(1)).actualizar(userId, request);
    }

    /**
     * Test: Múltiples campos actualizados simultáneamente
     *
     * Given: UpdateUsuarioRequest con todos los campos
     * When: PUT /v1/usuarios/{id} is called
     * Then: Todos los campos son actualizados
     */
    @Test
    @DisplayName("PUT /v1/usuarios/{id} actualiza múltiples campos simultáneamente")
    void actualizar_shouldUpdateMultipleFields() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Nombre Nuevo")
            .email("nuevo@example.com")
            .contrasena("NewPass123")
            .activo(false)
            .build();

        User updatedUser = new User(userId, "Nombre Nuevo", "NewPass123", "nuevo@example.com", false);
        when(usuarioService.actualizar(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizar(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UsuarioResponse body = response.getBody();
        assertEquals("Nombre Nuevo", body.getNombre());
        assertEquals("nuevo@example.com", body.getEmail());
        assertFalse(body.isActivo());
    }
}

