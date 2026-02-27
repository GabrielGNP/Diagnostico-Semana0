package com.example.usuarioservice.persistence;

import com.example.usuarioservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CachedUserPersistenceDecorator - caching behavior")
class CachedUserPersistenceDecoratorTest {

    @Mock
    private IUserPersistence delegate;

    private CachedUserPersistenceDecorator cachedDecorator;

    @BeforeEach
    void setUp() {
        cachedDecorator = new CachedUserPersistenceDecorator(delegate);
    }

    @Test
    @DisplayName("findById cache miss calls delegate")
    void findById_cacheMiss_shouldCallDelegate() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.findById(1)).thenReturn(user);

        User result = cachedDecorator.findById(1);

        assertEquals(user, result);
        verify(delegate, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById cache hit does not call delegate")
    void findById_cacheHit_shouldNotCallDelegate() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.findById(1)).thenReturn(user);

        cachedDecorator.findById(1);
        User result = cachedDecorator.findById(1);

        assertEquals(user, result);
        verify(delegate, times(1)).findById(1);
    }

    @Test
    @DisplayName("findByEmail cache miss calls delegate")
    void findByEmail_cacheMiss_shouldCallDelegate() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.findByEmail("juan@test.com")).thenReturn(user);

        User result = cachedDecorator.findByEmail("juan@test.com");

        assertEquals(user, result);
        verify(delegate, times(1)).findByEmail("juan@test.com");
    }

    @Test
    @DisplayName("findByEmail cache hit does not call delegate")
    void findByEmail_cacheHit_shouldNotCallDelegate() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.findByEmail("juan@test.com")).thenReturn(user);

        cachedDecorator.findByEmail("juan@test.com");
        User result = cachedDecorator.findByEmail("juan@test.com");

        assertEquals(user, result);
        verify(delegate, times(1)).findByEmail("juan@test.com");
    }

    @Test
    @DisplayName("save invalidates cache")
    void save_shouldInvalidateCache() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.save(user)).thenReturn(user);

        cachedDecorator.save(user);

        verify(delegate, times(1)).save(user);
    }

    @Test
    @DisplayName("clearCache removes all entries")
    void clearCache_shouldRemoveAllEntries() {
        User user = new User(1, "Juan", "pass", "juan@test.com", true);
        when(delegate.findById(1)).thenReturn(user);

        cachedDecorator.findById(1);
        cachedDecorator.clearCache();
        cachedDecorator.findById(1);

        verify(delegate, times(2)).findById(1);
    }
}
