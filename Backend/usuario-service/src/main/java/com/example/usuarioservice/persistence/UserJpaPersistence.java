package com.example.usuarioservice.persistence;

import com.example.usuarioservice.entity.UserEntity;
import com.example.usuarioservice.mapper.UserEntityMapper;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserJpaPersistence implements IUserPersistence {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    @Override
    public void initialize() throws IOException {
        // No initialization needed for JPA - database is managed by Spring/Hibernate
        log.info("JPA persistence initialized - database schema managed by Hibernate");
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Finding all users from PostgreSQL");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> findAllActive() {
        log.debug("Finding all active users from PostgreSQL");
        return jpaRepository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public User findById(int id) {
        log.debug("Finding user by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return jpaRepository.findByMailIgnoreCase(email)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public User save(User user) {
        log.info("Saving user with email: {}", user.getMail());
        UserEntity entity = mapper.toEntity(user);
        entity.setId(null); // Ensure new ID is generated
        UserEntity saved = jpaRepository.save(entity);
        log.info("User saved with ID: {}", saved.getId());
        return mapper.toDomain(saved);
    }

    @Override
    public User update(int id, User user) {
        log.info("Updating user ID: {}", id);
        return jpaRepository.findById(id)
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setPassword(user.getPassword());
                    existing.setMail(user.getMail());
                    existing.setActive(user.isActive());
                    UserEntity updated = jpaRepository.save(existing);
                    log.info("User {} updated successfully", id);
                    return mapper.toDomain(updated);
                })
                .orElse(null);
    }

    @Override
    public User partialUpdate(int id, Map<String, Object> updates) {
        log.debug("Partially updating user ID: {}", id);
        return jpaRepository.findById(id)
                .map(existing -> {
                    applyUpdates(existing, updates);
                    UserEntity updated = jpaRepository.save(existing);
                    return mapper.toDomain(updated);
                })
                .orElse(null);
    }

    /**
     * Aplica los updates a una entidad existente.
     * Itera sobre los campos presentes en el mapa de updates y los aplica a la entidad.
     *
     * @param entity Entidad a actualizar
     * @param updates Mapa de campos a actualizar con sus valores
     */
    private void applyUpdates(UserEntity entity, Map<String, Object> updates) {
        if (updates.containsKey("name")) {
            entity.setName((String) updates.get("name"));
        }
        if (updates.containsKey("password")) {
            entity.setPassword((String) updates.get("password"));
        }
        if (updates.containsKey("mail")) {
            entity.setMail((String) updates.get("mail"));
        }
        if (updates.containsKey("active")) {
            entity.setActive(parseBoolean(updates.get("active")));
        }
    }

    /**
     * Parsea un objeto a booleano de forma segura.
     * Maneja los casos donde el valor puede ser Boolean, String o null.
     *
     * @param value Valor a parsear
     * @return Booleano parseado, o false si no es válido
     */
    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @Override
    public boolean deleteById(int id) {
        log.info("Deleting user ID: {}", id);
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            log.info("User {} deleted successfully", id);
            return true;
        }
        log.warn("User {} not found for deletion", id);
        return false;
    }

    @Override
    public void deleteAll() {
        log.info("Deleting all users");
        jpaRepository.deleteAll();
        log.info("All users deleted successfully");
    }
}
