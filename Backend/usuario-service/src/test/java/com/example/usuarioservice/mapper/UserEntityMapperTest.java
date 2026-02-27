package com.example.usuarioservice.mapper;

import com.example.usuarioservice.entity.UserEntity;
import com.example.usuarioservice.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserEntityMapper - mapping behavior")
class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper();

    @Test
    @DisplayName("toDomain returns null when entity is null")
    void toDomain_returnsNull_whenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    @DisplayName("toEntity returns null when user is null")
    void toEntity_returnsNull_whenUserIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    @DisplayName("toDomain maps all fields")
    void toDomain_mapsAllFields() {
        UserEntity entity = UserEntity.builder()
            .id(1)
            .name("Juan")
            .password("pass")
            .mail("juan@test.com")
            .active(true)
            .build();

        User domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(1, domain.getId());
        assertEquals("Juan", domain.getName());
        assertEquals("pass", domain.getPassword());
        assertEquals("juan@test.com", domain.getMail());
        assertTrue(domain.isActive());
    }

    @Test
    @DisplayName("toEntity maps all fields")
    void toEntity_mapsAllFields() {
        User user = new User(2, "Ana", "secret", "ana@test.com", false);

        UserEntity entity = mapper.toEntity(user);

        assertNotNull(entity);
        assertEquals(2, entity.getId());
        assertEquals("Ana", entity.getName());
        assertEquals("secret", entity.getPassword());
        assertEquals("ana@test.com", entity.getMail());
        assertFalse(entity.isActive());
    }

    @Test
    @DisplayName("toDomain preserves null fields")
    void toDomain_preservesNullFields() {
        UserEntity entity = UserEntity.builder()
            .id(null)
            .name(null)
            .password(null)
            .mail(null)
            .active(false)
            .build();

        User domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertNull(domain.getId());
        assertNull(domain.getName());
        assertNull(domain.getPassword());
        assertNull(domain.getMail());
        assertFalse(domain.isActive());
    }

    @Test
    @DisplayName("toEntity preserves null fields")
    void toEntity_preservesNullFields() {
        User user = new User(null, null, null, null, true);

        UserEntity entity = mapper.toEntity(user);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getName());
        assertNull(entity.getPassword());
        assertNull(entity.getMail());
        assertTrue(entity.isActive());
    }

    @Test
    @DisplayName("toDomain maps active false")
    void toDomain_mapsActiveFalse() {
        UserEntity entity = UserEntity.builder()
            .id(3)
            .name("Carlos")
            .password("p")
            .mail("carlos@test.com")
            .active(false)
            .build();

        User domain = mapper.toDomain(entity);

        assertFalse(domain.isActive());
    }

    @Test
    @DisplayName("toEntity maps active false")
    void toEntity_mapsActiveFalse() {
        User user = new User(4, "Luisa", "p", "luisa@test.com", false);

        UserEntity entity = mapper.toEntity(user);

        assertFalse(entity.isActive());
    }

    @Test
    @DisplayName("entity to domain to entity round trip preserves fields")
    void roundTrip_entityToDomainToEntity_preservesFields() {
        UserEntity entity = UserEntity.builder()
            .id(5)
            .name("Mario")
            .password("pass")
            .mail("mario@test.com")
            .active(true)
            .build();

        User domain = mapper.toDomain(entity);
        UserEntity roundTrip = mapper.toEntity(domain);

        assertEquals(entity.getId(), roundTrip.getId());
        assertEquals(entity.getName(), roundTrip.getName());
        assertEquals(entity.getPassword(), roundTrip.getPassword());
        assertEquals(entity.getMail(), roundTrip.getMail());
        assertEquals(entity.isActive(), roundTrip.isActive());
    }

    @Test
    @DisplayName("domain to entity to domain round trip preserves fields")
    void roundTrip_domainToEntityToDomain_preservesFields() {
        User user = new User(6, "Sofia", "pw", "sofia@test.com", true);

        UserEntity entity = mapper.toEntity(user);
        User roundTrip = mapper.toDomain(entity);

        assertEquals(user.getId(), roundTrip.getId());
        assertEquals(user.getName(), roundTrip.getName());
        assertEquals(user.getPassword(), roundTrip.getPassword());
        assertEquals(user.getMail(), roundTrip.getMail());
        assertEquals(user.isActive(), roundTrip.isActive());
    }
}

