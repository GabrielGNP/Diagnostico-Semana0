package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;

/**
 * Strategy Pattern: Interfaz para diferentes estrategias de validación.
 * 
 * Permite implementar múltiples estrategias de validación según el contexto:
 * - Validación estricta para administradores
 * - Validación leniente para usuarios normales
 * - Validaciones personalizadas para casos especiales
 * 
 * Ventajas del Strategy Pattern:
 * - Open/Closed Principle: Agregar nuevas validaciones sin modificar código existente
 * - Single Responsibility: Cada estrategia se enfoca en un tipo de validación
 * - Testabilidad: Cada estrategia se puede testear independientemente
 * - Flexibilidad: Cambiar estrategia en runtime según necesidad
 * 
 * Ejemplo de uso:
 * <pre>
 * IValidationStrategy strategy = new StrictValidationStrategy();
 * strategy.validateForCreation(request);  // Lanza ValidationException si falla
 * </pre>
 */
public interface IValidationStrategy {
    
    /**
     * Valida un request de creación de usuario.
     * 
     * Esta validación se ejecuta DESPUÉS de las validaciones de anotaciones
     * (@NotBlank, @Email, etc.) y puede agregar validaciones adicionales
     * según el tipo de usuario o contexto.
     * 
     * @param request El request con los datos del nuevo usuario
     * @throws ValidationException Si la validación falla
     */
    void validateForCreation(CreateUsuarioRequest request) throws ValidationException;
    
    /**
     * Valida un request de actualización de usuario.
     * 
     * Puede tener validaciones diferentes a la creación, ya que
     * en la actualización algunos campos son opcionales.
     * 
     * @param request El request con los datos a actualizar
     * @throws ValidationException Si la validación falla
     */
    void validateForUpdate(UpdateUsuarioRequest request) throws ValidationException;
    
    /**
     * Retorna el nombre de la estrategia para logging/debugging.
     * 
     * @return Nombre descriptivo de la estrategia
     */
    String getStrategyName();
}
