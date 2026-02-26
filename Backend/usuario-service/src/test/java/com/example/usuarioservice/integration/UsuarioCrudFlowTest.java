package com.example.usuarioservice.integration;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.service.UsuarioService;
import com.example.usuarioservice.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Integration tests for usuario-service CRUD operations.
 * Tests complete end-to-end workflows combining Service + Persistence.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Usuario Service - E2E CRUD flows")
class UsuarioCrudFlowTest {

    @Mock
    private IUserPersistence persistence;

    @Mock
    private ValidationContext validationContext;

    @Test
    @DisplayName("E2E: Complete CRUD flow - Create, Read, Update, Delete")
    void completeCrudFlow_shouldSucceed() {
        // Given: Setup service with mocks
        UsuarioService service = new UsuarioService(persistence, validationContext);

        // STEP 1: CREATE
        CreateUsuarioRequest createRequest = CreateUsuarioRequest.builder()
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .contrasena("Pass123")
            .build();

        User createdUser = new User(1, "Juan Pérez", "Pass123", "juan@example.com", true);
        when(persistence.findByEmail("juan@example.com")).thenReturn(null);
        when(persistence.save(any(User.class))).thenReturn(createdUser);

        User result1 = service.crear(createRequest);
        assertEquals(1, result1.getId());
        assertEquals("juan@example.com", result1.getMail());

        // STEP 2: READ by ID
        when(persistence.findById(1)).thenReturn(createdUser);
        Optional<User> result2 = service.obtenerPorIdentificador("1");
        assertTrue(result2.isPresent());
        assertEquals("Juan Pérez", result2.get().getName());

        // STEP 3: READ by EMAIL
        when(persistence.findByEmail("juan@example.com")).thenReturn(createdUser);
        Optional<User> result3 = service.obtenerPorIdentificador("juan@example.com");
        assertTrue(result3.isPresent());

        // STEP 4: UPDATE
        UpdateUsuarioRequest updateRequest = UpdateUsuarioRequest.builder()
            .nombre("Juan García")
            .email("juan.garcia@example.com")
            .build();

        User updatedUser = new User(1, "Juan García", "Pass123", "juan.garcia@example.com", true);
        when(persistence.findById(1)).thenReturn(createdUser);
        when(persistence.findByEmail("juan.garcia@example.com")).thenReturn(null);
        when(persistence.update(eq(1), any(User.class))).thenReturn(updatedUser);

        Optional<User> result4 = service.actualizar(1, updateRequest);
        assertTrue(result4.isPresent());
        assertEquals("Juan García", result4.get().getName());

        // STEP 5: DELETE
        when(persistence.deleteById(1)).thenReturn(true);
        boolean deleted = service.eliminar(1);
        assertTrue(deleted);

        verify(persistence).save(any(User.class));
        verify(persistence, atLeastOnce()).findById(anyInt());
        verify(persistence).update(eq(1), any(User.class));
        verify(persistence).deleteById(1);
    }

    @Test
    @DisplayName("E2E: Multiple users CRUD flow")
    void multipleUsersCrudFlow_shouldSucceed() {
        // Given
        UsuarioService service = new UsuarioService(persistence, validationContext);

        // Create user 1
        User user1 = new User(1, "User 1", "pass1", "user1@test.com", true);
        when(persistence.findByEmail("user1@test.com")).thenReturn(null);
        when(persistence.save(any())).thenReturn(user1);

        CreateUsuarioRequest request1 = CreateUsuarioRequest.builder()
            .nombre("User 1").email("user1@test.com").contrasena("pass1").build();
        User result1 = service.crear(request1);
        assertEquals(1, result1.getId());

        // Create user 2
        User user2 = new User(2, "User 2", "pass2", "user2@test.com", true);
        when(persistence.findByEmail("user2@test.com")).thenReturn(null);
        when(persistence.save(any())).thenReturn(user2);

        CreateUsuarioRequest request2 = CreateUsuarioRequest.builder()
            .nombre("User 2").email("user2@test.com").contrasena("pass2").build();
        User result2 = service.crear(request2);
        assertEquals(2, result2.getId());

        // Get all active users
        when(persistence.findAllActive()).thenReturn(Arrays.asList(user1, user2));
        Collection<User> allUsers = service.obtenerTodos();
        assertEquals(2, allUsers.size());
    }

    @Test
    @DisplayName("E2E: Error handling flow - Email conflict")
    void errorHandlingFlow_emailConflict_shouldThrowException() {
        // Given
        UsuarioService service = new UsuarioService(persistence, validationContext);

        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan").email("duplicate@test.com").contrasena("Pass123").build();

        User existingUser = new User(1, "Existing", "pass", "duplicate@test.com", true);
        when(persistence.findByEmail("duplicate@test.com")).thenReturn(existingUser);

        // When & Then
        assertThrows(com.example.usuarioservice.exception.UsuarioYaExisteException.class,
            () -> service.crear(request));
    }

    @Test
    @DisplayName("E2E: Partial update flow")
    void partialUpdateFlow_shouldUpdateSelectiveFields() {
        // Given
        UsuarioService service = new UsuarioService(persistence, validationContext);

        User existingUser = new User(1, "Old Name", "oldpass", "email@test.com", true);
        User updatedUser = new User(1, "New Name", "oldpass", "email@test.com", true);

        when(persistence.findById(1)).thenReturn(existingUser);
        when(persistence.update(eq(1), any())).thenReturn(updatedUser);

        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .nombre("New Name").build();

        // When
        Optional<User> result = service.actualizar(1, request);

        // Then
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals("oldpass", result.get().getPassword());
    }

    @Test
    @DisplayName("E2E: User not found flow")
    void userNotFoundFlow_shouldReturnEmpty() {
        // Given
        UsuarioService service = new UsuarioService(persistence, validationContext);

        when(persistence.findById(999)).thenReturn(null);
        when(persistence.findByEmail("notexist@test.com")).thenReturn(null);

        // When
        Optional<User> result1 = service.obtenerPorIdentificador("999");
        Optional<User> result2 = service.obtenerPorIdentificador("notexist@test.com");

        // Then
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }

    @Test
    @DisplayName("E2E: Deactivate user flow")
    void deactivateUserFlow_shouldMarkAsInactive() {
        // Given
        UsuarioService service = new UsuarioService(persistence, validationContext);

        User activeUser = new User(1, "User", "pass", "user@test.com", true);
        User inactiveUser = new User(1, "User", "pass", "user@test.com", false);

        when(persistence.findById(1)).thenReturn(activeUser);
        when(persistence.update(eq(1), any())).thenReturn(inactiveUser);

        UpdateUsuarioRequest request = UpdateUsuarioRequest.builder()
            .activo(false).build();

        // When
        Optional<User> result = service.actualizar(1, request);

        // Then
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    @DisplayName("E2E: Response DTO conversion flow")
    void responseDtoConversionFlow_shouldConvertCorrectly() {
        // Given
        User user = new User(1, "Juan", "pass", "juan@test.com", true);

        // When
        UsuarioResponse response = UsuarioResponse.from(user);

        // Then
        assertEquals(1, response.getId());
        assertEquals("Juan", response.getNombre());
        assertEquals("juan@test.com", response.getEmail());
        assertTrue(response.isActivo());
    }
}

