package com.example.usuarioservice.mapper;

import com.example.usuarioservice.entity.UserEntity;
import com.example.usuarioservice.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper between User domain model and UserEntity JPA entity.
 */
@Component
public class UserEntityMapper {

    /**
     * Convert UserEntity to User domain model.
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getPassword(),
                entity.getMail(),
                entity.isActive()
        );
    }

    /**
     * Convert User domain model to UserEntity.
     */
    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .password(user.getPassword())
                .mail(user.getMail())
                .active(user.isActive())
                .build();
    }
}
