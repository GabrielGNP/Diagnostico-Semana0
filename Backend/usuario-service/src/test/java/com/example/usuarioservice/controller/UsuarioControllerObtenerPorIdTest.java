package com.example.usuarioservice.controller;

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
 * Unit tests for UsuarioController.obtenerPorIdentificador() endpoint (GET /{id}).
 *
 * Tests verify correct HTTP status codes for different identifier types (ID, email, invalid).
 * Covers HU-USR-02 requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - GET /v1/usuarios/{identificador} endpoint")
class UsuarioControllerObtenerPorIdTest {

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private UsuarioController controller;

    /**
     * Test: Obtener usuario por ID numérico
     *
     * Given: Identificador es un número válido (ID)
     * When: GET /v1/usuarios/{id} is called
     * Then: HTTP 200 OK con usuario response
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} obtiene usuario por ID y devuelve 200 OK")
    void obtenerPorIdentificador_shouldReturn200OK_whenIdentifierIsValidId() {
        // Given
        String identificador = "1";
        User expectedUser = new User(1, "Juan", "pass", "juan@test.com", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(expectedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Juan", response.getBody().getNombre());
        assertEquals("juan@test.com", response.getBody().getEmail());

        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
    }

    /**
     * Test: Obtener usuario por email válido
     *
     * Given: Identificador es un email válido
     * When: GET /v1/usuarios/{email} is called
     * Then: HTTP 200 OK con usuario response
     */
    @Test
    @DisplayName("GET /v1/usuarios/{email} obtiene usuario por email y devuelve 200 OK")
    void obtenerPorIdentificador_shouldReturn200OK_whenIdentifierIsValidEmail() {
        // Given
        String identificador = "maria@example.com";
        User expectedUser = new User(2, "María", "pass", "maria@example.com", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(expectedUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getId());
        assertEquals("María", response.getBody().getNombre());
        assertEquals("maria@example.com", response.getBody().getEmail());

        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
    }

    /**
     * Test: Usuario no encontrado por ID
     *
     * Given: Identificador (ID) no existe en base de datos
     * When: GET /v1/usuarios/{id} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} lanza UsuarioNotFoundException cuando usuario no existe")
    void obtenerPorIdentificador_shouldThrowNotFoundException_whenIdDoesNotExist() {
        // Given
        String identificador = "999";
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.empty());

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.obtenerPorIdentificador(identificador)
        );

        assertEquals("Usuario no encontrado: 999", exception.getMessage());
        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
    }

    /**
     * Test: Usuario no encontrado por email
     *
     * Given: Identificador (email) no existe en base de datos
     * When: GET /v1/usuarios/{email} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("GET /v1/usuarios/{email} lanza UsuarioNotFoundException cuando email no existe")
    void obtenerPorIdentificador_shouldThrowNotFoundException_whenEmailDoesNotExist() {
        // Given
        String identificador = "inexistente@example.com";
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.empty());

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.obtenerPorIdentificador(identificador)
        );

        assertEquals("Usuario no encontrado: inexistente@example.com", exception.getMessage());
        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
    }

    /**
     * Test: Identificador inválido (no es número ni email)
     *
     * Given: Identificador no es ni ID numérico ni email válido
     * When: GET /v1/usuarios/{invalidId} is called
     * Then: UsuarioNotFoundException (404 Not Found)
     */
    @Test
    @DisplayName("GET /v1/usuarios/{invalid} lanza UsuarioNotFoundException cuando identificador es inválido")
    void obtenerPorIdentificador_shouldThrowNotFoundException_whenIdentifierIsInvalid() {
        // Given
        String identificador = "abc123xyz";
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.empty());

        // When & Then
        UsuarioNotFoundException exception = assertThrows(
            UsuarioNotFoundException.class,
            () -> controller.obtenerPorIdentificador(identificador)
        );

        assertEquals("Usuario no encontrado: abc123xyz", exception.getMessage());
        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
    }

    /**
     * Test: Response contiene todos los campos
     *
     * Given: Usuario existe con todos los datos
     * When: GET /v1/usuarios/{id} is called
     * Then: Response contiene id, nombre, email, activo
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} retorna response completo con todos los campos")
    void obtenerPorIdentificador_shouldReturnCompleteResponse() {
        // Given
        String identificador = "42";
        User user = new User(42, "Carlos López", "password", "carlos@example.com", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(user));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        UsuarioResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(42, body.getId());
        assertEquals("Carlos López", body.getNombre());
        assertEquals("carlos@example.com", body.getEmail());
        assertTrue(body.isActivo());
    }

    /**
     * Test: Usuario inactivo es retornado correctamente
     *
     * Given: Usuario existe pero está inactivo (active=false)
     * When: GET /v1/usuarios/{id} is called
     * Then: Response muestra activo=false
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} retorna usuario inactivo correctamente")
    void obtenerPorIdentificador_shouldReturnInactiveUserCorrectly() {
        // Given
        String identificador = "3";
        User inactiveUser = new User(3, "Inactivo", "pass", "inactivo@test.com", false);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(inactiveUser));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isActivo());
    }

    /**
     * Test: Service es invocado con identificador correcto
     *
     * Given: Identificador válido
     * When: GET /v1/usuarios/{id} is called
     * Then: Service.obtenerPorIdentificador es invocado exactamente una vez
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} invoca service.obtenerPorIdentificador() exactamente una vez")
    void obtenerPorIdentificador_shouldCallServiceOnce() {
        // Given
        String identificador = "5";
        User user = new User(5, "Test", "pass", "test@test.com", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(user));

        // When
        controller.obtenerPorIdentificador(identificador);

        // Then
        verify(usuarioService, times(1)).obtenerPorIdentificador(identificador);
        verifyNoMoreInteractions(usuarioService);
    }

    /**
     * Test: Números grandes son tratados como IDs válidos
     *
     * Given: Identificador es un número muy grande
     * When: GET /v1/usuarios/{largeId} is called
     * Then: Service es invocado (parsing es responsabilidad del servicio)
     */
    @Test
    @DisplayName("GET /v1/usuarios/{id} maneja números grandes correctamente")
    void obtenerPorIdentificador_shouldHandleLargeNumbers() {
        // Given
        String identificador = "2147483647";  // Integer.MAX_VALUE
        User user = new User(2147483647, "BigId", "pass", "big@test.com", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(user));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2147483647, response.getBody().getId());
    }

    /**
     * Test: Email con caracteres especiales
     *
     * Given: Identificador es email con caracteres especiales
     * When: GET /v1/usuarios/{email} is called
     * Then: Service es invocado correctamente
     */
    @Test
    @DisplayName("GET /v1/usuarios/{email} maneja emails con caracteres especiales")
    void obtenerPorIdentificador_shouldHandleEmailsWithSpecialChars() {
        // Given
        String identificador = "user+test@example.co.uk";
        User user = new User(10, "Special", "pass", "user+test@example.co.uk", true);
        when(usuarioService.obtenerPorIdentificador(identificador))
            .thenReturn(Optional.of(user));

        // When
        ResponseEntity<UsuarioResponse> response = controller.obtenerPorIdentificador(identificador);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user+test@example.co.uk", response.getBody().getEmail());
    }
}

