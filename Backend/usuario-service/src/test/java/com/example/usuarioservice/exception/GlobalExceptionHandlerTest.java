package com.example.usuarioservice.exception;

import com.example.usuarioservice.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests verify correct HTTP status codes and error response structure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Exception handling")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Test
    @DisplayName("handleUsuarioNotFound returns 404 NOT_FOUND")
    void handleUsuarioNotFound_shouldReturn404() {
        // Given
        UsuarioNotFoundException exception = new UsuarioNotFoundException("Usuario no encontrado con ID: 1");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios/1");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsuarioNotFound(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Usuario no encontrado", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Usuario no encontrado"));
    }

    @Test
    @DisplayName("handleUsuarioYaExiste returns 409 CONFLICT")
    void handleUsuarioYaExiste_shouldReturn409() {
        // Given
        UsuarioYaExisteException exception = new UsuarioYaExisteException("Email ya está registrado");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsuarioYaExiste(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Usuario ya existe", response.getBody().getError());
    }

    @Test
    @DisplayName("handleValidationException returns 400 BAD_REQUEST")
    void handleValidationException_shouldReturn400() {
        // Given
        ValidationException exception = new ValidationException("Validación fallida");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Error de validación", response.getBody().getError());
    }

    @Test
    @DisplayName("handleValidationErrors returns 400 BAD_REQUEST with field errors")
    void handleValidationErrors_shouldReturn400WithFieldErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        FieldError fieldError = new FieldError("usuario", "email", "Email inválido");
        errors.add(fieldError);

        when(bindingResult.getAllErrors()).thenReturn(errors);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            mock(org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver.class),
            bindingResult
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Errores de validación", response.getBody().getError());
    }

    @Test
    @DisplayName("handleGlobalException returns 500 INTERNAL_SERVER_ERROR")
    void handleGlobalException_shouldReturn500() {
        // Given
        Exception exception = new RuntimeException("Error inesperado");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error interno del servidor", response.getBody().getError());
    }

    @Test
    @DisplayName("ErrorResponse includes timestamp")
    void errorResponse_shouldIncludeTimestamp() {
        // Given
        UsuarioNotFoundException exception = new UsuarioNotFoundException("Test");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsuarioNotFound(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("ErrorResponse includes path information")
    void errorResponse_shouldIncludePath() {
        // Given
        UsuarioNotFoundException exception = new UsuarioNotFoundException("Test");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/usuarios/1");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsuarioNotFound(exception, webRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals("/api/v1/usuarios/1", response.getBody().getPath());
    }

    @Test
    @DisplayName("UsuarioNotFoundException constructor works correctly")
    void usuarioNotFoundException_constructorShouldWork() {
        // Given
        String message = "Usuario no encontrado";

        // When
        UsuarioNotFoundException exception = new UsuarioNotFoundException(message);

        // Then
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("UsuarioYaExisteException constructor works correctly")
    void usuarioYaExisteException_constructorShouldWork() {
        // Given
        String message = "Usuario ya existe";

        // When
        UsuarioYaExisteException exception = new UsuarioYaExisteException(message);

        // Then
        assertEquals(message, exception.getMessage());
    }
}

