package com.example.usuarioservice.validation;

import com.example.usuarioservice.dto.CreateUsuarioRequest;
import com.example.usuarioservice.dto.UpdateUsuarioRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Context del Strategy Pattern para validación de usuarios.
 * 
 * Esta clase orquesta las diferentes estrategias de validación disponibles
 * y permite seleccionar dinámicamente cuál usar según el contexto.
 * 
 * Ventajas del patrón:
 * - Desacopla UsuarioService de las estrategias concretas
 * - Permite cambiar estrategia en runtime
 * - Facilita agregar nuevas estrategias sin modificar código existente
 * - Centraliza la gestión de estrategias
 * 
 * Flujo de uso:
 * 1. Spring inyecta automáticamente todas las implementaciones de IValidationStrategy
 * 2. Se almacenan en un Map con su nombre como clave (@Component("STRICT"), etc.)
 * 3. Cuando se solicita validación, se selecciona la estrategia por nombre
 * 4. Se ejecuta la validación con la estrategia seleccionada
 * 
 * Ejemplo:
 * <pre>
 * // Validar con estrategia estricta
 * validationContext.validateForCreation(request, ValidationStrategyType.STRICT);
 * 
 * // Validar con estrategia leniente
 * validationContext.validateForCreation(request, ValidationStrategyType.LENIENT);
 * </pre>
 */
@Component
@Slf4j
public class ValidationContext {
    
    private final Map<String, IValidationStrategy> strategies;
    
    /**
     * Constructor con inyección de dependencias.
     * 
     * Spring automáticamente inyecta un Map con todas las implementaciones
     * de IValidationStrategy, usando el nombre del bean como clave:
     * - "STRICT" -> StrictValidationStrategy
     * - "LENIENT" -> LenientValidationStrategy
     * 
     * @param strategies Map de estrategias inyectado por Spring
     */
    @Autowired
    public ValidationContext(Map<String, IValidationStrategy> strategies) {
        this.strategies = strategies;
        log.info("ValidationContext inicializado con {} estrategias: {}", 
            strategies.size(), strategies.keySet());
    }
    
    /**
     * Valida un request de creación usando la estrategia especificada.
     * 
     * @param request El request con los datos del usuario
     * @param strategyType Tipo de estrategia a usar
     * @throws ValidationException Si la validación falla
     * @throws IllegalArgumentException Si la estrategia no existe
     */
    public void validateForCreation(CreateUsuarioRequest request, ValidationStrategyType strategyType) 
            throws ValidationException {
        
        IValidationStrategy strategy = getStrategy(strategyType);
        
        log.debug("Validando creación con estrategia: {}", strategy.getStrategyName());
        strategy.validateForCreation(request);
    }
    
    /**
     * Valida un request de actualización usando la estrategia especificada.
     * 
     * @param request El request con los datos a actualizar
     * @param strategyType Tipo de estrategia a usar
     * @throws ValidationException Si la validación falla
     * @throws IllegalArgumentException Si la estrategia no existe
     */
    public void validateForUpdate(UpdateUsuarioRequest request, ValidationStrategyType strategyType) 
            throws ValidationException {
        
        IValidationStrategy strategy = getStrategy(strategyType);
        
        log.debug("Validando actualización con estrategia: {}", strategy.getStrategyName());
        strategy.validateForUpdate(request);
    }
    
    /**
     * Obtiene una estrategia por su tipo.
     * 
     * @param strategyType Tipo de estrategia
     * @return La estrategia correspondiente
     * @throws IllegalArgumentException Si la estrategia no existe
     */
    private IValidationStrategy getStrategy(ValidationStrategyType strategyType) {
        IValidationStrategy strategy = strategies.get(strategyType.name());
        
        if (strategy == null) {
            String availableStrategies = String.join(", ", strategies.keySet());
            throw new IllegalArgumentException(
                String.format("Estrategia de validación '%s' no encontrada. Disponibles: %s", 
                    strategyType, availableStrategies)
            );
        }
        
        return strategy;
    }
    
    /**
     * Obtiene todas las estrategias disponibles (útil para debugging).
     * 
     * @return Map con todas las estrategias disponibles
     */
    public Map<String, IValidationStrategy> getAvailableStrategies() {
        return Map.copyOf(strategies);
    }
    
    /**
     * Enum que define los tipos de estrategias de validación disponibles.
     * 
     * Usar un enum en lugar de Strings reduce errores y mejora el autocompletado.
     */
    public enum ValidationStrategyType {
        /**
         * Validación estricta: Para administradores y usuarios privilegiados.
         * Contraseña mínimo 12 caracteres, caracteres especiales obligatorios.
         */
        STRICT,
        
        /**
         * Validación leniente: Para usuarios normales.
         * Contraseña mínimo 8 caracteres, sin caracteres especiales obligatorios.
         */
        LENIENT
    }
}
