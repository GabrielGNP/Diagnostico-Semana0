package com.example.usuarioservice.service;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.validation.ValidationContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU-USR-01: Unit tests for UsuarioService.obtenerTodos() method.
 * 
 * Tests verify that the service returns only active users by delegating to
 * the repository's findAllActive() method.
 * Based on TEST_PLAN.md scenarios EP-USR-01-01 and EP-USR-01-02.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceObtenerTodosTest {

    @Mock
    private IUserPersistence userRepository;
    
    @Mock
    private ValidationContext validationContext;

    @InjectMocks
    private UsuarioService usuarioService;

    /**
     * EP-USR-01-01: Listado exitoso con usuarios activos existentes.
     * 
     * Given: Repository returns active users
     * When: obtenerTodos() is called
     * Then: Active users are returned
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-01: Returns active users from repository")
    void obtenerTodos_shouldReturnActiveUsers_whenActiveUsersExist() {
        // Given: Repository returns active users only
        User activeUser1 = new User(1, "Active 1", "pass1", "active1@test.com", true);
        User activeUser2 = new User(2, "Active 2", "pass2", "active2@test.com", true);
        Collection<User> activeUsers = Arrays.asList(activeUser1, activeUser2);
        
        when(userRepository.findAllActive()).thenReturn(activeUsers);
        
        // When
        Collection<User> result = usuarioService.obtenerTodos();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAllActive();
    }

    /**
     * EP-USR-01-02: Listado vacío cuando no existen usuarios activos.
     * 
     * Given: Repository returns empty collection
     * When: obtenerTodos() is called
     * Then: Empty collection is returned
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-02: Returns empty collection when no active users")
    void obtenerTodos_shouldReturnEmptyCollection_whenNoActiveUsers() {
        // Given: Repository returns empty collection
        when(userRepository.findAllActive()).thenReturn(Collections.emptyList());
        
        // When
        Collection<User> result = usuarioService.obtenerTodos();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAllActive();
    }

    /**
     * Verify service does not expose soft-deleted users.
     * 
     * Given: Repository properly filters soft-deleted users
     * When: obtenerTodos() is called
     * Then: Only active users returned (soft-deleted excluded)
     */
    @Test
    @DisplayName("HU-USR-01 - Service returns only active users, no soft-deleted")
    void obtenerTodos_shouldNotReturnSoftDeletedUsers() {
        // Given: Only active users from repository (soft-deleted already filtered)
        User activeUser = new User(1, "Active", "pass1", "active@test.com", true);
        Collection<User> activeUsers = Collections.singletonList(activeUser);
        
        when(userRepository.findAllActive()).thenReturn(activeUsers);
        
        // When
        Collection<User> result = usuarioService.obtenerTodos();
        
        // Then: All returned users are active
        assertNotNull(result);
        assertEquals(1, result.size());
        for (User user : result) {
            assertTrue(user.isActive(), "All returned users must be active");
        }
    }
}
