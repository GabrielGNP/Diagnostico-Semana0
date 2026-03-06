package com.example.usuarioservice.mapper;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DTOs y entidades de Usuario.
 * Centraliza la lógica de conversión.
 */
@Component
public class UsuarioMapper {
    
    /**
     * Convierte CreateUsuarioRequest a User
     */
    public User toUser(CreateUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        
        User user = new User();
        user.setName(request.getNombre());
        user.setMail(request.getEmail());
        user.setPassword(request.getContrasena());
        user.setActive(true);
        
        return user;
    }
    
    /**
     * Convierte UpdateUsuarioRequest a User (parcial)
     */
    public User toUserUpdate(UpdateUsuarioRequest request, User existente) {
        if (request == null) {
            return existente;
        }
        
        User user = existente;
        
        if (request.getNombre() != null) {
            user.setName(request.getNombre());
        }
        if (request.getEmail() != null) {
            user.setMail(request.getEmail());
        }
        if (request.getContrasena() != null) {
            user.setPassword(request.getContrasena());
        }
        if (request.getActivo() != null) {
            user.setActive(request.getActivo());
        }
        
        return user;
    }
    
    /**
     * Convierte User a UsuarioResponse
     */
    public UsuarioResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UsuarioResponse.builder()
            .id(user.getId())
            .nombre(user.getName())
            .email(user.getMail())
            .activo(user.isActive())
            .build();
    }
}
