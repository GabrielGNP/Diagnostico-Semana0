package com.example.usuarioservice;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.model.User;
import com.example.usuarioservice.persistence.IUserPersistence;
import com.example.usuarioservice.service.UsuarioService;
import com.example.usuarioservice.validation.ValidationContext;
import com.example.usuarioservice.validation.ValidationException;
import com.example.usuarioservice.exception.UsuarioYaExisteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RED unit tests for HU-USR-03 using Mockito to avoid DB configuration.
 * Tests follow Given/When/Then structure and are intentionally minimal.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HU-USR-03 RED (unit): Create User - Given/When/Then")
public class HUUSR03_CreateUserTests {

    private static final Logger log = LoggerFactory.getLogger(HUUSR03_CreateUserTests.class);

    @Mock
    private IUserPersistence userPersistence;

    @Mock
    private ValidationContext validationContext;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Given valid data When creating a user Then it is saved and returned")
    public void givenValidData_whenCreate_thenSaved() {
        // Given
        log.info("Given: valid CreateUsuarioRequest is prepared");
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Unit Test User")
            .email("unit+huusr03@example.com")
            .contrasena("ValidPass123")
            .build();

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setName("Unit Test User");
        savedUser.setMail("unit+huusr03@example.com");
        savedUser.setPassword("ValidPass123");
        savedUser.setActive(true);

        when(userPersistence.findByEmail("unit+huusr03@example.com")).thenReturn(null);
        when(userPersistence.save(any(User.class))).thenReturn(savedUser);

        // When
        log.info("When: calling usuarioService.crear");
        User result = usuarioService.crear(request);

        // Then
        log.info("Then: asserting created user and verifying interactions");
        assertNotNull(result, "Created user should not be null");
        assertEquals(1, result.getId(), "Created user should have the persisted ID");
        assertEquals("unit+huusr03@example.com", result.getMail(), "Email should match");

        // Verify interactions
        verify(validationContext, times(1)).validateForCreation(request, ValidationContext.ValidationStrategyType.LENIENT);
        verify(userPersistence, times(1)).findByEmail("unit+huusr03@example.com");
        verify(userPersistence, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Given invalid data When validation fails Then create throws ValidationException")
    public void givenInvalidData_whenValidationFails_thenThrows() {
        // Given
        log.info("Given: invalid CreateUsuarioRequest (violates business rules)");
        CreateUsuarioRequest invalid = CreateUsuarioRequest.builder()
            .nombre("A")
            .email("invalid-email")
            .contrasena("short")
            .build();

        doThrow(new ValidationException("Invalid business data")).when(validationContext)
            .validateForCreation(invalid, ValidationContext.ValidationStrategyType.LENIENT);

        // When
        log.info("When: calling usuarioService.crear with invalid request (expect exception)");

        // Then
        assertThrows(ValidationException.class, () -> usuarioService.crear(invalid));

        // Ensure no persistence call happened
        log.info("Then: verify userPersistence.save was never called");
        verify(userPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Given duplicate email When creating Then UsuarioYaExisteException is thrown")
    public void givenDuplicateEmail_whenCreate_thenThrowsDuplicate() {
        // Given
        log.info("Given: an existing user with the same email");
        CreateUsuarioRequest req = CreateUsuarioRequest.builder()
            .nombre("Dup")
            .email("dup+huusr03@example.com")
            .contrasena("ValidPass123")
            .build();

        User existing = new User();
        existing.setId(5);
        existing.setMail("dup+huusr03@example.com");

        when(userPersistence.findByEmail("dup+huusr03@example.com")).thenReturn(existing);

        // When
        log.info("When: calling usuarioService.crear with duplicate email (expect duplicate exception)");

        // Then
        assertThrows(UsuarioYaExisteException.class, () -> usuarioService.crear(req));

        // Verify no save call
        log.info("Then: verify userPersistence.save was never called for duplicate");
        verify(userPersistence, never()).save(any());
    }

    @Test
    @DisplayName("Given valid data When creating Then saved user is active (integrity invariant)")
    public void givenValidData_whenCreate_thenSavedUserIsActive() {
        // Given
        log.info("Given: valid request for active-check");
        CreateUsuarioRequest request = CreateUsuarioRequest.builder()
            .nombre("Active Check")
            .email("active+huusr03@example.com")
            .contrasena("ValidPass123")
            .build();

        User savedUser = new User();
        savedUser.setId(2);
        savedUser.setName("Active Check");
        savedUser.setMail("active+huusr03@example.com");
        savedUser.setPassword("ValidPass123");
        savedUser.setActive(true);

        when(userPersistence.findByEmail("active+huusr03@example.com")).thenReturn(null);
        when(userPersistence.save(any(User.class))).thenReturn(savedUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // When
        log.info("When: calling usuarioService.crear and capturing saved user");
        User result = usuarioService.crear(request);

        // Then
        log.info("Then: verify that saved user was active and interactions occurred");
        verify(userPersistence, times(1)).save(captor.capture());
        User toSave = captor.getValue();
        assertTrue(toSave.isActive(), "User passed to persistence should be active");
        assertEquals("active+huusr03@example.com", toSave.getMail(), "Email on saved entity should match request");
        assertNotNull(result, "Result should not be null");
    }
}
