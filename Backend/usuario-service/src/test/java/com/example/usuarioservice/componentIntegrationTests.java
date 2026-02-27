package com.example.usuarioservice;

import com.example.usuarioservice.controller.UsuarioController;
import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import com.example.usuarioservice.dto.UsuarioResponse;
import com.example.usuarioservice.exception.UsuarioNotFoundException;
import com.example.usuarioservice.mapper.UsuarioMapper;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración de componentes del usuario-service.
 * Verifica la integración entre Controller, Service, Persistence y Mapper.
 * Prueba flujos de negocio completos del sistema de usuarios.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Component Integration Tests - Usuario Service")
public class componentIntegrationTests {

    @Autowired
    private UsuarioController usuarioController;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private IUserPersistence userPersistence;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private Validator validator;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializa la persistencia antes de cada test
        userPersistence.initialize();
    }

    @Test
    @DisplayName("Integration: Create User - Controller to Persistence")
    public void testCreateUserComponentIntegration() {
        // Arrange
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Juan Pérez")
            .email("juan@example.com")
            .contrasena("SecurePass123")
            .build();

        // Act
        ResponseEntity<UsuarioResponse> response = usuarioController.crear(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(201, response.getStatusCode().value(), "Status code should be 201 CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Juan Pérez", response.getBody().getNombre(), "User name should match");
        assertEquals("juan@example.com", response.getBody().getEmail(), "User email should match");
        assertTrue(response.getBody().isActivo(), "User should be active");
    }

    @Test
    @DisplayName("Integration: Get All Users - Complete Flow")
    public void testGetAllUsersComponentIntegration() {
        // Arrange - Create a user first
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("María García")
            .email("maria@example.com")
            .contrasena("SecurePass123")
            .build();
        usuarioController.crear(request);

        // Act
        ResponseEntity<Collection<UsuarioResponse>> response = usuarioController.obtenerTodos();

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "Status code should be 200");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isEmpty(), "Collection should not be empty");
        assertTrue(
            response.getBody().stream()
                .anyMatch(u -> "María García".equals(u.getNombre())),
            "Created user should be in response"
        );
    }

    @Test
    @DisplayName("Integration: Get User by Email - Complete Flow")
    public void testGetUserByEmailComponentIntegration() {
        // Arrange
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Carlos López")
            .email("carlos@example.com")
            .contrasena("SecurePass123")
            .build();
        ResponseEntity<UsuarioResponse> createResponse = usuarioController.crear(request);

        // Act
        ResponseEntity<UsuarioResponse> getResponse = usuarioController.obtenerPorIdentificador("carlos@example.com");

        // Assert
        assertNotNull(getResponse, "Get response should not be null");
        assertEquals(200, getResponse.getStatusCode().value(), "Status code should be 200");
        assertEquals("carlos@example.com", getResponse.getBody().getEmail(), "Email should match");
        assertEquals("Carlos López", getResponse.getBody().getNombre(), "Name should match");
    }

    @Test
    @DisplayName("Integration: Update User - Complete Flow")
    public void testUpdateUserComponentIntegration() {
        // Arrange
        CreateUsuarioRequest createRequest = CreateUsuarioRequest.builder()
            .nombre("Ana Martínez")
            .email("ana@example.com")
            .contrasena("SecurePass123")
            .build();
        ResponseEntity<UsuarioResponse> createResponse = usuarioController.crear(createRequest);
        int userId = createResponse.getBody().getId();

        UpdateUsuarioRequest updateRequest = UpdateUsuarioRequest.builder()
            .nombre("Ana María Martínez")
            .email("ana.maria@example.com")
            .contrasena("NewSecurePass123")
            .activo(true)
            .build();

        // Act
        ResponseEntity<UsuarioResponse> updateResponse = usuarioController.actualizar(userId, updateRequest);

        // Assert
        assertNotNull(updateResponse, "Update response should not be null");
        assertEquals(200, updateResponse.getStatusCode().value(), "Status code should be 200");
        assertEquals("Ana María Martínez", updateResponse.getBody().getNombre(), "Name should be updated");
        assertEquals("ana.maria@example.com", updateResponse.getBody().getEmail(), "Email should be updated");
    }

    @Test
    @DisplayName("Integration: Delete User - Complete Flow")
    public void testDeleteUserComponentIntegration() {
        // Arrange
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Diego Fernández")
            .email("diego@example.com")
            .contrasena("SecurePass123")
            .build();
        ResponseEntity<UsuarioResponse> createResponse = usuarioController.crear(request);
        int userId = createResponse.getBody().getId();

        // Act
        ResponseEntity<Void> deleteResponse = usuarioController.eliminar(userId);
        ResponseEntity<UsuarioResponse> getResponse = null;
        
        try {
            getResponse = usuarioController.obtenerPorIdentificador(String.valueOf(userId));
        } catch (UsuarioNotFoundException e) {
            // Expected behavior
        }

        // Assert
        assertNotNull(deleteResponse, "Delete response should not be null");
        assertEquals(204, deleteResponse.getStatusCode().value(), "Status code should be 204 NO CONTENT");
        assertNull(getResponse, "Deleted user should not be retrievable");
    }

    @Test
    @DisplayName("Integration: Mapper - DTO to Entity and back")
    public void testMapperComponentIntegration() {
        // Arrange
        CreateUsuarioRequest createRequest = CreateUsuarioRequest.builder()
            .nombre("Pedro Sánchez")
            .email("pedro@example.com")
            .contrasena("SecurePass123")
            .build();

        // Act
        User entity = usuarioMapper.toUser(createRequest);
        UsuarioResponse response = usuarioMapper.toResponse(entity);

        // Assert
        assertNotNull(entity, "Entity should not be null");
        assertNotNull(response, "Response should not be null");
        assertEquals("Pedro Sánchez", entity.getName(), "Entity name should match");
        assertEquals("Pedro Sánchez", response.getNombre(), "Response name should match");
        assertEquals("pedro@example.com", response.getEmail(), "Response email should match");
        assertTrue(response.isActivo(), "Response should indicate active user");
    }

    @Test
    @DisplayName("Integration: Service Layer - Multiple Operations")
    public void testServiceLayerComponentIntegration() {
        // Arrange
        CreateUsuarioRequest request1 = CreateUsuarioRequest.builder()
            .nombre("User One")
            .email("user1@example.com")
            .contrasena("SecurePass123")
            .build();

        CreateUsuarioRequest request2 = CreateUsuarioRequest.builder()
            .nombre("User Two")
            .email("user2@example.com")
            .contrasena("SecurePass123")
            .build();

        // Act
        User created1 = usuarioService.crear(request1);
        User created2 = usuarioService.crear(request2);
        var retrieved = usuarioService.obtenerPorEmail("user1@example.com");
        Collection<User> allUsers = usuarioService.obtenerTodos();

        // Assert
        assertNotNull(created1, "First user should be created");
        assertNotNull(created2, "Second user should be created");
        assertTrue(retrieved.isPresent(), "User should be retrievable by email");
        assertEquals("User One", retrieved.get().getName(), "Retrieved user should match");
        assertFalse(allUsers.isEmpty(), "Collection of all users should not be empty");
    }

    @Test
    @DisplayName("Integration: Partial Update User")
    public void testPartialUpdateUserComponentIntegration() {
        // Arrange
        CreateUsuarioRequest createRequest = CreateUsuarioRequest.builder()
            .nombre("Elena García")
            .email("elena@example.com")
            .contrasena("SecurePass123")
            .build();
        ResponseEntity<UsuarioResponse> createResponse = usuarioController.crear(createRequest);
        int userId = createResponse.getBody().getId();

        UpdateUsuarioRequest partialUpdate = UpdateUsuarioRequest.builder()
            .nombre("Elena María García")
            .build();

        // Act
        ResponseEntity<UsuarioResponse> updateResponse = usuarioController.actualizarParcial(userId, partialUpdate);

        // Assert
        assertNotNull(updateResponse, "Update response should not be null");
        assertEquals(200, updateResponse.getStatusCode().value(), "Status code should be 200");
        assertEquals("Elena María García", updateResponse.getBody().getNombre(), "Name should be partially updated");
        assertEquals("elena@example.com", updateResponse.getBody().getEmail(), "Email should remain unchanged");
    }

    @Test
    @DisplayName("Integration: Non-existent User Retrieval")
    public void testNonExistentUserRetrieval() {
        // Act & Assert
        assertThrows(UsuarioNotFoundException.class, () -> {
            usuarioController.obtenerPorIdentificador("99999");
        }, "Should throw UsuarioNotFoundException for non-existent ID");
    }

    @Test
    @DisplayName("Integration: User Persistence")
    public void testUserPersistence() {
        // Arrange
        User user = new User();
        user.setName("Persistence Test User");
        user.setMail("persistence@example.com");
        user.setPassword("SecurePass123");
        user.setActive(true);

        // Act
        User saved = userPersistence.save(user);
        User retrieved = userPersistence.findById(saved.getId());

        // Assert
        assertNotNull(saved, "User should be saved");
        assertNotNull(retrieved, "User should be retrievable from persistence");
        assertEquals("Persistence Test User", retrieved.getName(), "User name should persist");
        assertEquals("persistence@example.com", retrieved.getMail(), "User email should persist");
        assertTrue(retrieved.isActive(), "User active status should persist");
    }

    @Test
    @DisplayName("Integration: User Validation - Invalid Email")
    public void testUserValidationInvalidEmail() {
        // Arrange
        CreateUsuarioRequest invalidRequest = CreateUsuarioRequest.builder()
            .nombre("Invalid User")
            .email("invalid-email")  // Invalid email format
            .contrasena("SecurePass123")
            .build();

        // Act - Use the validator to validate the request
        Set<ConstraintViolation<CreateUsuarioRequest>> violations = validator.validate(invalidRequest);

        // Assert - Should have validation violations
        assertFalse(violations.isEmpty(), "Should have validation violations for invalid email");
        
        // Verify that the email validation violation is present
        boolean hasEmailViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(hasEmailViolation, "Should have email validation violation");
    }

    @Test
    @DisplayName("Integration: User Identification - By ID")
    public void testUserIdentificationById() {
        // Arrange
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("ID Test User")
            .email("idtest@example.com")
            .contrasena("SecurePass123")
            .build();
        ResponseEntity<UsuarioResponse> createResponse = usuarioController.crear(request);
        int userId = createResponse.getBody().getId();

        // Act
        ResponseEntity<UsuarioResponse> getResponse = usuarioController.obtenerPorIdentificador(String.valueOf(userId));

        // Assert
        assertNotNull(getResponse, "Get response should not be null");
        assertEquals(200, getResponse.getStatusCode().value(), "Status code should be 200");
        assertEquals(userId, getResponse.getBody().getId(), "Retrieved user ID should match");
        assertEquals("ID Test User", getResponse.getBody().getNombre(), "Retrieved user name should match");
    }
}
