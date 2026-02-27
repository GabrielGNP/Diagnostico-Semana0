package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.CachedUserPersistenceDecorator;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.persistence.UserJpaPersistence;
import com.example.usuarioservice.persistence.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserPersistenceFactory.
 * Tests factory pattern implementation and type selection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserPersistenceFactory - persistence creation")
class UserPersistenceFactoryTest {

    @Mock
    private UserJpaPersistence jpaPersistence;

    @Test
    @DisplayName("createPersistence with 'json' returns UserRepository")
    void createPersistence_json_returnsUserRepository() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence("json");

        assertNotNull(persistence);
        assertInstanceOf(UserRepository.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with 'jpa' returns UserJpaPersistence")
    void createPersistence_jpa_returnsUserJpaPersistence() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence("jpa", Optional.of(jpaPersistence));

        assertNotNull(persistence);
        assertInstanceOf(UserJpaPersistence.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with 'JSON' (case-insensitive) returns UserRepository")
    void createPersistence_jsonUpperCase_returnsUserRepository() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence("JSON");

        assertNotNull(persistence);
        assertInstanceOf(UserRepository.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with 'JPA' (case-insensitive) returns UserJpaPersistence")
    void createPersistence_jpaUpperCase_returnsUserJpaPersistence() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence("JPA", Optional.of(jpaPersistence));

        assertNotNull(persistence);
        assertInstanceOf(UserJpaPersistence.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with null type defaults to JSON")
    void createPersistence_nullType_defaultsToJson() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence(null);

        assertNotNull(persistence);
        assertInstanceOf(UserRepository.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with blank type defaults to JSON")
    void createPersistence_blankType_defaultsToJson() {
        IUserPersistence persistence = UserPersistenceFactory.createPersistence("   ");

        assertNotNull(persistence);
        assertInstanceOf(UserRepository.class, persistence);
    }

    @Test
    @DisplayName("createPersistence with unsupported type throws IllegalArgumentException")
    void createPersistence_unsupportedType_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> UserPersistenceFactory.createPersistence("mongodb"));
    }

    @Test
    @DisplayName("createPersistence with 'jpa' and no optional throws IllegalStateException")
    void createPersistence_jpaWithoutOptional_throwsException() {
        assertThrows(IllegalStateException.class,
            () -> UserPersistenceFactory.createPersistence("jpa", Optional.empty()));
    }

    @Test
    @DisplayName("isTypeSupported returns true for 'json'")
    void isTypeSupported_json_returnsTrue() {
        assertTrue(UserPersistenceFactory.isTypeSupported("json"));
    }

    @Test
    @DisplayName("isTypeSupported returns true for 'jpa'")
    void isTypeSupported_jpa_returnsTrue() {
        assertTrue(UserPersistenceFactory.isTypeSupported("jpa"));
    }

    @Test
    @DisplayName("isTypeSupported returns false for unsupported type")
    void isTypeSupported_unsupportedType_returnsFalse() {
        assertFalse(UserPersistenceFactory.isTypeSupported("mongodb"));
    }

    @Test
    @DisplayName("isTypeSupported returns false for null")
    void isTypeSupported_null_returnsFalse() {
        assertFalse(UserPersistenceFactory.isTypeSupported(null));
    }

    @Test
    @DisplayName("isTypeSupported returns false for blank")
    void isTypeSupported_blank_returnsFalse() {
        assertFalse(UserPersistenceFactory.isTypeSupported("   "));
    }
}

