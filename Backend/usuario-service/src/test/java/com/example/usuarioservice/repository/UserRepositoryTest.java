package com.example.usuarioservice.repository;

import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.UserRepository;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    private UserRepository userRepository;

    @org.junit.jupiter.api.io.TempDir
    java.nio.file.Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = new UserRepository();
        java.nio.file.Path tempFile = tempDir.resolve("users-test.json");
        userRepository.setFilePathForTests(tempFile.toString());
        userRepository.initialize();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // no-op: tempDir cleanup handled by JUnit
    }

    @Test
    void testSaveUser() {
        User user = new User(1, "Juan", "password123", "juan@mail.com", true);
        userRepository.save(user);
        User found = userRepository.findById(1);
        assertNotNull(found);
        assertEquals("Juan", found.getName());
    }

    @Test
    void testFindById() {
        User user = new User(2, "Ana", "password123", "ana@mail.com", true);
        userRepository.save(user);
        User found = userRepository.findById(2);
        assertNotNull(found);
        assertEquals("Ana", found.getName());
        assertNull(userRepository.findById(999)); // No existe
    }

    @Test
    void testDeleteUser() {
        User user = new User(3, "Luis", "password123", "luis@mail.com", true);
        userRepository.save(user);
        userRepository.deleteById(3);
        assertNull(userRepository.findById(3));
    }

    @Test
    void testFindAllUsers() {
        userRepository.save(new User(4, "Mario", "password123", "mario@mail.com", true));
        userRepository.save(new User(5, "Lucia", "password123", "lucia@mail.com", true));
        Collection<User> usersCol = userRepository.findAll();
        List<User> users = new ArrayList<>(usersCol);
        assertEquals(2, users.size());
        Set<String> names = new HashSet<>();
        for (User u : users) names.add(u.getName());
        assertTrue(names.contains("Mario"));
        assertTrue(names.contains("Lucia"));
    }
}
