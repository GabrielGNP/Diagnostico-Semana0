package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.service.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory Pattern para crear instancias de IUserPersistence.
 * 
 * Este patrón permite desacoplar la creación de objetos de su uso,
 * facilitando futuros cambios de implementación sin modificar el código cliente.
 * 
 * Beneficios del Factory Pattern:
 * - Single Responsibility: La creación está separada del uso
 * - Open/Closed Principle: Abierto a nuevas implementaciones sin modificar código existente
 * - Dependency Inversion: El código depende de abstracciones (IUserPersistence)
 * - Facilita testing: Se pueden inyectar diferentes implementaciones según el contexto
 * 
 * Actualmente soporta:
 * - "json": Persistencia en archivos JSON (UserRepository)
 * 
 * Futuras extensiones podrían incluir:
 * - "database": Persistencia en base de datos relacional
 * - "in-memory": Persistencia en memoria para tests
 * - "mongodb": Persistencia en MongoDB
 */
@Slf4j
public class UserPersistenceFactory {

    /**
     * Crea una instancia de IUserPersistence según el tipo especificado.
     * 
     * @param type El tipo de persistencia a crear (actualmente solo "json")
     * @return Una nueva instancia de IUserPersistence
     * @throws IllegalArgumentException Si el tipo no es soportado
     */
    public static IUserPersistence createPersistence(String type) {
        log.info("Creando persistencia de tipo: {}", type);
        
        if (type == null || type.isBlank()) {
            log.warn("Tipo de persistencia no especificado, usando JSON por defecto");
            type = "json";
        }

        switch (type.toLowerCase().trim()) {
            case "json":
                log.info("Instanciando UserRepository (persistencia JSON)");
                return new UserRepository();
                
            default:
                log.error("Tipo de persistencia no soportado: {}", type);
                throw new IllegalArgumentException(
                    String.format("Tipo de persistencia no soportado: '%s'. " +
                    "Actualmente solo se soporta: 'json'", type)
                );
        }
    }

    /**
     * Valida si un tipo de persistencia es soportado.
     * 
     * @param type El tipo a validar
     * @return true si es soportado, false en caso contrario
     */
    public static boolean isTypeSupported(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        
        String normalized = type.toLowerCase().trim();
        return normalized.equals("json");
    }
}
