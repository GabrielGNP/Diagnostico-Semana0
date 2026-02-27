package com.example.usuarioservice.repository;

import com.example.usuarioservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    List<UserEntity> findByActiveTrue();

    Optional<UserEntity> findByMailIgnoreCase(String mail);
}
