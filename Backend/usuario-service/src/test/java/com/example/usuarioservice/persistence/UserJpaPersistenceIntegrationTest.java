package com.example.usuarioservice.persistence;

import com.example.usuarioservice.entity.UserEntity;
import com.example.usuarioservice.mapper.UserEntityMapper;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserJpaPersistence.
 * Uses H2 in-memory database via SpringBootTest with test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserJpaPersistenceIntegrationTest {

    @Autowired
    private UserJpaPersistence userJpaPersistence;

    @Autowired
    private UserJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("HU-USR-01: findAllActive() - List only active users")
    class FindAllActiveTests {

        @Test
        @DisplayName("should return only active users excluding soft-deleted ones")
        void findAllActive_ShouldReturnOnlyActiveUsers() {
            // Given: 3 active users and 2 inactive (soft-deleted) users
            jpaRepository.save(UserEntity.builder()
                    .name("Active User 1").password("pass1").mail("active1@test.com").active(true).build());
            jpaRepository.save(UserEntity.builder()
                    .name("Active User 2").password("pass2").mail("active2@test.com").active(true).build());
            jpaRepository.save(UserEntity.builder()
                    .name("Active User 3").password("pass3").mail("active3@test.com").active(true).build());
            jpaRepository.save(UserEntity.builder()
                    .name("Deleted User 1").password("pass4").mail("deleted1@test.com").active(false).build());
            jpaRepository.save(UserEntity.builder()
                    .name("Deleted User 2").password("pass5").mail("deleted2@test.com").active(false).build());

            // When
            Collection<User> activeUsers = userJpaPersistence.findAllActive();

            // Then
            assertEquals(3, activeUsers.size());
            assertTrue(activeUsers.stream().allMatch(User::isActive));
            assertTrue(activeUsers.stream().anyMatch(u -> u.getMail().equals("active1@test.com")));
            assertTrue(activeUsers.stream().anyMatch(u -> u.getMail().equals("active2@test.com")));
            assertTrue(activeUsers.stream().anyMatch(u -> u.getMail().equals("active3@test.com")));
        }

        @Test
        @DisplayName("should return empty collection when all users are soft-deleted")
        void findAllActive_WhenAllUsersDeleted_ShouldReturnEmpty() {
            // Given: only soft-deleted users
            jpaRepository.save(UserEntity.builder()
                    .name("Deleted 1").password("pass1").mail("del1@test.com").active(false).build());
            jpaRepository.save(UserEntity.builder()
                    .name("Deleted 2").password("pass2").mail("del2@test.com").active(false).build());

            // When
            Collection<User> activeUsers = userJpaPersistence.findAllActive();

            // Then
            assertTrue(activeUsers.isEmpty());
        }

        @Test
        @DisplayName("should return empty collection when database is empty")
        void findAllActive_WhenNoUsers_ShouldReturnEmpty() {
            // Given: empty database (setUp clears it)

            // When
            Collection<User> activeUsers = userJpaPersistence.findAllActive();

            // Then
            assertTrue(activeUsers.isEmpty());
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperationsTests {

        @Test
        @DisplayName("should save and retrieve a user")
        void save_ShouldPersistUser() {
            // Given
            User user = new User(null, "John Doe", "password123", "john@test.com", true);

            // When
            User saved = userJpaPersistence.save(user);

            // Then
            assertNotNull(saved.getId());
            assertEquals("John Doe", saved.getName());
            assertEquals("john@test.com", saved.getMail());
            assertTrue(saved.isActive());
        }

        @Test
        @DisplayName("should find user by ID")
        void findById_ShouldReturnUser() {
            // Given
            UserEntity entity = jpaRepository.save(UserEntity.builder()
                    .name("Test User").password("pass").mail("test@test.com").active(true).build());

            // When
            User found = userJpaPersistence.findById(entity.getId());

            // Then
            assertNotNull(found);
            assertEquals(entity.getId(), found.getId());
            assertEquals("Test User", found.getName());
        }

        @Test
        @DisplayName("should return null when user not found by ID")
        void findById_WhenNotFound_ShouldReturnNull() {
            // When
            User found = userJpaPersistence.findById(99999);

            // Then
            assertNull(found);
        }

        @Test
        @DisplayName("should find user by email (case-insensitive)")
        void findByEmail_ShouldBeCaseInsensitive() {
            // Given
            jpaRepository.save(UserEntity.builder()
                    .name("Test User").password("pass").mail("Test@Example.COM").active(true).build());

            // When
            User found = userJpaPersistence.findByEmail("test@example.com");

            // Then
            assertNotNull(found);
            assertEquals("Test@Example.COM", found.getMail());
        }

        @Test
        @DisplayName("should update existing user")
        void update_ShouldModifyUser() {
            // Given
            UserEntity entity = jpaRepository.save(UserEntity.builder()
                    .name("Original Name").password("pass").mail("user@test.com").active(true).build());
            User updated = new User(entity.getId(), "Updated Name", "newpass", "newemail@test.com", false);

            // When
            User result = userJpaPersistence.update(entity.getId(), updated);

            // Then
            assertNotNull(result);
            assertEquals("Updated Name", result.getName());
            assertEquals("newemail@test.com", result.getMail());
            assertFalse(result.isActive());
        }

        @Test
        @DisplayName("should delete user by ID")
        void deleteById_ShouldRemoveUser() {
            // Given
            UserEntity entity = jpaRepository.save(UserEntity.builder()
                    .name("To Delete").password("pass").mail("delete@test.com").active(true).build());

            // When
            boolean deleted = userJpaPersistence.deleteById(entity.getId());

            // Then
            assertTrue(deleted);
            assertNull(userJpaPersistence.findById(entity.getId()));
        }

        @Test
        @DisplayName("should return false when deleting non-existent user")
        void deleteById_WhenNotFound_ShouldReturnFalse() {
            // When
            boolean deleted = userJpaPersistence.deleteById(99999);

            // Then
            assertFalse(deleted);
        }
    }
}
