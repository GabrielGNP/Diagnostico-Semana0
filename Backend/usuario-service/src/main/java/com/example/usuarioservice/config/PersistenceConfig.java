package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.IUserPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuración de persistencia usando el Factory Pattern.
 * 
 * Esta clase demuestra el uso del patrón Factory al crear el bean de persistencia.
 * En lugar de instanciar directamente UserRepository, delegamos la creación
 * al UserPersistenceFactory.
 * 
 * Ventajas de este enfoque:
 * 1. Centralización: La lógica de creación está en un solo lugar (Factory)
 * 2. Flexibilidad: Podemos cambiar el tipo de persistencia desde configuración
 * 3. Mantenibilidad: Agregar nuevas implementaciones no requiere cambiar esta clase
 * 4. Testing: Facilita la inyección de diferentes implementaciones en tests
 * 
 * Configuración:
 * - app.persistence.type: Tipo de persistencia (actualmente solo "json")
 * - Por defecto: "json"
 * 
 * Ejemplo en application.properties:
 *   app.persistence.type=json
 */
@Configuration
@Slf4j
public class PersistenceConfig {

    @Value("${app.persistence.type:json}")
    private String persistenceType;

    /**
     * Crea y configura el bean de IUserPersistence usando el Factory Pattern.
     * 
     * El Factory Pattern permite que esta configuración sea agnóstica de la
     * implementación concreta. Si en el futuro agregamos una implementación
     * de base de datos, solo necesitamos:
     * 1. Crear la nueva clase que implemente IUserPersistence
     * 2. Actualizar UserPersistenceFactory para soportar el nuevo tipo
     * 3. Cambiar app.persistence.type en application.properties
     * 
     * Esta clase NO necesita modificación.
     * 
     * @return La instancia de IUserPersistence configurada
     * @throws IOException Si ocurre un error durante la inicialización
     */
    @Bean
    public IUserPersistence userPersistence() throws IOException {
        log.info("========== Configurando persistencia de usuarios ==========");
        log.info("Tipo de persistencia configurado: {}", persistenceType);
        
        // Usamos el Factory Pattern para crear la instancia
        IUserPersistence persistence = UserPersistenceFactory.createPersistence(persistenceType);
        
        // Inicializamos la persistencia (cargar datos, establecer conexiones, etc.)
        try {
            persistence.initialize();
            log.info("Persistencia inicializada exitosamente");
            log.info("===========================================================");
        } catch (IOException e) {
            log.error("Error inicializando persistencia de tipo: {}", persistenceType, e);
            throw e;
        }
        
        return persistence;
    }
}
