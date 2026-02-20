package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.CachedUserPersistenceDecorator;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.persistence.UserJpaPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Optional;

/**
 * Configuración de persistencia usando el Factory Pattern y Decorator Pattern.
 * 
 * Factory Pattern: Crea la implementación de persistencia según configuración
 * Decorator Pattern: Envuelve la persistencia con caché si está habilitado
 * 
 * Esta clase demuestra el uso de ambos patrones:
 * 1. Factory crea la implementación base (JSON, JPA/Database, etc.)
 * 2. Decorator agrega caché de forma transparente si está activado
 * 
 * Ventajas de este enfoque:
 * 1. Centralización: La lógica de creación está en un solo lugar (Factory)
 * 2. Flexibilidad: Podemos cambiar el tipo de persistencia desde configuración
 * 3. Composición: El caché se puede activar/desactivar sin modificar la persistencia
 * 4. Testing: Facilita la inyección de diferentes implementaciones en tests
 * 
 * Configuración:
 * - app.persistence.type: Tipo de persistencia ("json" o "jpa")
 * - app.persistence.cache.enabled: Activar/desactivar caché (true/false)
 * 
 * Ejemplo en application.properties:
 *   app.persistence.type=jpa
 *   app.persistence.cache.enabled=true
 */
@Configuration
@Slf4j
public class PersistenceConfig {

    @Value("${app.persistence.type:json}")
    private String persistenceType;
    
    @Value("${app.persistence.cache.enabled:true}")
    private boolean cacheEnabled;

    @Autowired(required = false)
    private UserJpaPersistence jpaPersistence;

    /**
     * Crea y configura el bean de IUserPersistence usando Factory y Decorator Pattern.
     * 
     * Factory Pattern: Crea la implementación base según configuración
     * Decorator Pattern: Envuelve con caché de forma opcional y transparente
     * 
     * El Factory Pattern permite que esta configuración sea agnóstica de la
     * implementación concreta. Soporta:
     * - "json": Persistencia en archivos JSON (UserRepository)
     * - "jpa": Persistencia en PostgreSQL/H2 (UserJpaPersistence)
     * 
     * El Decorator Pattern permite agregar funcionalidad (caché) sin modificar
     * la implementación base:
     * 1. La persistencia base NO sabe que está siendo decorada
     * 2. El caché se puede activar/desactivar desde configuración
     * 3. El código cliente (UsuarioService) es agnóstico al caché
     * 
     * @return La instancia de IUserPersistence configurada (decorada si cache.enabled=true)
     * @throws IOException Si ocurre un error durante la inicialización
     */
    @Bean
    public IUserPersistence userPersistence() throws IOException {
        log.info("========== Configurando persistencia de usuarios ==========");
        log.info("Tipo de persistencia configurado: {}", persistenceType);
        log.info("Caché habilitado: {}", cacheEnabled);
        
        // Usamos el Factory Pattern para crear la instancia base
        IUserPersistence persistence = UserPersistenceFactory.createPersistence(
                persistenceType, 
                Optional.ofNullable(jpaPersistence)
        );
        
        // Inicializamos la persistencia (cargar datos, establecer conexiones, etc.)
        try {
            persistence.initialize();
            log.info("Persistencia inicializada exitosamente");
        } catch (IOException e) {
            log.error("Error inicializando persistencia de tipo: {}", persistenceType, e);
            throw e;
        }
        
        // Aplicamos Decorator Pattern: si el caché está habilitado, envolvemos la persistencia
        if (cacheEnabled) {
            log.info("Aplicando Decorator Pattern: Envolviendo persistencia con caché");
            persistence = new CachedUserPersistenceDecorator(persistence);
            log.info("Caché activado - Mejora de rendimiento para búsquedas: O(n) → O(1)");
        } else {
            log.info("Caché deshabilitado - Usando persistencia directa");
        }
        
        log.info("===========================================================");
        return persistence;
    }
}
