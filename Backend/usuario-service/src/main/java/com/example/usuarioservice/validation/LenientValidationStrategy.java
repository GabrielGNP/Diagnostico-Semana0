package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Estrategia de validación LENIENTE para usuarios normales.
 * 
 * Strategy Pattern: Implementación concreta con validaciones básicas.
 * 
 * Reglas de validación leniente:
 * - Contraseña: Mínimo 8 caracteres
 * - Contraseña: Debe contener mayúsculas, minúsculas y números (sin caracteres especiales obligatorios)
 * - Nombre: Mínimo 2 caracteres
 * - Email: Validación básica de formato
 * 
 * Esta estrategia se usa típicamente para:
 * - Creación de cuentas de usuarios estándar
 * - Auto-registro de usuarios
 * - Contextos donde la facilidad de uso es prioritaria
 * 
 * Nota: Esta validación es ADICIONAL a las validaciones de anotaciones
 * del DTO (@NotBlank, @Email, etc.). Aquí agregamos validaciones de negocio
 * que no se pueden expresar con anotaciones simples.
 * 
 * Uso:
 * <pre>
 * IValidationStrategy strategy = new LenientValidationStrategy();
 * strategy.validateForCreation(request);
 * </pre>
 */
@Component("LENIENT")
@Slf4j
public class LenientValidationStrategy implements IValidationStrategy {
    
    // Pattern para contraseña leniente: min 8 chars, mayúsculas, minúsculas, números
    private static final Pattern LENIENT_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&#]{8,}$"
    );
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_NAME_LENGTH = 2;
    
    @Override
    public void validateForCreation(CreateUsuarioRequest request) throws ValidationException {
        log.debug("Aplicando validación LENIENTE para creación de usuario: {}", request.getEmail());
        
        validateName(request.getNombre());
        validatePassword(request.getContrasena());
        
        // Email: Solo confiamos en la validación de anotaciones (@Email)
        // No agregamos validaciones adicionales en modo leniente
        
        log.debug("Validación LENIENTE exitosa para usuario: {}", request.getEmail());
    }
    
    @Override
    public void validateForUpdate(UpdateUsuarioRequest request) throws ValidationException {
        log.debug("Aplicando validación LENIENTE para actualización de usuario");
        
        // En actualización, los campos son opcionales, solo validamos si están presentes
        if (request.getNombre() != null) {
            validateName(request.getNombre());
        }
        
        if (request.getContrasena() != null) {
            validatePassword(request.getContrasena());
        }
        
        // Email: Solo validación de anotaciones
        
        log.debug("Validación LENIENTE de actualización exitosa");
    }
    
    /**
     * Valida el nombre con reglas lenientes.
     * - Mínimo 2 caracteres
     * - Permite espacios al inicio/final (se trimean automáticamente)
     */
    private void validateName(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("El nombre no puede estar vacío");
        }
        
        // En modo leniente, aceptamos espacios y los trimeamos
        String trimmedName = nombre.trim();
        
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new ValidationException(
                String.format("El nombre debe tener al menos %d caracteres", MIN_NAME_LENGTH)
            );
        }
    }
    
    /**
     * Valida la contraseña con reglas lenientes.
     * - Mínimo 8 caracteres
     * - Debe contener mayúsculas
     * - Debe contener minúsculas
     * - Debe contener números
     * - NO requiere caracteres especiales (diferencia con strict)
     */
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("La contraseña no puede estar vacía");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("La contraseña debe tener al menos %d caracteres", MIN_PASSWORD_LENGTH)
            );
        }
        
        if (!LENIENT_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException(
                "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
            );
        }
        
        // No verificamos contraseñas comunes en modo leniente
        // para no frustrar al usuario
    }
    
    @Override
    public String getStrategyName() {
        return "LENIENT";
    }
}
