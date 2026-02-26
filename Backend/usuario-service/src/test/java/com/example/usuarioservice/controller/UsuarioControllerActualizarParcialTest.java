package com.example.usuarioservice.controller;

import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.exception.UsuarioNotFoundException;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuarioController.actualizarParcial() endpoint (PATCH).
 *
 * Tests verify correct HTTP status codes for partial update operations.
 * PATCH allows selective field updates without requiring all fields.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - PATCH /v1/usuarios/{id} endpoint")
class UsuarioControllerActualizarParcialTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    /**
     * Test: Actualización parcial exitosa
     *
     * Given: UpdateUsuarioRequest con solo algunos campos
     * When: PATCH /v1/usuarios/{id} is called
     * Then: HTTP 200 OK con usuario actualizado parcialmente
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza parcialmente usuario y devuelve 200 OK")
    void actualizarParcial_shouldReturn200OK_whenUpdateIsPartial() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Juan Actualizado")
            .build();

        User updatedUser = new User(userId, "Juan Actualizado", "oldpass", "juan@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Actualizado", response.getBody().getNombre());

        verify(usuarioService, times(1)).actualizarParcial(userId, request);
    }

    /**
     * Test: Actualizar solo nombre
     *
     * Given: UpdateUsuarioRequest con solo nombre
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Solo nombre es actualizado, otros campos permanecen igual
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza solo nombre cuando otros campos son null")
    void actualizarParcial_shouldUpdateOnlyName_whenOnlyNameIsProvided() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Nuevo Nombre")
            .email(null)
            .contrasena(null)
            .activo(null)
            .build();

        User updatedUser = new User(userId, "Nuevo Nombre", "oldpass", "juan@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nuevo Nombre", response.getBody().getNombre());
        assertEquals("juan@test.com", response.getBody().getEmail());  // sin cambios
    }

    /**
     * Test: Actualizar solo email
     *
     * Given: UpdateUsuarioRequest con solo email
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Solo email es actualizado
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza solo email cuando solo email es proporcionado")
    void actualizarParcial_shouldUpdateOnlyEmail_whenOnlyEmailIsProvided() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .email("nuevo@example.com")
            .build();

        User updatedUser = new User(userId, "Juan", "pass", "nuevo@example.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("nuevo@example.com", response.getBody().getEmail());
    }

    /**
     * Test: Actualizar solo contraseña
     *
     * Given: UpdateUsuarioRequest con solo contraseña
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Solo contraseña es actualizada
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza solo contraseña cuando solo contraseña es proporcionada")
    void actualizarParcial_shouldUpdateOnlyPassword_whenOnlyPasswordIsProvided() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .contrasena("NewPass123")
            .build();

        User updatedUser = new User(userId, "Juan", "NewPass123", "juan@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(usuarioService, times(1)).actualizarParcial(userId, request);
    }

    /**
     * Test: Actualizar solo estado activo
     *
     * Given: UpdateUsuarioRequest con solo activo
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Solo estado activo es actualizado
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza solo estado activo cuando solo activo es proporcionado")
    void actualizarParcial_shouldUpdateOnlyActive_whenOnlyActivoIsProvided() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .activo(false)
            .build();

        User updatedUser = new User(userId, "Juan", "pass", "juan@test.com", false);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActivo());
    }

    /**
     * Test: Usuario no encontrado
     *
     * Given: Usuario con ID no existe
     * When: PATCH /v1/usuarios/{id} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} lanza UsuarioNotFoundException cuando usuario no existe")
    void actualizarParcial_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Given
        int userId = 999;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Nuevo")
            .build();

        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.empty());

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.actualizarParcial(userId, request)
        );

        assertEquals("Usuario no encontrado: 999", exception.getMessage());
        verify(usuarioService, times(1)).actualizarParcial(userId, request);
    }

    /**
     * Test: Request vacío (ningún campo)
     *
     * Given: UpdateUsuarioRequest con todos los campos null
     * When: PATCH /v1/usuarios/{id} is called
     * Then: HTTP 200 OK (sin cambios)
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} permite request vacío (ningún campo)")
    void actualizarParcial_shouldAllow_whenRequestIsEmpty() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .build();

        User unchangedUser = new User(userId, "Juan", "pass", "juan@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(unchangedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Juan", response.getBody().getNombre());
    }

    /**
     * Test: Múltiples campos parcialmente
     *
     * Given: UpdateUsuarioRequest con dos campos
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Dos campos son actualizados
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} actualiza múltiples campos parcialmente")
    void actualizarParcial_shouldUpdateMultipleFields() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Nuevo Nombre")
            .email("nuevo@example.com")
            .build();

        User updatedUser = new User(userId, "Nuevo Nombre", "oldpass", "nuevo@example.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nuevo Nombre", response.getBody().getNombre());
        assertEquals("nuevo@example.com", response.getBody().getEmail());
    }

    /**
     * Test: Request sin validación @Valid
     *
     * Given: PATCH endpoint (no tiene @Valid)
     * When: Request sin validación de estructura
     * Then: Permitido (PATCH no requiere validación estricta)
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} no requiere @Valid en request")
    void actualizarParcial_shouldNotRequireValidAnnotation() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("A")  // Muy corto, pero PATCH lo permite
            .build();

        User updatedUser = new User(userId, "A", "pass", "juan@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test: Response contiene todos los campos actuales
     *
     * Given: Actualización parcial exitosa
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Response contiene estado completo del usuario
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} retorna response completo con estado actual")
    void actualizarParcial_shouldReturnCompleteResponse() {
        // Given
        int userId = 42;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Carlos Actualizado")
            .build();

        User updatedUser = new User(userId, "Carlos Actualizado", "oldpass", "carlos@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        UsuarioResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(userId, body.getId());
        assertEquals("Carlos Actualizado", body.getNombre());
        assertEquals("carlos@test.com", body.getEmail());
        assertTrue(body.isActivo());
    }

    /**
     * Test: Service es invocado correctamente
     *
     * Given: UpdateUsuarioRequest válido
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Service.actualizarParcial es invocado con parámetros correctos
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} invoca service.actualizarParcial() correctamente")
    void actualizarParcial_shouldCallServiceWithCorrectParameters() {
        // Given
        int userId = 5;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("Test")
            .build();

        User updatedUser = new User(userId, "Test", "pass", "test@test.com", true);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(updatedUser));

        // When
        controller.actualizarParcial(userId, request);

        // Then
        verify(usuarioService, times(1)).actualizarParcial(userId, request);
        verifyNoMoreInteractions(usuarioService);
    }

    /**
     * Test: Diferencia semántica entre PUT y PATCH
     *
     * Given: PATCH vs PUT con mismo request
     * When: Ambos endpoints son llamados
     * Then: PATCH no requiere todos los campos (PUT sí)
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} permite actualización sin campos requeridos")
    void actualizarParcial_shouldAllowPartialUpdate_UnlikeFullUpdatePUT() {
        // Given: Request con solo nombre (campos vacíos permitidos en PATCH)
        int userId = 1;
        UpdateUsuarioRequest partialRequest = UpdateUsuarioRequest.builder()
            .nombre("Solo Nombre")
            .build();

        User updatedUser = new User(userId, "Solo Nombre", "oldpass", "old@test.com", true);
        when(usuarioService.actualizarParcial(userId, partialRequest))
            .thenReturn(Optional.of(updatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, partialRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Solo Nombre", response.getBody().getNombre());
    }

    /**
     * Test: Desactivar usuario parcialmente
     *
     * Given: UpdateUsuarioRequest solo desactiva usuario
     * When: PATCH /v1/usuarios/{id} is called
     * Then: Solo estado activo cambia
     */
    @Test
    @DisplayName("PATCH /v1/usuarios/{id} puede desactivar usuario sin otros cambios")
    void actualizarParcial_shouldDeactivateWithoutOtherChanges() {
        // Given
        int userId = 1;
        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .activo(false)
            .build();

        User deactivatedUser = new User(userId, "Juan", "pass", "juan@test.com", false);
        when(usuarioService.actualizarParcial(userId, request))
            .thenReturn(Optional.of(deactivatedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.actualizarParcial(userId, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActivo());
        assertEquals("Juan", response.getBody().getNombre());  // sin cambios
    }
}

