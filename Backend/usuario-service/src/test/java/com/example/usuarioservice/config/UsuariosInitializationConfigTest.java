package com.example.usuarioservice.config;

import com.example.usuarioservice.persistence.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UsuariosInitializationConfig.
 * Tests initialization bean and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuariosInitializationConfig - initialization")
class UsuariosInitializationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("initializeUsers creates CommandLineRunner bean")
    void initializeUsers_createsCommandLineRunner() {
        UsuariosInitializationConfig config = new UsuariosInitializationConfig();
        CommandLineRunner runner = config.initializeUsers(userRepository);

        assertNotNull(runner);
    }

    @Test
    @DisplayName("CommandLineRunner calls userRepository.init()")
    void initializeUsers_commandLineRunner_callsInit() throws Exception {
        UsuariosInitializationConfig config = new UsuariosInitializationConfig();
        CommandLineRunner runner = config.initializeUsers(userRepository);

        runner.run();

        verify(userRepository, times(1)).init();
    }

    @Test
    @DisplayName("CommandLineRunner propagates exception from init()")
    void initializeUsers_commandLineRunner_propagatesException() throws Exception {
        UsuariosInitializationConfig config = new UsuariosInitializationConfig();
        doThrow(new RuntimeException("DB error")).when(userRepository).init();
        CommandLineRunner runner = config.initializeUsers(userRepository);

        assertThrows(RuntimeException.class, () -> runner.run());
        verify(userRepository, times(1)).init();
    }

    @Test
    @DisplayName("CommandLineRunner handles IOExceptionWrapped in RuntimeException")
    void initializeUsers_commandLineRunner_handlesIOException() throws Exception {
        UsuariosInitializationConfig config = new UsuariosInitializationConfig();
        java.io.IOException ioException = new java.io.IOException("File not found");
        doThrow(ioException).when(userRepository).init();
        CommandLineRunner runner = config.initializeUsers(userRepository);

        assertThrows(java.io.IOException.class, () -> runner.run());
    }

    @Test
    @DisplayName("initializeUsers with null userRepository throws NullPointerException (fail-fast)")
    void initializeUsers_nullRepository_throwsNullPointerException() {
        UsuariosInitializationConfig config = new UsuariosInitializationConfig();

        NullPointerException thrown = assertThrows(NullPointerException.class,
            () -> config.initializeUsers(null),
            "initializeUsers debe validar que UserRepository no sea null");

        assertTrue(thrown.getMessage().contains("UserRepository no puede ser null"),
            "El mensaje debe mencionar que UserRepository no puede ser null");
    }
}



