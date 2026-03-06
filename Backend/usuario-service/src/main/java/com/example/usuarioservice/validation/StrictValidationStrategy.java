package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Estrategia de validación ESTRICTA para usuarios privilegiados (administradores).
 * 
 * Strategy Pattern: Implementación concreta con validaciones más rigurosas.
 * 
 * Reglas de validación estricta:
 * - Contraseña: Mínimo 12 caracteres
 * - Contraseña: Debe contener mayúsculas, minúsculas, números Y caracteres especiales
 * - Nombre: Mínimo 3 caracteres (más que la validación leniente)
 * - Email: Validación adicional de dominio
 * - Sin espacios al inicio/final de los campos
 * 
 * Esta estrategia se usa típicamente para:
 * - Creación de cuentas de administrador
 * - Usuarios con permisos elevados
 * - Contextos de alta seguridad
 * 
 * Uso:
 * <pre>
 * IValidationStrategy strategy = new StrictValidationStrategy();
 * strategy.validateForCreation(request);
 * </pre>
 */
@Component("STRICT")
@Slf4j
public class StrictValidationStrategy implements IValidationStrategy {
    
    // Pattern para contraseña estricta: min 12 chars, mayúsculas, minúsculas, números, especiales
    private static final Pattern STRICT_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,}$"
    );
    
    // Pattern para validar que el email tenga un dominio válido
    private static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MIN_NAME_LENGTH = 3;
    
    @Override
    public void validateForCreation(CreateUsuarioRequest request) throws ValidationException {
        log.debug("Aplicando validación ESTRICTA para creación de usuario: {}", request.getEmail());
        
        validateName(request.getNombre());
        validateEmail(request.getEmail());
        validatePassword(request.getContrasena());
        
        log.debug("Validación ESTRICTA exitosa para usuario: {}", request.getEmail());
    }
    
    @Override
    public void validateForUpdate(UpdateUsuarioRequest request) throws ValidationException {
        log.debug("Aplicando validación ESTRICTA para actualización de usuario");
        
        // En actualización, los campos son opcionales, solo validamos si están presentes
        if (request.getNombre() != null) {
            validateName(request.getNombre());
        }
        
        if (request.getEmail() != null) {
            validateEmail(request.getEmail());
        }
        
        if (request.getContrasena() != null) {
            validatePassword(request.getContrasena());
        }
        
        log.debug("Validación ESTRICTA de actualización exitosa");
    }
    
    /**
     * Valida el nombre con reglas estrictas.
     * - Mínimo 3 caracteres
     * - Sin espacios al inicio/final
     */
    private void validateName(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("El nombre no puede estar vacío");
        }
        
        String trimmedName = nombre.trim();
        if (!nombre.equals(trimmedName)) {
            throw new ValidationException("El nombre no debe tener espacios al inicio o final");
        }
        
        if (nombre.length() < MIN_NAME_LENGTH) {
            throw new ValidationException(
                String.format("El nombre debe tener al menos %d caracteres (validación estricta)", MIN_NAME_LENGTH)
            );
        }
    }
    
    /**
     * Valida el email con reglas estrictas.
     * - Formato válido
     * - Dominio válido
     * - Sin espacios
     */
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("El email no puede estar vacío");
        }
        
        String trimmedEmail = email.trim();
        if (!email.equals(trimmedEmail)) {
            throw new ValidationException("El email no debe tener espacios al inicio o final");
        }
        
        if (!EMAIL_DOMAIN_PATTERN.matcher(email).matches()) {
            throw new ValidationException("El email debe tener un formato y dominio válido");
        }
        
        // Validar que no use dominios temporales o inseguros (ejemplo básico)
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        if (domain.equals("tempmail.com") || domain.equals("trash-mail.com")) {
            throw new ValidationException("No se permiten emails temporales en validación estricta");
        }
    }
    
    /**
     * Valida la contraseña con reglas MUY estrictas.
     * - Mínimo 12 caracteres
     * - Debe contener mayúsculas
     * - Debe contener minúsculas
     * - Debe contener números
     * - Debe contener caracteres especiales (@$!%*?&#)
     */
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("La contraseña no puede estar vacía");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("La contraseña debe tener al menos %d caracteres (validación estricta)", 
                    MIN_PASSWORD_LENGTH)
            );
        }
        
        if (!STRICT_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException(
                "La contraseña debe contener mayúsculas, minúsculas, números " +
                "Y al menos un carácter especial (@$!%*?&#)"
            );
        }
        
        // Verificar que no sea una contraseña común
        if (isCommonPassword(password)) {
            throw new ValidationException(
                "La contraseña es demasiado común. Use una contraseña más segura"
            );
        }
    }
    
    /**
     * Verifica si la contraseña es demasiado común.
     * En producción, esto consultaría una lista de contraseñas comunes.
     */
    private boolean isCommonPassword(String password) {
        // Lista básica de contraseñas comunes (en producción sería más completa)
        String[] commonPasswords = {
            "Password123!", "Admin123456!", "Welcome123!", 
            "Qwerty123456!", "Abc123456789!"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getStrategyName() {
        return "STRICT";
    }
}
