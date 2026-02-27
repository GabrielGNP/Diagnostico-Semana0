package com.example.usuarioservice.persistence;

import com.example.usuarioservice.entity.UserEntity;
import com.example.usuarioservice.mapper.UserEntityMapper;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.repository.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserJpaPersistence class with mocks.
 * Tests CRUD operations and persistence layer behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserJpaPersistence - CRUD operations")
class UserJpaPersistenceTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private UserEntityMapper mapper;

    @InjectMocks
    private UserJpaPersistence persistence;

    @Test
    @DisplayName("save persists user and returns domain model")
    void save_shouldPersistAndReturnDomainModel() {
        // Given
        User user = new User(null, "Juan", "pass", "juan@test.com", true);
        UserEntity entity = new UserEntity(null, "Juan", "pass", "juan@test.com", true);
        UserEntity saved = new UserEntity(1, "Juan", "pass", "juan@test.com", true);
        User domainResult = new User(1, "Juan", "pass", "juan@test.com", true);

        when(mapper.toEntity(user)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(domainResult);

        // When
        User result = persistence.save(user);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("juan@test.com", result.getMail());
        verify(mapper).toEntity(user);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(saved);
    }

    @Test
    @DisplayName("findById returns user when found")
    void findById_shouldReturnUser_whenFound() {
        // Given
        int userId = 1;
        UserEntity entity = new UserEntity(userId, "Test", "pass", "test@test.com", true);
        User expectedUser = new User(userId, "Test", "pass", "test@test.com", true);

        when(jpaRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedUser);

        // When
        User result = persistence.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(jpaRepository).findById(userId);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findById returns null when not found")
    void findById_shouldReturnNull_whenNotFound() {
        // Given
        int userId = 999;
        when(jpaRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        User result = persistence.findById(userId);

        // Then
        assertNull(result);
        verify(jpaRepository).findById(userId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByEmail returns user when found")
    void findByEmail_shouldReturnUser_whenFound() {
        // Given
        String email = "test@example.com";
        UserEntity entity = new UserEntity(1, "Test", "pass", email, true);
        User expectedUser = new User(1, "Test", "pass", email, true);

        when(jpaRepository.findByMailIgnoreCase(email)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expectedUser);

        // When
        User result = persistence.findByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getMail());
        verify(jpaRepository).findByMailIgnoreCase(email);
    }

    @Test
    @DisplayName("update modifies existing user")
    void update_shouldModifyExistingUser() {
        // Given
        int userId = 1;
        User updateUser = new User(userId, "Updated", "newpass", "new@test.com", false);
        UserEntity existing = new UserEntity(userId, "Old", "oldpass", "old@test.com", true);
        UserEntity updated = new UserEntity(userId, "Updated", "newpass", "new@test.com", false);
        User resultUser = new User(userId, "Updated", "newpass", "new@test.com", false);

        when(jpaRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(updated);
        when(mapper.toDomain(updated)).thenReturn(resultUser);

        // When
        User result = persistence.update(userId, updateUser);

        // Then
        assertNotNull(result);
        assertEquals("Updated", result.getName());
        assertEquals("new@test.com", result.getMail());
        verify(jpaRepository).findById(userId);
        verify(jpaRepository).save(existing);
    }

    @Test
    @DisplayName("deleteById removes user when exists")
    void deleteById_shouldRemoveUser_whenExists() {
        // Given
        int userId = 1;
        when(jpaRepository.existsById(userId)).thenReturn(true);

        // When
        boolean result = persistence.deleteById(userId);

        // Then
        assertTrue(result);
        verify(jpaRepository).existsById(userId);
        verify(jpaRepository).deleteById(userId);
    }

    @Test
    @DisplayName("deleteById returns false when user not found")
    void deleteById_shouldReturnFalse_whenUserNotFound() {
        // Given
        int userId = 999;
        when(jpaRepository.existsById(userId)).thenReturn(false);

        // When
        boolean result = persistence.deleteById(userId);

        // Then
        assertFalse(result);
        verify(jpaRepository).existsById(userId);
        verify(jpaRepository, never()).deleteById(userId);
    }
}

