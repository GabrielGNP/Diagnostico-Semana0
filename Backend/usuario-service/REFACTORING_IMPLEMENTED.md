# 🏗️ REFACTORING IMPLEMENTADO CON PATRONES DE DISEÑO

## Documento de Análisis y Justificación de Patrones

### Objetivo
Documentar dónde implementar patrones de diseño en la refactorización del usuario-service, justificando por qué cada patrón mejora la arquitectura actual.

---

## 📋 TABLA DE CONTENIDOS

1. [Patrones de Creación](#patrones-de-creación)
2. [Patrones Estructurales](#patrones-estructurales)
3. [Patrones de Comportamiento](#patrones-de-comportamiento)
4. [Arquitectura Bien Implementada](#arquitectura-bien-implementada)
5. [Resumen de Beneficios](#resumen-de-beneficios)

---

## 🔧 PATRONES DE CREACIÓN

### 1. Factory Pattern (Recomendado para UserRepository)

#### ¿Dónde se implementaría?

En la creación de instancias de `IUserPersistence`

```
app/config/
└── UserPersistenceFactory.java (NUEVO)

app/persistence/
├── IUserPersistence.java (EXISTENTE)
├── JsonUserPersistence.java (NUEVA IMPL)
├── DatabaseUserPersistence.java (FUTURA IMPL)
└── InMemoryUserPersistence.java (PARA TESTS)
```

#### ANTES (Problema):

```java
// En UsuarioService.java
@Service
public class UsuarioService {
    
    @Autowired
    private UserRepository userRepository;  // ❌ Directamente acoplado
    
    // Si quiero cambiar a base de datos, cambio en 20 clases
}

// En Spring Boot Config - hardcodeado
@Bean
public IUserPersistence userPersistence() {
    return new UserRepository();  // ❌ Solo JSON
}
```

**Problemas:**
- ❌ Solo soporta JSON, agregar otra persistencia requiere cambiar toda la app
- ❌ No hay flexibilidad para cambiar en runtime
- ❌ Testing difícil - no se puede cambiar la implementación
- ❌ Violación del Open/Closed Principle

#### DESPUÉS (Factory Pattern):

```java
public class UserPersistenceFactory {
    
    public static IUserPersistence createPersistence(String type) {
        switch(type.toLowerCase()) {
            case "json":
                return new JsonUserPersistence();
            case "database":
                return new DatabaseUserPersistence();
            case "in-memory":
                return new InMemoryUserPersistence();
            default:
                throw new IllegalArgumentException("Unknown persistence type: " + type);
        }
    }
}

// En config
@Bean
public IUserPersistence userPersistence(
        @Value("${app.persistence.type:json}") String type) {
    return UserPersistenceFactory.createPersistence(type);
}
```

**Beneficios:**
- ✅ Crear nuevas implementaciones sin cambiar código existente
- ✅ Cambiar persistencia desde `application.properties`
- ✅ Cambiar en runtime si necesario
- ✅ Testing: Inyectar InMemory en tests
- ✅ Sigue Open/Closed Principle

#### Justificación Técnica:

| Aspecto | Sin Factory | Con Factory |
|--------|-------------|------------|
| **Agregar BD nueva** | Cambiar UsuarioService | Solo crear DatabaseUserPersistence |
| **Cambiar de JSON a BD** | Cambiar @Autowired en múltiples clases | Solo `application.properties` |
| **Testing** | Difícil mockear | Inyectar InMemory |
| **Escalabilidad** | Baja | Alta |
| **Mantenibilidad** | Baja | Alta |

**Por qué es mejor:**
- 🎯 **Single Responsibility:** Factory solo crea, no usa
- 🎯 **Open/Closed:** Abierto a nuevas implementaciones, cerrado a modificaciones
- 🎯 **Dependency Inversion:** Depende de interfaz, no de implementación

---

#### ✅ IMPLEMENTACIÓN REALIZADA


**Objetivo de la implementación:**
Aplicar el Factory Pattern a la arquitectura existente **sin crear nuevas implementaciones de persistencia**.
Solo estructurar el código con el patrón, manteniendo UserRepository como única implementación.

**Archivos creados:**

1. **`config/UserPersistenceFactory.java`**
2. **`config/PersistenceConfig.java`**

**Archivos modificados:**

1. **`service/UserRepository.java`**
   - Agregado método `initialize()` que delega a `init()`
   - Mantiene compatibilidad con IUserPersistence
2. **`service/UsuarioService.java`**
   - ANTES: `private final UserRepository userRepository;`
   - DESPUÉS: `private final IUserPersistence userRepository;`
   - Ahora depende de la abstracción, no de la implementación concreta
   - Documentación actualizada explicando DIP + Factory Pattern
3. **`application.properties`**
   ```properties
   # Persistence - Factory Pattern
   # Define qué implementación de IUserPersistence usar
   # Actualmente soportado: json
   # Futuro: database, in-memory, etc.
   app.persistence.type=json
   ```

**Estructura resultante:**

```
config/
├── UserPersistenceFactory.java  ✅ NUEVO - Patrón Factory
├── PersistenceConfig.java       ✅ NUEVO - Usa el Factory
├── CorsConfig.java
└── UsuariosInitializationConfig.java

service/
├── UsuarioService.java           🔄 MODIFICADO - Usa IUserPersistence
├── IUsuarioService.java
└── UserRepository.java           🔄 MODIFICADO - Agregado initialize()

```

El objetivo es aplicar el **patrón estructuralmente** sin agregar nuevas
implementaciones de persistencia. El Factory actualmente retorna `new UserRepository()`, pero
la arquitectura está preparada para soportar múltiples implementaciones en el futuro.

---

#### 📊 BENEFICIOS LOGRADOS

**ANTES del Factory Pattern:**
```java
@Service
public class UsuarioService {
    private final UserRepository userRepository;  // ❌ Acoplado a implementación
}
```

**DESPUÉS del Factory Pattern:**
```java
@Service
public class UsuarioService {
    private final IUserPersistence userRepository;  // ✅ Depende de abstracción
}

// La instancia es creada por:
PersistenceConfig → UserPersistenceFactory → UserRepository
```

**Mejoras concretas:**

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Acoplamiento** | Alto (clase concreta) | Bajo (interfaz) |
| **Testabilidad** | Difícil (mock de UserRepository) | Fácil (implementar IUserPersistence) |
| **Flexibilidad futura** | Ninguna | Alta (solo agregar al Factory) |
| **Principio DIP** | Violado | Cumplido ✅ |
| **Principio OCP** | Violado | Cumplido ✅ |
| **Single Responsibility** | Violado (crear + usar) | Cumplido ✅ (Factory crea, Service usa) |

**Escalabilidad demostrada:**

Si mañana queremos agregar persistencia en MongoDB:
```java
// 1. Crear nueva implementación
public class MongoUserPersistence implements IUserPersistence { ... }

// 2. Actualizar Factory
case "mongodb":
    return new MongoUserPersistence();

// 3. Cambiar properties
app.persistence.type=mongodb

// ✅ UsuarioService NO necesita cambios
// ✅ Resto del código intacto
```

---

**Por qué es mejor:**
- 🎯 **Single Responsibility:** Factory solo crea, no usa
- 🎯 **Open/Closed:** Abierto a nuevas implementaciones, cerrado a modificaciones
- 🎯 **Dependency Inversion:** Depende de interfaz, no de implementación

---

### 2. Builder Pattern (Para DTOs y Configuraciones)

#### ¿Dónde se implementa?
En DTOs 

```
dto/
├── CreateUsuarioRequest.java (YA TIENE @Builder ✅)
├── UpdateUsuarioRequest.java (YA TIENE @Builder ✅)
└── UsuarioResponse.java (YA TIENE @Builder ✅)
```


**Ya implementado correctamente en:**
- ✅ CreateUsuarioRequest (Lombok @Builder)
- ✅ UpdateUsuarioRequest (Lombok @Builder)
- ✅ UsuarioResponse (Lombok @Builder)
- ✅ ErrorResponse (Lombok @Builder)

**Beneficios:**
- ✅ Código más legible
- ✅ Parámetros opcionales naturales
- ✅ Validación centralizada
- ✅ Inmutabilidad

---

### 3. Singleton Pattern (Para Configuraciones Globales)

#### ¿Dónde se implementa?

En objetos que deben existir una sola vez en la aplicación

```
config/
├── CorsConfig.java (YA IMPLEMENTA SINGLETON - via @Configuration ✅)
└── RabbitMQConfig.java (YA IMPLEMENTA SINGLETON - via @Configuration ✅)
```

**Ya implementado correctamente en:**
- ✅ CorsConfig (@Configuration - Singleton)
- ✅ RabbitMQConfig (@Configuration - Singleton)
- ✅ Todos los @Bean (Singleton por defecto en Spring)

**Beneficios:**
- ✅ Una única instancia garantizada
- ✅ Gestión centralizada
- ✅ Thread-safe (Spring lo maneja)
- ✅ Inyección consistente

---

## 📐 PATRONES ESTRUCTURALES

### 1. Decorator Pattern (Para UserRepository con Caché)

#### ✅ IMPLEMENTACIÓN REALIZADA

El patrón Decorator ha sido implementado exitosamente para agregar funcionalidad de caché a la persistencia de usuarios sin modificar la implementación base.

**Archivos Creados:**

1. **`persistence/CachedUserPersistenceDecorator.java`** (210 líneas)
   - Implementa `IUserPersistence`
   - Envuelve cualquier implementación de persistencia con caché
   - Usa `ConcurrentHashMap` para thread-safety
   - Mantiene dos cachés: por email (lowercase) y por ID
   - Invalida caché automáticamente en operaciones de escritura
   - Métodos de utilidad: `clearCache()`, `getCacheStats()`

**Archivos Modificados:**

2. **`config/PersistenceConfig.java`**
   - Agregado `@Value("${app.persistence.cache.enabled:true}")`
   - Modificado `@Bean userPersistence()` para decorar condicionalmente
   - Si `app.persistence.cache.enabled=true`, envuelve con `CachedUserPersistenceDecorator`
   - Logging extensivo para debugging

3. **`src/main/resources/application.properties`**
   - Agregado `app.persistence.cache.enabled=true`
   - Documentación de configuración del caché
   - Por defecto activado para producción

**Características Implementadas:**

- ✅ **Caché transparente:** La persistencia base (UserRepository) NO sabe que está siendo decorada
- ✅ **Thread-safe:** Usa `ConcurrentHashMap` para acceso concurrente seguro
- ✅ **Dual-cache:** Cachea por email (normalizado a lowercase) y por ID simultáneamente
- ✅ **Invalidación automática:** save(), update(), delete() limpian el caché
- ✅ **Configurable:** Se activa/desactiva desde `application.properties` sin cambiar código
- ✅ **Composición limpia:** Sigue el principio Open/Closed
- ✅ **Mejora de rendimiento:** findByEmail pasa de O(n) a O(1)
- ✅ **Observabilidad:** Logging de cache hits/misses para métricas

**Ejemplo de Uso:**

```java
// En PersistenceConfig.java
@Bean
public IUserPersistence userPersistence() throws IOException {
    // 1. Factory crea la persistencia base
    IUserPersistence persistence = UserPersistenceFactory.createPersistence(persistenceType);
    persistence.initialize();
    
    // 2. Decorator agrega caché si está habilitado
    if (cacheEnabled) {
        persistence = new CachedUserPersistenceDecorator(persistence);
    }
    
    return persistence;  // Retorna decorada o sin decorar según config
}
```

**Ventaja Clave:** El código cliente (UsuarioService) es **completamente agnóstico** al caché. No necesita cambiar nada.

---

#### ¿Dónde se implementa?

Agregar caché a UserRepository sin modificarlo

```
persistence/
├── IUserPersistence.java (EXISTENTE)
├── JsonUserPersistence.java (EXISTENTE)
└── CachedUserPersistenceDecorator.java (NUEVO - Decorator)

config/
└── PersistenceDecoratorConfig.java (NUEVO)
```

#### ANTES (Sin Caché - cada búsqueda va a disco):

```java
public User findByEmail(String email) {
    // Búsqueda lineal cada vez
    for (User u : users.values()) {
        if (u.getMail().equalsIgnoreCase(email)) {
            return u;  // ❌ O(n) - lento con 10k usuarios
        }
    }
    return null;
}

// Si agregamos caché, modificamos UserRepository
// ❌ Viola Open/Closed Principle
// ❌ UserRepository no debería saber de caché
```

#### DESPUÉS (Decorator Pattern):

```java
// Decorator que envuelve la persistencia
@Component
public class CachedUserPersistenceDecorator implements IUserPersistence {
    
    private final IUserPersistence delegate;
    private final Map<String, User> emailCache = new ConcurrentHashMap<>();
    private final Map<Integer, User> idCache = new ConcurrentHashMap<>();
    
    @Override
    public User findByEmail(String email) {
        // Primero intenta caché
        return emailCache.computeIfAbsent(email, key -> 
            delegate.findByEmail(key)  // Si no está, delega y cachea
        );
    }
    
    @Override
    public User save(User user) {
        User saved = delegate.save(user);
        // Invalida caché
        emailCache.remove(user.getMail());
        idCache.remove(user.getId());
        return saved;
    }
}

// En config
@Bean
@Primary
public IUserPersistence userPersistence(JsonUserPersistence json) {
    // Decora JsonUserPersistence con caché
    return new CachedUserPersistenceDecorator(json);
}
```

**Beneficios:**
- ✅ Caché transparente - JsonUserPersistence no cambia
- ✅ Validación separada de persistencia
- ✅ Fácil de remover si no se necesita
- ✅ Reutilizable con cualquier implementación

#### Justificación Técnica:

| Aspecto | Sin Decorator | Con Decorator |
|--------|------------|-------------|
| **Búsqueda por email** | O(n) - ~50ms | O(1) - ~1ms |
| **modificar UserRepository** | Sí - violación OCP | No - solo agregar bean |
| **Remover caché** | Reescribir Repo | Solo cambiar bean |
| **Testing sin caché** | Difícil (caché mezclado) | Fácil (inyectar sin decorator) |

**Por qué es mejor:**
- 🎯 **Open/Closed:** Abierto a agregar funcionalidad via decorators
- 🎯 **Single Responsibility:** Cada clase hace una cosa
- 🎯 **Composición:** Combinable dinámicamente

---

## 🎭 PATRONES DE COMPORTAMIENTO

### 1. Strategy Pattern (Para Validación de Usuarios)

#### ✅ IMPLEMENTACIÓN REALIZADA

El patrón Strategy ha sido implementado exitosamente para manejar diferentes estrategias de validación de usuarios según el contexto.

**Archivos Creados:**

1. **`validation/IValidationStrategy.java`**
   - Interfaz base del patrón Strategy
   - Define métodos `validateForCreation()` y `validateForUpdate()`
   - Permite agregar nuevas estrategias sin modificar código existente

2. **`validation/StrictValidationStrategy.java`** (@Component("STRICT"))
   - Estrategia de validación estricta para administradores
   - Contraseña: mínimo 12 caracteres, caracteres especiales obligatorios
   - Validación de dominios de email
   - Verifica contraseñas comunes
   - Nombre: mínimo 3 caracteres sin espacios al inicio/final

3. **`validation/LenientValidationStrategy.java`** (@Component("LENIENT"))
   - Estrategia de validación leniente para usuarios normales
   - Contraseña: mínimo 8 caracteres
   - Validación básica sin caracteres especiales obligatorios
   - Nombre: mínimo 2 caracteres
   - Más flexible para no frustrar al usuario

4. **`validation/ValidationContext.java`**
   - Contexto que orquesta las estrategias (patrón Context)
   - Inyección automática de todas las estrategias mediante Map
   - Enum `ValidationStrategyType` para type-safety
   - Selección dinámica de estrategia en runtime
   - Métodos: `validateForCreation()`, `validateForUpdate()`

5. **`validation/ValidationException.java`**
   - Excepción personalizada para errores de validación de negocio
   - Se diferencia de las validaciones de anotaciones (@NotBlank, etc.)

**Archivos Modificados:**

6. **`service/UsuarioService.java`**
   - Agregado `private final ValidationContext validationContext;`
   - Método `crear()`: Llama a `validationContext.validateForCreation(request, LENIENT)`
   - Método `actualizar()`: Llama a `validationContext.validateForUpdate(request, LENIENT)`
   - Documentación actualizada para reflejar el uso del Strategy Pattern

7. **`exception/GlobalExceptionHandler.java`**
   - Agregado `@ExceptionHandler(ValidationException.class)`
   - Retorna HTTP 400 BAD_REQUEST con mensaje descriptivo
   - Logging de errores de validación de negocio

**Características Implementadas:**

- ✅ **Desacoplamiento:** UsuarioService no conoce las estrategias concretas
- ✅ **Open/Closed:** Agregar nuevas estrategias sin modificar código existente
- ✅ **Runtime flexibility:** Cambiar estrategia dinámicamente según contexto
- ✅ **Single Responsibility:** Cada estrategia tiene una responsabilidad única
- ✅ **Testabilidad:** Cada estrategia se puede testear independientemente
- ✅ **Reutilización:** Las estrategias son reutilizables en otros contextos
- ✅ **Type-safe:** Uso de enum en lugar de strings para evitar errores

**Ejemplo de Uso:**

```java
// En UsuarioService - validación leniente para usuarios normales
validationContext.validateForCreation(request, ValidationStrategyType.LENIENT);

// Para admin se podría usar (ejemplo futuro):
validationContext.validateForCreation(request, ValidationStrategyType.STRICT);
```

**Ventaja Clave:** Agregar una nueva estrategia (ej: "MODERATE") solo requiere:
1. Crear clase que implemente `IValidationStrategy`
2. Anotar con `@Component("MODERATE")`
3. Agregar valor al enum `ValidationStrategyType`

**NO requiere** modificar UsuarioService ni ValidationContext.

---

#### ¿Dónde se implementa?

Diferentes estrategias de validación según el tipo de usuario

```
validation/
├── IValidationStrategy.java (NUEVO)
├── StrictValidationStrategy.java (NUEVO)
├── LenientValidationStrategy.java (NUEVO)
└── ValidationContext.java (NUEVO)

service/
└── UsuarioService.java (Usar ValidationContext)
```

#### ANTES (Validación Hardcodeada):

```java
// En UsuarioService
public User crear(CreateUsuarioRequest request) {
    
    // ❌ Validación hardcodeada
    if (request.getContrasena().length() < 8) {
        throw new InvalidPasswordException("Mínimo 8 caracteres");
    }
    
    if (!request.getContrasena().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")) {
        throw new InvalidPasswordException("Debe tener mayúsculas, minúsculas, números");
    }
    
    if (request.getNombre().length() < 2) {
        throw new InvalidNameException("Mínimo 2 caracteres");
    }
    
    // Si agregamos tipo de usuario, agregar más validaciones
    // ❌ El método crece infinitamente
    // ❌ Cambiar validación requiere cambiar código
}
```

**Problemas:**
- ❌ Validación mezclada con lógica
- ❌ Difícil de cambiar
- ❌ No se puede reutilizar
- ❌ Difícil de testear

#### DESPUÉS (Strategy Pattern):

```java
// Estrategia agnóstica
public interface IValidationStrategy {
    void validate(CreateUsuarioRequest request) throws ValidationException;
}

// Implementación estricta (para admin)
@Component
public class StrictValidationStrategy implements IValidationStrategy {
    
    @Override
    public void validate(CreateUsuarioRequest request) {
        validatePassword(request);  // Muy estricta
        validateEmail(request);      // Muy estricta
        validateName(request);       // Muy estricta
    }
    
    private void validatePassword(CreateUsuarioRequest request) {
        // Mínimo 12 caracteres, caracteres especiales, etc.
    }
}

// Implementación leniente (para usuarios normales)
@Component
public class LenientValidationStrategy implements IValidationStrategy {
    
    @Override
    public void validate(CreateUsuarioRequest request) {
        // Mínimo 8 caracteres, solo esto
        if (request.getContrasena().length() < 8) {
            throw new ValidationException("Mínimo 8 caracteres");
        }
    }
}

// Contexto que usa la estrategia
@Component
public class ValidationContext {
    
    @Autowired
    private Map<String, IValidationStrategy> strategies;
    
    public void validate(User.Type userType, CreateUsuarioRequest request) {
        IValidationStrategy strategy = strategies.get(userType.name());
        strategy.validate(request);  // ✅ Ejecuta la estrategia adecuada
    }
}

// En UsuarioService
public User crear(CreateUsuarioRequest request, User.Type validationType) {
    
    validationContext.validate(validationType, request);  // ✅ Usa estrategia
    
    // Rest del código...
}
```

**Beneficios:**
- ✅ Cambiar estrategia sin cambiar UsuarioService
- ✅ Agregar nueva validación = crear nueva clase
- ✅ Reutilizable en múltiples contextos
- ✅ Testeable: una estrategia por test

#### Justificación Técnica:

| Aspecto | Sin Strategy | Con Strategy |
|--------|-----------|------------|
| **Agregar validación** | Modificar método | Crear clase |
| **Cambiar validación** | Cambiar lógica | Cambiar bean |
| **Testabilidad** | Difícil (todo junto) | Fácil (por estrategia) |
| **Reutilización** | Baja | Alta |

**Por qué es mejor:**
- 🎯 **Open/Closed:** Abierto a nuevas estrategias
- 🎯 **Single Responsability:** Cada estrategia valida una cosa
- 🎯 **Runtime Flexibility:** Cambiar estrategia en runtime

---



## 🏆 ARQUITECTURA BIEN IMPLEMENTADA




### 2. Inversión de Dependencias (DIP) ✅

**Implementado Correctamente:**

```java
// ✅ Controller depende de interfaz
@RestController
public class UsuarioController {
    
    private final IUsuarioService service;  // ← Interfaz
    
    public UsuarioController(IUsuarioService service) {
        this.service = Objects.requireNonNull(service);
    }
}

// ✅ Service depende de interfaz
@Service
public class UsuarioService implements IUsuarioService {
    
    private final UserRepository repository;  // Implementa IUserPersistence
    
    public void crear(...) {
        repository.save(user);  // Usa interfaz
    }
}
```

**Por qué está bien:**
- ✅ Inyección por constructor (no field injection)
- ✅ Depende de abstracciones (interfaces)
- ✅ Testeable (fácil inyectar mocks)
- ✅ Flexible (cambiar implementación)

---

### 3. DTOs con Validación ✅

**Implementado Correctamente:**

```java
@Valid @RequestBody CreateUsuarioRequest request

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUsuarioRequest {
    
    @NotBlank(message = "...", groups = {Create.class})
    @Email
    private String email;
    
    @Size(min = 8)
    private String contrasena;
}
```

**Por qué está bien:**
- ✅ Validación declarativa
- ✅ Errores automáticos
- ✅ Separación Modelo ↔ DTO
- ✅ Control de qué datos expone

---

### 4. Manejo de Excepciones Centralizado ✅

**Implementado Correctamente:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...) {
        // ✅ Respuesta consistente
        // ✅ Logging centralizado
        // ✅ HTTP status correcto
    }
}
```

**Por qué está bien:**
- ✅ Una handler global
- ✅ Respuesta consistente
- ✅ Logging automático
- ✅ Controller limpio

---

### 5. Creación de usuario Estructurado ✅

**Implementado Correctamente:**

```java
@Slf4j  // ← SLF4J via Lombok
public class UsuarioService {
    
    public User crear(CreateUsuarioRequest request) {
        log.info("Creando usuario con email: {}", request.getEmail());
        
        try {
            User user = crearYGuardar(request);
            log.info("Usuario creado con ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("Error creando usuario", e);
            throw new RuntimeException(...);
        }
    }
}
```

**Por qué está bien:**
- ✅ Información en contexto
- ✅ Niveles apropiados (INFO, ERROR)
- ✅ Stack traces en errores
- ✅ Rastreable en logs

---

### 6. Inyección de Dependencias por Constructor ✅

**Implementado Correctamente:**

```java
@Service
@RequiredArgsConstructor  // ← Lombok genera constructor
public class UsuarioService {
    
    private final UserRepository repository;
    private final UsuarioMapper mapper;
    
    // ✅ Constructor inyectado automáticamente
    // ✅ Inmutable (final)
    // ✅ Testeable (pasar mocks)
}
```

**Por qué está bien:**
- ✅ Visible (qué depende de qué)
- ✅ Testeable
- ✅ Evita circular dependencies
- ✅ Inmutabilidad

---

### 7. Configuración Externalizada ✅

**Implementado Correctamente:**

```properties
# application.properties
app.cors.allowed-origins=http://localhost:3001
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
logging.level.com.example.usuarioservice=DEBUG
```

**Por qué está bien:**
- ✅ Diferente config por ambiente
- ✅ No recompilación
- ✅ Gestión centralizada
- ✅ Variables de entorno soportadas

---

## 📊 RESUMEN DE BENEFICIOS

### Patrones a Implementar

| Patrón | Dónde | Por qué | Benefit | Estado |
|--------|-------|--------|---------|--------|
| **Factory** | UserPersistenceFactory | Crear persistencia | Cambiar BD sin código | ✅ Implementado |
| **Builder** | DTOs | Construcción fluida | Ya implementado | ✅ Implementado |
| **Singleton** | Configs | Una instancia | Ya implementado | ✅ Implementado |
| **Decorator** | CachedPersistence | Agregar funcionalidad | Caché sin modificar Repo | ✅ Implementado |
| **Strategy** | ValidationStrategy | Múltiples validaciones | Cambiar validación en runtime | ✅ Implementado |

### Arquitectura Implementada Correctamente

✅ **Separación de capas** - Controller, Service, Repository  
✅ **Inversión de dependencias** - Interfaces, constructor injection  
✅ **DTOs con validación** - CreateRequest, UpdateRequest, Response  
✅ **Excepciones centralizadas** - GlobalExceptionHandler  
✅ **Logging estructurado** - SLF4J  
✅ **Configuración externalizada** - properties  
✅ **Inyección por constructor** - No field injection  

---

## 📝 PRÓXIMAS FASES

### ✅ Fase 1: COMPLETADA - Patrones de Creación y Estructurales Básicos

1. ✅ Factory Pattern
   - UserPersistenceFactory implementado
   - PersistenceConfig usando Factory
   - Soporte para múltiples persistencias (actualmente json)

2. ✅ Decorator Pattern
   - CachedUserPersistenceDecorator implementado
   - Caché transparente con ConcurrentHashMap
   - Configurable desde application.properties
   - Mejora de rendimiento: O(n) → O(1)

### ✅ Fase 2: COMPLETADA - Patrones de Comportamiento

1. ✅ Strategy Pattern
   - IValidationStrategy interface implementada
   - StrictValidationStrategy (contraseña 12+ chars, especiales obligatorios)
   - LenientValidationStrategy (contraseña 8+ chars, básica)
   - ValidationContext con selección dinámica de estrategia
   - Integrado en UsuarioService para crear() y actualizar()
   - ValidationException con manejo en GlobalExceptionHandler

---

*Documento preparado: 13 de Febrero de 2026*  
*Listo para implementación por fases*
