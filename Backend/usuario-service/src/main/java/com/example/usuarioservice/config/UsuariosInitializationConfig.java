package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Configuración para inicializar la persistencia de usuarios al startup.
 *
 * Responsabilidades:
 * - Crear el bean CommandLineRunner que ejecuta la inicialización
 * - Validar que las dependencias (UserRepository) estén disponibles (fail-fast)
 * - Ejecutar la inicialización con manejo robusto de errores
 * - Loguear el progreso y cualquier problema
 *
 * Beneficios del diseño:
 * - Fail-fast: Detecta configuración inválida inmediatamente al startup
 * - No silent catches: Toda excepción se propaga
 * - Validación explícita: Uso de Objects.requireNonNull
 * - Buenas prácticas: Separación de concerns, logging adecuado
 */
@Configuration
@Slf4j
public class UsuariosInitializationConfig {
    
    /**
     * Crea un CommandLineRunner que inicializa la persistencia de usuarios.
     *
     * Aplica fail-fast: valida que userRepository no sea null antes de crear el runner.
     * De este modo, si la inyección falla, el startup del aplicativo falla inmediatamente,
     * alertando al operador de un problema de configuración.
     *
     * @param userRepository El repositorio de usuarios (obligatorio)
     * @return Un CommandLineRunner que ejecuta la inicialización
     * @throws IllegalStateException Si userRepository es null
     */
    @Bean
    public CommandLineRunner initializeUsers(UserRepository userRepository) {
        // Validación fail-fast: detectar configuración inválida inmediatamente
        Objects.requireNonNull(userRepository,
            "UserRepository no puede ser null - Verifica la configuración de Spring");

        return args -> {
            try {
                log.info("Inicializando persistencia de usuarios...");
                userRepository.init();
                log.info("Usuarios inicializados exitosamente");
            } catch (Exception e) {
                log.error("Error inicializando usuarios - el startup fallará", e);
                throw e;
            }
        };
    }
}
