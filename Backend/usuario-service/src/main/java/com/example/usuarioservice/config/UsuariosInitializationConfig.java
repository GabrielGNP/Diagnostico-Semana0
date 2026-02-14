package com.example.usuarioservice.config;

import com.example.usuarioservice.service.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UsuariosInitializationConfig {
    
    @Bean
    public CommandLineRunner initializeUsers(UserRepository userRepository) {
        return args -> {
            try {
                log.info("Inicializando persistencia de usuarios...");
                userRepository.init();
                log.info("Usuarios inicializados exitosamente");
            } catch (Exception e) {
                log.error("Error inicializando usuarios", e);
                throw e;
            }
        };
    }
}
