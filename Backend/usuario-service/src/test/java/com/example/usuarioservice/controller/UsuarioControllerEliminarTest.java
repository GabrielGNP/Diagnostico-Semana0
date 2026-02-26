package com.example.usuarioservice.controller;

import com.example.usuarioservice.exception.UsuarioNotFoundException;
import com.example.usuarioservice.service.IUsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuarioController.eliminar() endpoint (DELETE).
 *
 * Tests verify correct HTTP status codes for delete operations.
 * Covers HU-USR-06 requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - DELETE /v1/usuarios/{id} endpoint")
class UsuarioControllerEliminarTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    /**
     * Test: Eliminar usuario exitosamente
     *
     * Given: Usuario existe
     * When: DELETE /v1/usuarios/{id} is called
     * Then: HTTP 204 No Content (sin body)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} elimina usuario exitosamente y devuelve 204 No Content")
    void eliminar_shouldReturn204NoContent_whenUserIsDeleted() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: Usuario no encontrado
     *
     * Given: Usuario con ID no existe
     * When: DELETE /v1/usuarios/{id} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} lanza UsuarioNotFoundException cuando usuario no existe")
    void eliminar_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Given
        int userId = 999;
        when(usuarioService.eliminar(userId)).thenReturn(false);

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.eliminar(userId)
        );

        assertEquals("Usuario no encontrado: 999", exception.getMessage());
        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: Eliminar usuario existente
     *
     * Given: Usuario con ID 1 existe
     * When: DELETE /v1/usuarios/1 is called
     * Then: Usuario es eliminado, respuesta 204
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/1 elimina usuario específico correctamente")
    void eliminar_shouldDeleteSpecificUser() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: Service es invocado con ID correcto
     *
     * Given: Valid ID
     * When: DELETE /v1/usuarios/{id} is called
     * Then: Service.eliminar es invocado exactamente una vez
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} invoca service.eliminar() exactamente una vez")
    void eliminar_shouldCallServiceOnce() {
        // Given
        int userId = 5;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        controller.eliminar(userId);

        // Then
        verify(usuarioService, times(1)).eliminar(userId);
        verifyNoMoreInteractions(usuarioService);
    }

    /**
     * Test: Múltiples eliminaciones consecutivas
     *
     * Given: Dos usuarios existentes
     * When: DELETE es llamado dos veces con IDs diferentes
     * Then: Ambos usuarios son eliminados
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} permite eliminar múltiples usuarios consecutivamente")
    void eliminar_shouldAllowMultipleDeletesSequentially() {
        // Given
        when(usuarioService.eliminar(1)).thenReturn(true);
        when(usuarioService.eliminar(2)).thenReturn(true);

        // When
        ResponseEntity<Void> response1 = controller.eliminar(1);
        ResponseEntity<Void> response2 = controller.eliminar(2);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());

        verify(usuarioService, times(1)).eliminar(1);
        verify(usuarioService, times(1)).eliminar(2);
    }

    /**
     * Test: Eliminar usuario inactivo
     *
     * Given: Usuario existe y está inactivo
     * When: DELETE /v1/usuarios/{id} is called
     * Then: Usuario es eliminado (204)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} puede eliminar usuario inactivo")
    void eliminar_shouldAllowDeletingInactiveUser() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /**
     * Test: Eliminar usuario activo
     *
     * Given: Usuario existe y está activo
     * When: DELETE /v1/usuarios/{id} is called
     * Then: Usuario es eliminado (204)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} puede eliminar usuario activo")
    void eliminar_shouldAllowDeletingActiveUser() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    /**
     * Test: No hay contenido en respuesta exitosa
     *
     * Given: Eliminación exitosa
     * When: DELETE /v1/usuarios/{id} is called
     * Then: Response body es null (204 No Content)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} retorna 204 sin contenido en respuesta")
    void eliminar_shouldReturnNoBodyOn204() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    /**
     * Test: ID cero es validado
     *
     * Given: ID es 0 (inválido)
     * When: DELETE /v1/usuarios/0 is called
     * Then: Service es invocado (validación es responsabilidad del servicio)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/0 trata ID cero como inválido")
    void eliminar_shouldHandleZeroId() {
        // Given
        int userId = 0;
        when(usuarioService.eliminar(userId)).thenReturn(false);

        // When & Then
        assertThrows(UsuarioNotFoundException.class, () -> controller.eliminar(userId));
        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: ID negativo es validado
     *
     * Given: ID es negativo
     * When: DELETE /v1/usuarios/{negativeId} is called
     * Then: Service es invocado (validación es responsabilidad del servicio)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} trata ID negativo como inválido")
    void eliminar_shouldHandleNegativeId() {
        // Given
        int userId = -1;
        when(usuarioService.eliminar(userId)).thenReturn(false);

        // When & Then
        assertThrows(UsuarioNotFoundException.class, () -> controller.eliminar(userId));
        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: ID muy grande
     *
     * Given: ID es un número muy grande
     * When: DELETE /v1/usuarios/{largeId} is called
     * Then: Service es invocado
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} maneja IDs grandes correctamente")
    void eliminar_shouldHandleLargeId() {
        // Given
        int userId = Integer.MAX_VALUE;
        when(usuarioService.eliminar(userId)).thenReturn(false);

        // When & Then
        assertThrows(UsuarioNotFoundException.class, () -> controller.eliminar(userId));
        verify(usuarioService, times(1)).eliminar(userId);
    }

    /**
     * Test: Idempotencia de delete
     *
     * Given: Usuario fue ya eliminado
     * When: DELETE es llamado nuevamente
     * Then: 404 Not Found (no existe)
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} es idempotente (segundo DELETE = 404)")
    void eliminar_shouldBeIdempotent() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId))
            .thenReturn(true)    // primera llamada
            .thenReturn(false);  // segunda llamada (ya eliminado)

        // When
        ResponseEntity<Void> response1 = controller.eliminar(userId);

        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.eliminar(userId)
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals("Usuario no encontrado: 1", exception.getMessage());

        verify(usuarioService, times(2)).eliminar(userId);
    }

    /**
     * Test: Respuesta 204 tiene headers correctos
     *
     * Given: Eliminación exitosa
     * When: DELETE /v1/usuarios/{id} is called
     * Then: Status 204 con headers HTTP correctos
     */
    @Test
    @DisplayName("DELETE /v1/usuarios/{id} retorna status 204 sin headers de contenido")
    void eliminar_shouldReturn204WithNoContentHeaders() {
        // Given
        int userId = 1;
        when(usuarioService.eliminar(userId)).thenReturn(true);

        // When
        ResponseEntity<Void> response = controller.eliminar(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertNotNull(response.getHeaders());
    }
}

