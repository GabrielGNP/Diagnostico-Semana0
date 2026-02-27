package com.example.usuarioservice.persistence;

import com.example.usuarioservice.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository (JSON persistence).
 * Tests file loading, CRUD operations, and error handling.
 */
@DisplayName("UserRepository - JSON persistence")
class UserRepositoryAdditionalTest {

    private UserRepository repository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        repository = new UserRepository();
        Path tempFile = tempDir.resolve("users-test.json");
        repository.setFilePathForTests(tempFile.toString());
        repository.initialize();
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // no-op: tempDir cleanup handled by JUnit
    }

    @Test
    @DisplayName("save assigns auto-incremental ID when ID is null")
    void save_shouldAssignAutoIncrementalId_whenIdIsNull() throws IOException {
        // Given
        User user1 = new User(null, "User1", "pass1", "user1@test.com", true);
        User user2 = new User(null, "User2", "pass2", "user2@test.com", true);

        // When
        User saved1 = repository.save(user1);
        User saved2 = repository.save(user2);

        // Then
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertTrue(saved2.getId() > saved1.getId(), "IDs should be auto-incremental");
    }

    @Test
    @DisplayName("save assigns auto-incremental ID when ID is zero")
    void save_shouldAssignAutoIncrementalId_whenIdIsZero() throws IOException {
        // Given
        User user = new User(0, "User", "pass", "user@test.com", true);

        // When
        User saved = repository.save(user);

        // Then
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }

    @Test
    @DisplayName("save preserves existing ID when ID is positive")
    void save_shouldPreserveId_whenIdIsPositive() throws IOException {
        // Given
        User user = new User(100, "User", "pass", "user@test.com", true);

        // When
        User saved = repository.save(user);

        // Then
        assertEquals(100, saved.getId());
    }

    @Test
    @DisplayName("update with non-existing user creates new entry")
    void update_shouldCreateNewEntry_whenUserDoesNotExist() throws IOException {
        // Given
        User user = new User(999, "NewUser", "pass", "new@test.com", true);

        // When
        User updated = repository.update(999, user);

        // Then
        assertNotNull(updated);
        assertEquals(999, updated.getId());
        assertEquals("NewUser", updated.getName());

        // Verify it was actually saved
        User found = repository.findById(999);
        assertNotNull(found);
        assertEquals("NewUser", found.getName());
    }

    @Test
    @DisplayName("partialUpdate with empty map returns user unchanged")
    void partialUpdate_shouldReturnUnchanged_whenMapIsEmpty() throws IOException {
        // Given
        User user = new User(1, "Original", "pass", "original@test.com", true);
        repository.save(user);

        Map<String, Object> emptyUpdates = new HashMap<>();

        // When
        User updated = repository.partialUpdate(1, emptyUpdates);

        // Then
        assertNotNull(updated);
        assertEquals("Original", updated.getName());
        assertEquals("original@test.com", updated.getMail());
    }

    @Test
    @DisplayName("partialUpdate with non-existing ID returns null")
    void partialUpdate_shouldReturnNull_whenUserDoesNotExist() throws IOException {
        // Given
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "NewName");

        // When
        User updated = repository.partialUpdate(999, updates);

        // Then
        assertNull(updated);
    }

    @Test
    @DisplayName("partialUpdate updates only provided fields")
    void partialUpdate_shouldUpdateOnlyProvidedFields() throws IOException {
        // Given
        User user = new User(1, "Original", "oldpass", "original@test.com", true);
        repository.save(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated");

        // When
        User updated = repository.partialUpdate(1, updates);

        // Then
        assertNotNull(updated);
        assertEquals("Updated", updated.getName());
        assertEquals("oldpass", updated.getPassword()); // Should remain unchanged
        assertEquals("original@test.com", updated.getMail()); // Should remain unchanged
    }

    @Test
    @DisplayName("partialUpdate handles active field as Boolean")
    void partialUpdate_shouldHandleActiveAsBoolean() throws IOException {
        // Given
        User user = new User(1, "User", "pass", "user@test.com", true);
        repository.save(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("active", false);

        // When
        User updated = repository.partialUpdate(1, updates);

        // Then
        assertNotNull(updated);
        assertFalse(updated.isActive());
    }

    @Test
    @DisplayName("partialUpdate handles active field as String")
    void partialUpdate_shouldHandleActiveAsString() throws IOException {
        // Given
        User user = new User(1, "User", "pass", "user@test.com", true);
        repository.save(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("active", "false");

        // When
        User updated = repository.partialUpdate(1, updates);

        // Then
        assertNotNull(updated);
        assertFalse(updated.isActive());
    }

    @Test
    @DisplayName("findByEmail returns null when email is null")
    void findByEmail_shouldReturnNull_whenEmailIsNull() throws IOException {
        // When
        User found = repository.findByEmail(null);

        // Then
        assertNull(found);
    }

    @Test
    @DisplayName("findByEmail is case-insensitive")
    void findByEmail_shouldBeCaseInsensitive() throws IOException {
        // Given
        User user = new User(1, "User", "pass", "User@Test.COM", true);
        repository.save(user);

        // When
        User found = repository.findByEmail("user@test.com");

        // Then
        assertNotNull(found);
        assertEquals(1, found.getId());
    }

    @Test
    @DisplayName("findAllActive excludes inactive users")
    void findAllActive_shouldExcludeInactiveUsers() throws IOException {
        // Given
        User activeUser = new User(1, "Active", "pass", "active@test.com", true);
        User inactiveUser = new User(2, "Inactive", "pass", "inactive@test.com", false);
        repository.save(activeUser);
        repository.save(inactiveUser);

        // When
        Collection<User> activeUsers = repository.findAllActive();

        // Then
        assertEquals(1, activeUsers.size());
        assertTrue(activeUsers.stream().allMatch(User::isActive));
    }

    @Test
    @DisplayName("deleteById returns false when user does not exist")
    void deleteById_shouldReturnFalse_whenUserDoesNotExist() throws IOException {
        // When
        boolean deleted = repository.deleteById(999);

        // Then
        assertFalse(deleted);
    }

    @Test
    @DisplayName("deleteById removes user and returns true when user exists")
    void deleteById_shouldRemoveUserAndReturnTrue_whenUserExists() throws IOException {
        // Given
        User user = new User(1, "User", "pass", "user@test.com", true);
        repository.save(user);

        // When
        boolean deleted = repository.deleteById(1);

        // Then
        assertTrue(deleted);
        assertNull(repository.findById(1));
    }

    @Test
    @DisplayName("save persists multiple users correctly")
    void save_shouldPersistMultipleUsers() throws IOException {
        // Given
        User user1 = new User(null, "User1", "pass1", "user1@test.com", true);
        User user2 = new User(null, "User2", "pass2", "user2@test.com", true);
        User user3 = new User(null, "User3", "pass3", "user3@test.com", false);

        // When
        repository.save(user1);
        repository.save(user2);
        repository.save(user3);

        // Then
        Collection<User> all = repository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("findAll returns all users including inactive")
    void findAll_shouldReturnAllUsersIncludingInactive() throws IOException {
        // Given
        User activeUser = new User(1, "Active", "pass", "active@test.com", true);
        User inactiveUser = new User(2, "Inactive", "pass", "inactive@test.com", false);
        repository.save(activeUser);
        repository.save(inactiveUser);

        // When
        Collection<User> all = repository.findAll();

        // Then
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(u -> !u.isActive()));
    }

    @Test
    @DisplayName("initialize creates file when none exists")
    void initialize_shouldCreateFile_whenNoneExists() throws IOException {
        // Given
        UserRepository freshRepository = new UserRepository();
        Path tempFile = tempDir.resolve("users-empty.json");
        freshRepository.setFilePathForTests(tempFile.toString());

        // When
        freshRepository.initialize();

        // Then - Should not throw exception
        assertTrue(java.nio.file.Files.exists(tempFile));
    }

    @Test
    @DisplayName("update overwrites all fields")
    void update_shouldOverwriteAllFields() throws IOException {
        // Given
        User original = new User(1, "Original", "oldpass", "old@test.com", true);
        repository.save(original);

        User updated = new User(1, "New", "newpass", "new@test.com", false);

        // When
        User result = repository.update(1, updated);

        // Then
        assertEquals("New", result.getName());
        assertEquals("newpass", result.getPassword());
        assertEquals("new@test.com", result.getMail());
        assertFalse(result.isActive());
    }

    @Test
    @DisplayName("findById returns null when user does not exist")
    void findById_shouldReturnNull_whenUserDoesNotExist() throws IOException {
        // When
        User found = repository.findById(999);

        // Then
        assertNull(found);
    }

    @Test
    @DisplayName("findByEmail returns null when no user has that email")
    void findByEmail_shouldReturnNull_whenNoUserHasThatEmail() throws IOException {
        // When
        User found = repository.findByEmail("nonexistent@test.com");

        // Then
        assertNull(found);
    }
}
