package com.example.usuarioservice.validation;

/**
 * Excepción lanzada cuando falla una validación de negocio.
 * 
 * Esta excepción se usa en las estrategias de validación (Strategy Pattern)
 * para indicar que los datos no cumplen con las reglas de negocio.
 * 
 * Se diferencia de las validaciones de anotaciones (@NotBlank, @Email, etc.)
 * que se manejan automáticamente por Spring. Esta excepción es para validaciones
 * más complejas que dependen del contexto o tipo de usuario.
 * 
 * Ejemplo:
 * <pre>
 * if (password.length() < 12) {
 *     throw new ValidationException("La contraseña debe tener al menos 12 caracteres");
 * }
 * </pre>
 */
public class ValidationException extends RuntimeException {
    
    /**
     * Constructor con mensaje de error.
     * 
     * @param mensaje Descripción del error de validación
     */
    public ValidationException(String mensaje) {
        super(mensaje);
    }
    
    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Descripción del error de validación
     * @param causa Excepción que causó el error
     */
    public ValidationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
