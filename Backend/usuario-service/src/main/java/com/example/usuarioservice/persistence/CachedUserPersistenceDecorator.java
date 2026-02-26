package com.example.usuarioservice.persistence;

import com.example.usuarioservice.model.User;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator Pattern para agregar caché a la persistencia de usuarios.
 * 
 * Este decorator envuelve cualquier implementación de IUserPersistence y agrega
 * una capa de caché transparente, mejorando el rendimiento sin modificar
 * la implementación subyacente.
 * 
 * Beneficios del Decorator Pattern:
 * - Open/Closed Principle: Agrega funcionalidad sin modificar UserRepository
 * - Single Responsibility: UserRepository persiste, Decorator cachea
 * - Composición sobre herencia: Se puede agregar/quitar dinámicamente
 * - Reutilizable: Funciona con cualquier implementación de IUserPersistence
 * 
 * Rendimiento:
 * - findByEmail: O(n) → O(1) con caché
 * - findById: O(1) → O(1) con caché (pero más rápido en memoria)
 * 
 * Thread-Safety:
 * - Usa ConcurrentHashMap para operaciones concurrentes seguras
 */
@Slf4j
public class CachedUserPersistenceDecorator implements IUserPersistence {

    private final IUserPersistence delegate;
    private final Map<String, User> emailCache = new ConcurrentHashMap<>();
    private final Map<Integer, User> idCache = new ConcurrentHashMap<>();

    /**
     * Constructor que recibe la implementación a decorar.
     * 
     * @param delegate La implementación de persistencia a envolver
     */
    public CachedUserPersistenceDecorator(IUserPersistence delegate) {
        this.delegate = delegate;
        log.info("CachedUserPersistenceDecorator creado - Caché activado");
    }

    @Override
    public void initialize() throws IOException {
        log.info("Inicializando persistencia con caché");
        delegate.initialize();
        log.info("Caché inicializado - Listo para usar");
    }

    @Override
    public Collection<User> findAll() {
        log.debug("findAll() - Delegando sin caché (retorna colección completa)");
        return delegate.findAll();
    }

    @Override
    public Collection<User> findAllActive() {
        log.debug("findAllActive() - Delegando sin caché (retorna usuarios activos)");
        return delegate.findAllActive();
    }

    @Override
    public User findById(int id) {
        return idCache.computeIfAbsent(id, key -> {
            log.debug("Cache MISS para ID: {} - Consultando persistencia", id);
            User user = delegate.findById(key);
            if (user != null) {
                log.debug("Usuario encontrado y cacheado - ID: {}", id);
            }
            return user;
        });
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        
        String normalizedEmail = email.toLowerCase();
        
        return emailCache.computeIfAbsent(normalizedEmail, key -> {
            log.debug("Cache MISS para email: {} - Consultando persistencia", email);
            User user = delegate.findByEmail(email);
            if (user != null) {
                log.debug("Usuario encontrado y cacheado - Email: {}", email);
                // También cachear por ID
                idCache.put(user.getId(), user);
            }
            return user;
        });
    }

    @Override
    public User save(User user) {
        log.debug("Guardando usuario y invalidando caché - Email: {}", user.getMail());
        
        User saved = delegate.save(user);
        
        // Invalidar caché para este usuario
        invalidateUserCache(saved);
        
        log.info("Usuario guardado - Cache invalidado para ID: {} y Email: {}", 
                 saved.getId(), saved.getMail());
        
        return saved;
    }

    @Override
    public User update(int id, User user) {
        log.debug("Actualizando usuario y invalidando caché - ID: {}", id);
        
        // Obtener el usuario anterior para invalidar su email si cambió
        User oldUser = delegate.findById(id);
        
        User updated = delegate.update(id, user);
        
        // Invalidar caché del usuario anterior y del nuevo
        if (oldUser != null) {
            invalidateUserCache(oldUser);
        }
        invalidateUserCache(updated);
        
        log.info("Usuario actualizado - Cache invalidado para ID: {}", id);
        
        return updated;
    }

    @Override
    public User partialUpdate(int id, Map<String, Object> updates) {
        log.debug("Actualización parcial de usuario - ID: {}", id);
        
        // Obtener el usuario anterior
        User oldUser = delegate.findById(id);
        
        User updated = delegate.partialUpdate(id, updates);
        
        // Invalidar caché
        if (oldUser != null) {
            invalidateUserCache(oldUser);
        }
        if (updated != null) {
            invalidateUserCache(updated);
        }
        
        log.info("Usuario parcialmente actualizado - Cache invalidado para ID: {}", id);
        
        return updated;
    }

    @Override
    public boolean deleteById(int id) {
        log.debug("Eliminando usuario y invalidando caché - ID: {}", id);
        
        // Obtener el usuario antes de eliminarlo para invalidar su email
        User user = delegate.findById(id);
        
        boolean deleted = delegate.deleteById(id);
        
        if (deleted && user != null) {
            invalidateUserCache(user);
            log.info("Usuario eliminado - Cache invalidado para ID: {} y Email: {}", 
                     id, user.getMail());
        }
        
        return deleted;
    }

    @Override
    public void deleteAll() {
        log.info("Eliminando todos los usuarios y limpiando caché");
        delegate.deleteAll();
        clearCache();
    }

    /**
     * Invalida todas las entradas de caché relacionadas con un usuario.
     * 
     * @param user El usuario cuyo caché se debe invalidar
     */
    private void invalidateUserCache(User user) {
        if (user == null) {
            return;
        }
        
        // Invalidar por ID
        if (user.getId() != null) {
            idCache.remove(user.getId());
        }
        
        // Invalidar por email (normalizado)
        if (user.getMail() != null) {
            emailCache.remove(user.getMail().toLowerCase());
        }
    }

    /**
     * Limpia completamente el caché (útil para testing o mantenimiento).
     */
    public void clearCache() {
        log.info("Limpiando caché completo");
        emailCache.clear();
        idCache.clear();
        log.info("Caché limpiado - {} entradas removidas", 
                 emailCache.size() + idCache.size());
    }

    /**
     * Obtiene estadísticas del caché (útil para monitoreo).
     * 
     * @return String con estadísticas del caché
     */
    public String getCacheStats() {
        return String.format("Cache Stats - Email entries: %d, ID entries: %d", 
                           emailCache.size(), idCache.size());
    }
}
