package com.example.usuarioservice.service;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuarioService.obtenerPorIdentificador() method.
 *
 * Tests verify the service correctly identifies and searches users by ID or email.
 * Covers HU-USR-02 requirements for identifier-based lookup.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - obtenerPorIdentificador() method")
class UsuarioServiceObtenerPorIdentificadorTest {

    @Mock
    private IUserPersistence userRepository;

    @Mock
    private ValidationContext validationContext;

    @InjectMocks
    private UsuarioService usuarioService;

    /**
     * Test: Buscar por ID válido
     *
     * Given: Identificador es un número (ID)
     * When: obtenerPorIdentificador is called
     * Then: Searches by ID and returns user
     */
    @Test
    @DisplayName("obtenerPorIdentificador busca por ID cuando identificador es numérico")
    void obtenerPorIdentificador_shouldSearchById_whenIdentifierIsNumeric() {
        // Given
        String identificador = "42";
        User expectedUser = new User(42, "Test", "pass", "test@example.com", true);
        when(userRepository.findById(42)).thenReturn(expectedUser);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertEquals(42, result.get().getId());
        assertEquals("test@example.com", result.get().getMail());

        verify(userRepository, times(1)).findById(42);
        verify(userRepository, never()).findByEmail(anyString());
    }

    /**
     * Test: Buscar por email válido
     *
     * Given: Identificador contiene @ (email)
     * When: obtenerPorIdentificador is called
     * Then: Searches by email and returns user
     */
    @Test
    @DisplayName("obtenerPorIdentificador busca por email cuando identificador contiene @")
    void obtenerPorIdentificador_shouldSearchByEmail_whenIdentifierContainsAt() {
        // Given
        String identificador = "user@example.com";
        User expectedUser = new User(1, "Test", "pass", "user@example.com", true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(expectedUser);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertEquals("user@example.com", result.get().getMail());

        verify(userRepository, times(1)).findByEmail("user@example.com");
        verify(userRepository, never()).findById(anyInt());
    }

    /**
     * Test: Identificador null retorna vacío
     *
     * Given: Identificador es null
     * When: obtenerPorIdentificador is called
     * Then: Returns empty Optional without calling repository
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna vacío cuando identificador es null")
    void obtenerPorIdentificador_shouldReturnEmpty_whenIdentifierIsNull() {
        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(null);

        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(userRepository);
    }

    /**
     * Test: Identificador vacío retorna vacío
     *
     * Given: Identificador es blank (vacío/espacios)
     * When: obtenerPorIdentificador is called
     * Then: Returns empty Optional without calling repository
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna vacío cuando identificador está vacío")
    void obtenerPorIdentificador_shouldReturnEmpty_whenIdentifierIsBlank() {
        // When
        Optional<User> result1 = usuarioService.obtenerPorIdentificador("");
        Optional<User> result2 = usuarioService.obtenerPorIdentificador("   ");

        // Then
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        verifyNoInteractions(userRepository);
    }

    /**
     * Test: Identificador no numérico ni email retorna vacío
     *
     * Given: Identificador no es ni email ni número válido
     * When: obtenerPorIdentificador is called
     * Then: Returns empty Optional (tryParseId fails)
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna vacío cuando identificador es inválido")
    void obtenerPorIdentificador_shouldReturnEmpty_whenIdentifierIsInvalid() {
        // Given
        String invalidId = "abc123xyz";

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(invalidId);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, never()).findById(anyInt());
        verify(userRepository, never()).findByEmail(anyString());
    }

    /**
     * Test: Usuario no encontrado por ID
     *
     * Given: ID válido pero usuario no existe
     * When: obtenerPorIdentificador is called
     * Then: Returns empty Optional
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna vacío cuando ID no existe")
    void obtenerPorIdentificador_shouldReturnEmpty_whenIdDoesNotExist() {
        // Given
        String identificador = "999";
        when(userRepository.findById(999)).thenReturn(null);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(999);
    }

    /**
     * Test: Usuario no encontrado por email
     *
     * Given: Email válido pero usuario no existe
     * When: obtenerPorIdentificador is called
     * Then: Returns empty Optional
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna vacío cuando email no existe")
    void obtenerPorIdentificador_shouldReturnEmpty_whenEmailDoesNotExist() {
        // Given
        String identificador = "noexiste@example.com";
        when(userRepository.findByEmail("noexiste@example.com")).thenReturn(null);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail("noexiste@example.com");
    }

    /**
     * Test: Email con caracteres especiales
     *
     * Given: Email con + y subdominios
     * When: obtenerPorIdentificador is called
     * Then: Searches correctly by email
     */
    @Test
    @DisplayName("obtenerPorIdentificador maneja emails con caracteres especiales")
    void obtenerPorIdentificador_shouldHandleSpecialCharsInEmail() {
        // Given
        String identificador = "user+test@subdomain.example.com";
        User expectedUser = new User(1, "Test", "pass", identificador, true);
        when(userRepository.findByEmail(identificador)).thenReturn(expectedUser);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertEquals(identificador, result.get().getMail());
        verify(userRepository, times(1)).findByEmail(identificador);
    }

    /**
     * Test: ID numérico muy grande
     *
     * Given: Identificador es Integer.MAX_VALUE
     * When: obtenerPorIdentificador is called
     * Then: Parses correctly and searches
     */
    @Test
    @DisplayName("obtenerPorIdentificador maneja IDs grandes (Integer.MAX_VALUE)")
    void obtenerPorIdentificador_shouldHandleLargeIds() {
        // Given
        String identificador = String.valueOf(Integer.MAX_VALUE);
        User expectedUser = new User(Integer.MAX_VALUE, "BigId", "pass", "big@test.com", true);
        when(userRepository.findById(Integer.MAX_VALUE)).thenReturn(expectedUser);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertEquals(Integer.MAX_VALUE, result.get().getId());
    }

    /**
     * Test: ID cero es válido (buscar por ID 0)
     *
     * Given: Identificador es "0"
     * When: obtenerPorIdentificador is called
     * Then: Searches by ID 0
     */
    @Test
    @DisplayName("obtenerPorIdentificador acepta ID cero como válido")
    void obtenerPorIdentificador_shouldAcceptZeroId() {
        // Given
        String identificador = "0";
        when(userRepository.findById(0)).thenReturn(null);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(0);
    }

    /**
     * Test: Identificador con solo @ (edge case)
     *
     * Given: Identificador es solo "@"
     * When: obtenerPorIdentificador is called
     * Then: Treats as email and searches
     */
    @Test
    @DisplayName("obtenerPorIdentificador trata '@' como email")
    void obtenerPorIdentificador_shouldTreatAtSignAsEmail() {
        // Given
        String identificador = "@";
        when(userRepository.findByEmail("@")).thenReturn(null);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail("@");
    }

    /**
     * Test: Usuario inactivo es retornado
     *
     * Given: Usuario existe pero está inactivo
     * When: obtenerPorIdentificador is called
     * Then: Returns inactive user (filtering is done in obtenerTodos)
     */
    @Test
    @DisplayName("obtenerPorIdentificador retorna usuario inactivo (sin filtrar)")
    void obtenerPorIdentificador_shouldReturnInactiveUser() {
        // Given
        String identificador = "5";
        User inactiveUser = new User(5, "Inactivo", "pass", "inactive@test.com", false);
        when(userRepository.findById(5)).thenReturn(inactiveUser);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    /**
     * Test: Identificador con espacios al inicio/fin
     *
     * Given: Identificador tiene espacios
     * When: obtenerPorIdentificador is called (after trim by controller)
     * Then: Processes correctly (assuming trimmed by layer above)
     */
    @Test
    @DisplayName("obtenerPorIdentificador asume identificador está trimmed")
    void obtenerPorIdentificador_assumesIdentifierIsTrimmed() {
        // Given: Identifier with meaningful content (controller should trim)
        String identificador = "123";
        User user = new User(123, "Test", "pass", "test@test.com", true);
        when(userRepository.findById(123)).thenReturn(user);

        // When
        Optional<User> result = usuarioService.obtenerPorIdentificador(identificador);

        // Then
        assertTrue(result.isPresent());
        assertEquals(123, result.get().getId());
    }
}

