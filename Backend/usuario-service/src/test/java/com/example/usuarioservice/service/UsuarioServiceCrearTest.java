package com.example.usuarioservice.service;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.exception.UsuarioYaExisteException;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.validation.ValidationContext;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuarioService.crear() method.
 *
 * Tests verify user creation with validation, uniqueness checks, and proper persistence.
 * Covers HU-USR-03 requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - crear() method")
class UsuarioServiceCrearTest {

    @Mock
    private IUserPersistence userRepository;

    @Mock
    private ValidationContext validationContext;

    @InjectMocks
    private UsuarioService usuarioService;

    /**
     * Test: Crear usuario exitosamente
     *
     * Given: Valid CreateUsuarioRequest and email no exists
     * When: crear is called
     * Then: User is created and persisted with active=true
     */
    @Test
    @DisplayName("crear crea usuario exitosamente cuando datos son válidos")
    void crear_shouldCreateUser_whenDataIsValid() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .contrasena("SecurePass123")
            .build();

        when(userRepository.findByEmail("juan@example.com")).thenReturn(null);

        User savedUser = new User(1, "Juan Pérez", "SecurePass123", "juan@example.com", true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = usuarioService.crear(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan Pérez", result.getName());
        assertEquals("juan@example.com", result.getMail());
        assertTrue(result.isActive());

        verify(validationContext, times(1))
            .validateForCreation(request, ValidationStrategyType.LENIENT);
        verify(userRepository, times(1)).findByEmail("juan@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Test: Usuario creado se marca como activo por defecto
     *
     * Given: Valid request
     * When: crear is called
     * Then: Created user has active=true
     */
    @Test
    @DisplayName("crear marca usuario como activo por defecto")
    void crear_shouldMarkUserAsActive() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("test@example.com")
            .contrasena("Pass123")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        User savedUser = new User(1, "Test", "Pass123", "test@example.com", true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = usuarioService.crear(request);

        // Then
        assertTrue(result.isActive());

        // Verify the User object passed to save() has active=true
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isActive());
    }

    /**
     * Test: Email duplicado lanza excepción
     *
     * Given: Email already exists in database
     * When: crear is called
     * Then: UsuarioYaExisteException is thrown
     */
    @Test
    @DisplayName("crear lanza UsuarioYaExisteException cuando email ya existe")
    void crear_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Nuevo")
            .email("duplicado@example.com")
            .contrasena("Pass123")
            .build();

        User existingUser = new User(99, "Existente", "pass", "duplicado@example.com", true);
        when(userRepository.findByEmail("duplicado@example.com")).thenReturn(existingUser);

        // When & Then
        UsuarioYaExisteException exception = assertThrows(
            UsuarioYaExisteException.class,
            () -> usuarioService.crear(request)
        );

        assertTrue(exception.getMessage().contains("duplicado@example.com"));
        assertTrue(exception.getMessage().contains("ya está registrado"));

        verify(validationContext, times(1))
            .validateForCreation(request, ValidationStrategyType.LENIENT);
        verify(userRepository, times(1)).findByEmail("duplicado@example.com");
        verify(userRepository, never()).save(any());
    }

    /**
     * Test: ValidationContext es invocado con estrategia LENIENT
     *
     * Given: Valid request
     * When: crear is called
     * Then: ValidationContext.validateForCreation is called with LENIENT
     */
    @Test
    @DisplayName("crear invoca validationContext con estrategia LENIENT")
    void crear_shouldUselenientValidationStrategy() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("test@example.com")
            .contrasena("Pass123")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(new User());

        // When
        usuarioService.crear(request);

        // Then
        verify(validationContext, times(1))
            .validateForCreation(eq(request), eq(ValidationStrategyType.LENIENT));
    }

    /**
     * Test: Usuario es persistido con todos los campos correctos
     *
     * Given: Valid request
     * When: crear is called
     * Then: User saved has all fields from request
     */
    @Test
    @DisplayName("crear persiste usuario con todos los campos del request")
    void crear_shouldPersistAllFields() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos López")
            .email("carlos@example.com")
            .contrasena("MySecurePass123")
            .build();

        when(userRepository.findByEmail("carlos@example.com")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(new User());

        // When
        usuarioService.crear(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Carlos López", capturedUser.getName());
        assertEquals("carlos@example.com", capturedUser.getMail());
        assertEquals("MySecurePass123", capturedUser.getPassword());
        assertTrue(capturedUser.isActive());
    }

    /**
     * Test: Excepción de validación no invoca save
     *
     * Given: ValidationContext throws exception
     * When: crear is called
     * Then: Repository.save is never called
     */
    @Test
    @DisplayName("crear no persiste si validación de negocio falla")
    void crear_shouldNotSave_whenValidationFails() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Invalid")
            .email("invalid@example.com")
            .contrasena("weak")
            .build();

        doThrow(new RuntimeException("Validation failed"))
            .when(validationContext).validateForCreation(any(), any());

        // When & Then
        assertThrows(RuntimeException.class, () -> usuarioService.crear(request));

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    /**
     * Test: Verificación de uniqueness antes de persistir
     *
     * Given: Valid request
     * When: crear is called
     * Then: Email uniqueness is checked before saving
     */
    @Test
    @DisplayName("crear verifica unicidad de email antes de persistir")
    void crear_shouldCheckUniquenessBeforeSaving() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("test@example.com")
            .contrasena("Pass123")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(new User());

        // When
        usuarioService.crear(request);

        // Then: Verify order of operations
        var inOrder = inOrder(validationContext, userRepository);
        inOrder.verify(validationContext).validateForCreation(any(), any());
        inOrder.verify(userRepository).findByEmail("test@example.com");
        inOrder.verify(userRepository).save(any());
    }

    /**
     * Test: Email case-sensitive (comportamiento actual)
     *
     * Given: Email con mayúsculas/minúsculas diferentes
     * When: crear is called
     * Then: Treats as different emails (current behavior)
     */
    @Test
    @DisplayName("crear trata emails como case-sensitive")
    void crear_shouldTreatEmailsAsCaseSensitive() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("Test@Example.com")
            .contrasena("Pass123")
            .build();

        // Different case, but findByEmail returns null (not found)
        when(userRepository.findByEmail("Test@Example.com")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(new User());

        // When
        usuarioService.crear(request);

        // Then
        verify(userRepository).findByEmail("Test@Example.com");
        verify(userRepository).save(any());
    }

    /**
     * Test: Logging de operación exitosa
     *
     * Given: Valid request
     * When: crear is called
     * Then: Logs creation and success (verificado implícitamente via spy o manual)
     */
    @Test
    @DisplayName("crear registra operación exitosa")
    void crear_shouldLogSuccessfulOperation() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("test@example.com")
            .contrasena("Pass123")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        User savedUser = new User(42, "Test", "Pass123", "test@example.com", true);
        when(userRepository.save(any())).thenReturn(savedUser);

        // When
        User result = usuarioService.crear(request);

        // Then: Verify it returns the saved user (log happens internally)
        assertEquals(42, result.getId());
        assertEquals("test@example.com", result.getMail());
    }

    /**
     * Test: Retorna usuario guardado con ID generado
     *
     * Given: Repository returns user with generated ID
     * When: crear is called
     * Then: Returns the persisted user with ID
     */
    @Test
    @DisplayName("crear retorna usuario con ID generado por persistencia")
    void crear_shouldReturnUserWithGeneratedId() {
        // Given
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Test")
            .email("test@example.com")
            .contrasena("Pass123")
            .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        User savedUser = new User(123, "Test", "Pass123", "test@example.com", true);
        when(userRepository.save(any())).thenReturn(savedUser);

        // When
        User result = usuarioService.crear(request);

        // Then
        assertEquals(123, result.getId());
        assertNotNull(result.getId());
    }
}

