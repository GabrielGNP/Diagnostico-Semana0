package com.example.usuarioservice.service;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.model.User;
import java.util.Collection;
import java.util.Optional;

/**
 * Interfaz que define los casos de uso del servicio de usuarios
 */
public interface IUsuarioService {
    
    /**
     * Obtiene todos los usuarios
     */
    Collection<User> obtenerTodos();
    
    /**
     * Obtiene un usuario por identificador (ID o email)
     */
    Optional<User> obtenerPorIdentificador(String identificador);
    
    /**
     * Obtiene un usuario por ID
     */
    Optional<User> obtenerPorId(int id);
    
    /**
     * Obtiene un usuario por email
     */
    Optional<User> obtenerPorEmail(String email);
    
    /**
     * Crea un nuevo usuario
     * @throws com.example.usuarioservice.exception.UsuarioYaExisteException si el email ya existe
     */
    User crear(CreateUsuarioRequest request);
    
    /**
     * Actualiza completamente un usuario
     */
    Optional<User> actualizar(int id, UpdateUsuarioRequest request);
    
    /**
     * Actualiza parcialmente un usuario
     */
    Optional<User> actualizarParcial(int id, UpdateUsuarioRequest request);
    
    /**
     * Elimina un usuario
     */
    boolean eliminar(int id);
}
