package com.example.usuarioservice.repository;

import com.example.usuarioservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for UserEntity.
 * Provides CRUD operations and custom queries for PostgreSQL.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Find all active users (not soft-deleted).
     * HU-USR-01: Filter users with active=true
     */
    List<UserEntity> findByActiveTrue();

    /**
     * Find user by email (case-insensitive).
     */
    Optional<UserEntity> findByMailIgnoreCase(String mail);

    /**
     * Check if email already exists.
     */
    boolean existsByMailIgnoreCase(String mail);
}
