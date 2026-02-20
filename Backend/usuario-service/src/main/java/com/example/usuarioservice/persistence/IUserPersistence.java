package com.example.usuarioservice.persistence;

import com.example.usuarioservice.model.User;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Interfaz que define el contrato para la persistencia de usuarios.
 * Implementaciones pueden ser JSON, Base de Datos, etc.
 */
public interface IUserPersistence {
    
    /**
     * Inicializa la persistencia (p.e., cargar datos de archivo)
     */
    void initialize() throws IOException;
    
    /**
     * Obtiene todos los usuarios
     */
    Collection<User> findAll();
    
    /**
     * Obtiene todos los usuarios activos (no soft-deleted).
     * HU-USR-01: Filtrar usuarios con active=false
     */
    Collection<User> findAllActive();
    
    /**
     * Busca un usuario por ID
     */
    User findById(int id);
    
    /**
     * Busca un usuario por email
     */
    User findByEmail(String email);
    
    /**
     * Guarda un nuevo usuario
     */
    User save(User user);
    
    /**
     * Actualiza un usuario existente
     */
    User update(int id, User user);
    
    /**
     * Actualiza parcialmente un usuario
     */
    User partialUpdate(int id, java.util.Map<String, Object> updates);
    
    /**
     * Elimina un usuario
     */
    boolean deleteById(int id);
}
