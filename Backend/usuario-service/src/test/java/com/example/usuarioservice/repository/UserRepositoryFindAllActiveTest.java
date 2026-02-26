package com.example.usuarioservice.repository;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.UserRepository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HU-USR-01: Tests for findAllActive() repository method.
 * 
 * Tests verify that only active users (not soft-deleted) are returned.
 * Based on TEST_PLAN.md scenarios EP-USR-01-01 and EP-USR-01-02.
 */
class UserRepositoryFindAllActiveTest {

    private UserRepository userRepository;
    private final String testFilePath = "test-users-active.json";

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        // Clean test file
        File file = new File(testFilePath);
        if (file.exists()) file.delete();
    }

    @AfterEach
    void tearDown() {
        File file = new File(testFilePath);
        if (file.exists()) file.delete();
    }

    /**
     * EP-USR-01-01: Listado exitoso con usuarios activos existentes.
     * 
     * Given: Multiple users exist - some active, some soft-deleted (active=false)
     * When: findAllActive() is called
     * Then: Only active users are returned
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-01: Returns only active users, filtering soft-deleted")
    void findAllActive_shouldReturnOnlyActiveUsers_whenMixedActiveAndInactiveExist() {
        // Given: 3 active users and 2 soft-deleted users
        userRepository.save(new User(1, "Active User 1", "pass1", "active1@test.com", true));
        userRepository.save(new User(2, "Active User 2", "pass2", "active2@test.com", true));
        userRepository.save(new User(3, "Active User 3", "pass3", "active3@test.com", true));
        userRepository.save(new User(4, "Deleted User 1", "pass4", "deleted1@test.com", false)); // soft-deleted
        userRepository.save(new User(5, "Deleted User 2", "pass5", "deleted2@test.com", false)); // soft-deleted
        
        // When
        Collection<User> activeUsers = userRepository.findAllActive();
        
        // Then: Only 3 active users returned
        assertNotNull(activeUsers);
        assertEquals(3, activeUsers.size());
        
        // Verify all returned users are active
        for (User user : activeUsers) {
            assertTrue(user.isActive(), "User " + user.getName() + " should be active");
        }
    }

    /**
     * EP-USR-01-02: Listado vacío cuando no existen usuarios activos.
     * 
     * Given: Only soft-deleted users exist
     * When: findAllActive() is called
     * Then: Empty collection is returned
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-02: Returns empty collection when only soft-deleted users exist")
    void findAllActive_shouldReturnEmptyCollection_whenOnlySoftDeletedUsersExist() {
        // Given: Only soft-deleted users
        userRepository.save(new User(1, "Deleted User 1", "pass1", "deleted1@test.com", false));
        userRepository.save(new User(2, "Deleted User 2", "pass2", "deleted2@test.com", false));
        
        // When
        Collection<User> activeUsers = userRepository.findAllActive();
        
        // Then: Empty collection
        assertNotNull(activeUsers);
        assertTrue(activeUsers.isEmpty(), "Should return empty collection when no active users exist");
    }

    /**
     * EP-USR-01-02: Listado vacío cuando no existen usuarios.
     * 
     * Given: No users in the system
     * When: findAllActive() is called
     * Then: Empty collection is returned
     */
    @Test
    @DisplayName("HU-USR-01 - EP-USR-01-02: Returns empty collection when no users exist")
    void findAllActive_shouldReturnEmptyCollection_whenNoUsersExist() {
        // Given: No users
        
        // When
        Collection<User> activeUsers = userRepository.findAllActive();
        
        // Then: Empty collection
        assertNotNull(activeUsers);
        assertTrue(activeUsers.isEmpty(), "Should return empty collection when no users exist");
    }

    /**
     * Edge case: All users are active.
     * 
     * Given: All users are active (no soft-deletes)
     * When: findAllActive() is called
     * Then: All users are returned
     */
    @Test
    @DisplayName("HU-USR-01 - All users active: Returns complete list")
    void findAllActive_shouldReturnAllUsers_whenAllAreActive() {
        // Given: All active users
        userRepository.save(new User(1, "User 1", "pass1", "user1@test.com", true));
        userRepository.save(new User(2, "User 2", "pass2", "user2@test.com", true));
        userRepository.save(new User(3, "User 3", "pass3", "user3@test.com", true));
        
        // When
        Collection<User> activeUsers = userRepository.findAllActive();
        
        // Then: All 3 users returned
        assertNotNull(activeUsers);
        assertEquals(3, activeUsers.size());
    }
}
