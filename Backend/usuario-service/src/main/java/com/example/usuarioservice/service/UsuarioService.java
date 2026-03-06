package com.example.usuarioservice.service;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.exception.UsuarioNotFoundException;
import com.example.usuarioservice.exception.UsuarioYaExisteException;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.validation.ValidationContext;
import com.example.usuarioservice.validation.ValidationContext.ValidationStrategyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio para usuarios.
 * Orquesta entre el controlador y la persistencia.
 * 
 * Aplica múltiples principios SOLID y patrones de diseño:
 * 
 * 1. Inversión de Dependencias (DIP):
 *    - Depende de abstracciones (IUserPersistence, IValidationStrategy)
 *    - No depende de implementaciones concretas
 *    - Facilita testing con mocks
 * 
 * 2. Strategy Pattern (para validación):
 *    - Usa ValidationContext para aplicar diferentes estrategias de validación
 *    - Permite cambiar estrategia sin modificar este código
 *    - Valida con LENIENT por defecto (usuarios normales)
 * 
 * 3. Open/Closed Principle:
 *    - Abierto a extensión (nuevas estrategias de validación)
 *    - Cerrado a modificación (no cambia al agregar estrategias)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService implements IUsuarioService {
    
    private final IUserPersistence userRepository;
    private final ValidationContext validationContext;
    
    @Override
    public Collection<User> obtenerTodos() {
        log.debug("Obteniendo todos los usuarios");
        Collection<User> usuarios = userRepository.findAll();
        log.info("Total de usuarios obtenidos: {}", usuarios.size());
        return usuarios;
    }
    
    @Override
    public Optional<User> obtenerPorIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            log.warn("Identificador vacío o null");
            return Optional.empty();
        }
        
        // Si es un email
        if (identificador.contains("@")) {
            log.debug("Buscando usuario por email: {}", identificador);
            return obtenerPorEmail(identificador);
        }
        
        // Si es un ID
        try {
            int id = Integer.parseInt(identificador);
            log.debug("Buscando usuario por ID: {}", id);
            return obtenerPorId(id);
        } catch (NumberFormatException e) {
            log.warn("Identificador inválido: no es email ni ID numérico: {}", identificador);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> obtenerPorId(int id) {
        log.debug("Buscando usuario por ID: {}", id);
        Optional<User> usuario = Optional.ofNullable(userRepository.findById(id));
        if (usuario.isEmpty()) {
            log.warn("Usuario no encontrado con ID: {}", id);
        }
        return usuario;
    }
    
    @Override
    public Optional<User> obtenerPorEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Email vacío o null");
            return Optional.empty();
        }
        log.debug("Buscando usuario por email: {}", email);
        Optional<User> usuario = Optional.ofNullable(userRepository.findByEmail(email));
        if (usuario.isEmpty()) {
            log.warn("Usuario no encontrado con email: {}", email);
        }
        return usuario;
    }
    
    @Override
    public User crear(CreateUsuarioRequest request) {
        log.info("Creando nuevo usuario con email: {}", request.getEmail());
        
        // Strategy Pattern: Aplicar validación de negocio según estrategia
        // Por defecto usa LENIENT para usuarios normales
        // Para admin se podría usar STRICT
        validationContext.validateForCreation(request, ValidationStrategyType.LENIENT);
        log.debug("Validación de negocio completada para: {}", request.getEmail());
        
        // Validar que el email no exista
        if (userRepository.findByEmail(request.getEmail()) != null) {
            log.warn("Intento de crear usuario con email duplicado: {}", request.getEmail());
            throw new UsuarioYaExisteException("El email " + request.getEmail() + " ya está registrado");
        }
        
        // Crear entidad
        User usuario = new User();
        usuario.setName(request.getNombre());
        usuario.setMail(request.getEmail());
        usuario.setPassword(request.getContrasena());
        usuario.setActive(true);
        
        // Persistir
        User guardado = userRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", guardado.getId());
        
        return guardado;
    }
    
    @Override
    public Optional<User> actualizar(int id, UpdateUsuarioRequest request) {
        log.info("Actualizando usuario ID: {}", id);
        
        User usuarioExistente = userRepository.findById(id);
        if (usuarioExistente == null) {
            log.warn("Usuario no encontrado para actualizar: {}", id);
            return Optional.empty();
        }
        
        // Strategy Pattern: Validar campos de actualización según estrategia
        validationContext.validateForUpdate(request, ValidationStrategyType.LENIENT);
        log.debug("Validación de actualización completada para usuario ID: {}", id);
        
        // Validar email no duplicado si cambió
        if (request.getEmail() != null && 
            !request.getEmail().equals(usuarioExistente.getMail()) &&
            userRepository.findByEmail(request.getEmail()) != null) {
            log.warn("Email duplicado al actualizar usuario {}: {}", id, request.getEmail());
            throw new UsuarioYaExisteException("El email " + request.getEmail() + " ya está registrado");
        }
        
        // Actualizar campos
        if (request.getNombre() != null) {
            usuarioExistente.setName(request.getNombre());
        }
        if (request.getEmail() != null) {
            usuarioExistente.setMail(request.getEmail());
        }
        if (request.getContrasena() != null) {
            usuarioExistente.setPassword(request.getContrasena());
        }
        if (request.getActivo() != null) {
            usuarioExistente.setActive(request.getActivo());
        }
        
        User resultado = userRepository.update(id, usuarioExistente);
        log.info("Usuario {} actualizado exitosamente", id);
        
        return Optional.of(resultado);
    }
    
    @Override
    public Optional<User> actualizarParcial(int id, UpdateUsuarioRequest request) {
        log.debug("Actualizando parcialmente usuario ID: {}", id);
        return actualizar(id, request);
    }
    
    @Override
    public boolean eliminar(int id) {
        log.info("Eliminando usuario ID: {}", id);
        boolean eliminado = userRepository.deleteById(id);
        if (eliminado) {
            log.info("Usuario {} eliminado exitosamente", id);
        } else {
            log.warn("Usuario no encontrado para eliminar: {}", id);
        }
        return eliminado;
    }
}
