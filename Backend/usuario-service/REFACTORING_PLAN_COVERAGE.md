# Plan de Refactorización para Cobertura de Pruebas
## usuario-service - Análisis y Estrategia Capa por Capa

**Fecha:** 25 de febrero de 2026  
**Objetivo:** Refactorizar el código para mejorar la testabilidad y cobertura de pruebas sin cambiar funcionalidad ni arquitectura MVC

---

## 📊 Análisis Arquitectónico Actual

### Estructura del Proyecto (MVC + Capas adicionales)

```
usuario-service/
├── controller/          → Capa de presentación (REST endpoints)
│   └── UsuarioController.java
├── service/            → Capa de lógica de negocio
│   ├── IUsuarioService.java
│   └── UsuarioService.java
├── persistence/        → Capa de acceso a datos (abstracción)
│   ├── IUserPersistence.java
│   ├── UserJpaPersistence.java
│   └── CachedUserPersistenceDecorator.java
├── repository/         → Capa de Spring Data JPA
│   └── UserJpaRepository.java
├── entity/             → Entidades JPA
│   └── UserEntity.java
├── model/              → Modelos de dominio
│   └── User.java
├── dto/                → Data Transfer Objects
│   ├── CreateUsuarioRequest.java
│   ├── UpdateUsuarioRequest.java
│   └── UsuarioResponse.java
├── validation/         → Validación de negocio (Strategy Pattern)
│   ├── IValidationStrategy.java
│   ├── ValidationContext.java
│   ├── LenientValidationStrategy.java
│   └── StrictValidationStrategy.java
├── exception/          → Manejo de excepciones
│   ├── GlobalExceptionHandler.java
│   ├── UsuarioNotFoundException.java
│   └── UsuarioYaExisteException.java
└── mapper/             → Mapeo entre capas
    └── UserEntityMapper.java
```

---

## 🔍 Análisis de Testabilidad por Capa

### ✅ **Capa 1: Controller** - BUENA TESTABILIDAD
**Archivo:** `UsuarioController.java`

**Puntos Fuertes:**
- ✅ Inyección de dependencias con `@RequiredArgsConstructor`
- ✅ Controlador "thin" - delega toda lógica al servicio
- ✅ Uso de ResponseEntity con códigos HTTP correctos
- ✅ Manejo de excepciones delegado a GlobalExceptionHandler

**Oportunidades de Mejora:**
1. **Mapeo DTO → Response en el controller:**
   ```java
   // Actual: mapeo inline
   .stream().map(UsuarioResponse::from).collect(Collectors.toList());
   
   // Propuesta: extraer a método privado para clarity
   private List<UsuarioResponse> mapToResponses(Collection<User> users)
   ```

2. **Log redundante con API_PATH constante:**
   - Simplificar logging para mejor legibilidad en tests

**Tests Actuales:**
- ✅ `UsuarioControllerObtenerTodosTest.java` - Cobertura parcial

**Tests Faltantes:**
- ❌ Tests para POST `/v1/usuarios`
- ❌ Tests para GET `/v1/usuarios/{identificador}`
- ❌ Tests para PUT `/v1/usuarios/{id}`
- ❌ Tests para PATCH `/v1/usuarios/{id}`
- ❌ Tests para DELETE `/v1/usuarios/{id}`
- ❌ Tests de validación de entrada (@Valid)

---

### ✅ **Capa 2: Service** - TESTABILIDAD MEDIA-ALTA
**Archivo:** `UsuarioService.java`

**Puntos Fuertes:**
- ✅ Inyección de dependencias clara
- ✅ Separación de responsabilidades (Strategy Pattern para validación)
- ✅ Logging detallado
- ✅ Manejo de Optional correctamente

**Oportunidades de Mejora:**

1. **Método `obtenerPorIdentificador` tiene lógica compleja:**
   ```java
   // Actual: mezcla detección de tipo + búsqueda
   public Optional<User> obtenerPorIdentificador(String identificador) {
       if (identificador == null || identificador.isBlank()) {
           return Optional.empty();
       }
       if (identificador.contains("@")) {
           return obtenerPorEmail(identificador);
       }
       try {
           int id = Integer.parseInt(identificador);
           return obtenerPorId(id);
       } catch (NumberFormatException e) {
           return Optional.empty();
       }
   }
   ```
   
   **Refactoring propuesto:**
   - Extraer lógica de detección a método privado: `isEmail(String)`
   - Extraer lógica de parseo a método privado: `tryParseId(String)`
   - Simplifica tests unitarios

2. **Método `actualizar` y `actualizarParcial` son casi idénticos:**
   ```java
   @Override
   public Optional<User> actualizarParcial(int id, UpdateUsuarioRequest request) {
       return actualizar(id, request);
   }
   ```
   - Consolidar o documentar diferencia semántica

3. **Validación duplicada de email en `actualizar`:**
   - Extraer validación de email único a método privado
   - Permite testear validación independientemente

**Tests Actuales:**
- ✅ `UsuarioServiceObtenerTodosTest.java` - Cobertura básica

**Tests Faltantes:**
- ❌ Tests para `obtenerPorIdentificador` (email vs ID vs inválido)
- ❌ Tests para `actualizar` completo
- ❌ Tests para `actualizarParcial`
- ❌ Tests para `eliminar`
- ❌ Tests de integración con ValidationContext
- ❌ Tests de casos edge (null, vacío, etc.)

---

### ⚠️ **Capa 3: Persistence** - TESTABILIDAD MEDIA
**Archivo:** `UserJpaPersistence.java`

**Puntos Fuertes:**
- ✅ Implementa interfaz `IUserPersistence` (fácil de mockear)
- ✅ Usa mapper para conversión entity ↔ domain

**Oportunidades de Mejora:**

1. **Método `partialUpdate` con lógica manual:**
   ```java
   public User partialUpdate(int id, Map<String, Object> updates) {
       return jpaRepository.findById(id)
           .map(existing -> {
               if (updates.containsKey("name")) {
                   existing.setName((String) updates.get("name"));
               }
               // ... repetido para cada campo
           })
   }
   ```
   
   **Problema:** Difícil de mantener, propenso a errores, hard to test
   
   **Refactoring propuesto:**
   - Extraer lógica de aplicación de updates a métodos privados:
     ```java
     private void applyNameUpdate(UserEntity entity, Map<String, Object> updates)
     private void applyPasswordUpdate(UserEntity entity, Map<String, Object> updates)
     // etc.
     ```
   - O usar un `UpdateApplier` strategy pattern

2. **Conversión booleana manual:**
   ```java
   Boolean.parseBoolean(String.valueOf(updates.get("active")))
   ```
   - Extraer a método utilitario: `parseBoolean(Object value)`

3. **Logging mezclado con lógica:**
   - Simplificar para tests más limpios

**Tests Actuales:**
- ✅ `UserJpaPersistenceIntegrationTest.java` - Test de integración

**Tests Faltantes:**
- ❌ Tests unitarios con mock de JpaRepository
- ❌ Tests de `partialUpdate` con diferentes combinaciones
- ❌ Tests de conversión entity ↔ domain

---

### ✅ **Capa 4: Repository** - TESTABILIDAD ALTA
**Archivo:** `UserJpaRepository.java` (Spring Data JPA)

**Estado:**
- ✅ Interfaz de Spring Data - altamente testable
- ✅ Ya tiene tests de integración con H2

**Tests Actuales:**
- ✅ `UserRepositoryTest.java`
- ✅ `UserRepositoryFindAllActiveTest.java`

**Acción:** ✅ NO REQUIERE REFACTORING

---

### ✅ **Capa 5: DTOs** - TESTABILIDAD ALTA
**Archivos:** `CreateUsuarioRequest.java`, `UpdateUsuarioRequest.java`, `UsuarioResponse.java`

**Puntos Fuertes:**
- ✅ Uso de Lombok (reduce boilerplate)
- ✅ Validaciones Jakarta en lugar correcto
- ✅ Factory method `from()` en UsuarioResponse

**Oportunidades de Mejora:**
1. **UsuarioResponse.from() sin null-safety:**
   ```java
   public static UsuarioResponse from(User usuario) {
       return UsuarioResponse.builder()
           .id(usuario.getId())
           .nombre(usuario.getName())
           // ... sin validar null
   }
   ```
   
   **Refactoring:**
   ```java
   public static UsuarioResponse from(User usuario) {
       if (usuario == null) {
           throw new IllegalArgumentException("User cannot be null");
       }
       return UsuarioResponse.builder()...
   }
   ```

**Tests Faltantes:**
- ❌ Tests de validación Jakarta (CreateUsuarioRequest)
- ❌ Tests de factory method (UsuarioResponse.from)
- ❌ Tests de serialización JSON

---

### ⚠️ **Capa 6: Validation** - TESTABILIDAD MEDIA-ALTA
**Archivos:** `ValidationContext.java`, `LenientValidationStrategy.java`, `StrictValidationStrategy.java`

**Puntos Fuertes:**
- ✅ Excelente uso de Strategy Pattern
- ✅ Separación de responsabilidades clara

**Oportunidades de Mejora:**

1. **ValidationContext con Map de estrategias:**
   ```java
   @Autowired
   public ValidationContext(Map<String, IValidationStrategy> strategies) {
       this.strategies = strategies;
   }
   ```
   - Añadir validación que estrategias requeridas existan
   - Logging en constructor para debugging

2. **Manejo de estrategia inexistente:**
   - Añadir método `hasStrategy(ValidationStrategyType type)`
   - Mejor manejo de error cuando estrategia no existe

**Tests Faltantes:**
- ❌ Tests para ValidationContext (selección de estrategia)
- ❌ Tests para LenientValidationStrategy
- ❌ Tests para StrictValidationStrategy
- ❌ Tests de integración Strategy ↔ Service

---

### ✅ **Capa 7: Exception Handling** - TESTABILIDAD ALTA
**Archivo:** `GlobalExceptionHandler.java`

**Estado:**
- ✅ Centralizado y bien estructurado
- ✅ ResponseEntity con ErrorResponse correcto

**Oportunidades de Mejora:**
1. Extraer construcción de ErrorResponse a builder methods:
   ```java
   private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message, WebRequest request)
   ```

**Tests Faltantes:**
- ❌ Tests unitarios de cada @ExceptionHandler
- ❌ Tests de integración con controllers

---

## 🎯 Plan de Refactorización Paso a Paso

### **FASE 1: Preparación y Setup (Sin cambios funcionales)**

#### Paso 1.1: Configurar herramientas de cobertura
```bash
# Verificar JaCoCo en pom.xml
# Establecer meta de cobertura (ej: 80%)
```

#### Paso 1.2: Ejecutar baseline de cobertura actual
```bash
mvn clean test jacoco:report
# Documentar cobertura inicial por paquete
```

---

### **FASE 2: Refactoring Capa Controller** (Semana 1)

#### Paso 2.1: Extraer método de mapeo
**Archivo:** `UsuarioController.java`
```java
// Antes
return ResponseEntity.ok(
    usuarioService.obtenerTodos()
        .stream()
        .map(UsuarioResponse::from)
        .collect(Collectors.toList())
);

// Después
return ResponseEntity.ok(mapToResponses(usuarioService.obtenerTodos()));

// Nuevo método privado
private List<UsuarioResponse> mapToResponses(Collection<User> users) {
    return users.stream()
        .map(UsuarioResponse::from)
        .collect(Collectors.toList());
}
```

**Tests a crear:**
- `UsuarioControllerCrearTest.java` - POST endpoint
- `UsuarioControllerObtenerPorIdTest.java` - GET /{id}
- `UsuarioControllerActualizarTest.java` - PUT /{id}
- `UsuarioControllerActualizarParcialTest.java` - PATCH /{id}
- `UsuarioControllerEliminarTest.java` - DELETE /{id}

**Meta de cobertura Controller:** 90%

---

### **FASE 3: Refactoring Capa Service** (Semana 2)

#### Paso 3.1: Refactorizar `obtenerPorIdentificador`
**Archivo:** `UsuarioService.java`

```java
// Extraer métodos privados
private boolean isEmail(String identificador) {
    return identificador != null && identificador.contains("@");
}

private Optional<Integer> tryParseId(String identificador) {
    try {
        return Optional.of(Integer.parseInt(identificador));
    } catch (NumberFormatException e) {
        return Optional.empty();
    }
}

// Método principal simplificado
@Override
public Optional<User> obtenerPorIdentificador(String identificador) {
    if (identificador == null || identificador.isBlank()) {
        return Optional.empty();
    }
    
    if (isEmail(identificador)) {
        return obtenerPorEmail(identificador);
    }
    
    return tryParseId(identificador)
        .flatMap(this::obtenerPorId);
}
```

#### Paso 3.2: Extraer validación de email único
```java
private void validateEmailUniqueness(String email, Integer excludeUserId) {
    User existing = userRepository.findByEmail(email);
    if (existing != null && !existing.getId().equals(excludeUserId)) {
        throw new UsuarioYaExisteException("El email " + email + " ya está registrado");
    }
}

// Usar en crear y actualizar
validateEmailUniqueness(request.getEmail(), null); // crear
validateEmailUniqueness(request.getEmail(), id); // actualizar
```

#### Paso 3.3: Consolidar actualizar/actualizarParcial
```java
@Override
public Optional<User> actualizarParcial(int id, UpdateUsuarioRequest request) {
    log.debug("Actualizando parcialmente usuario ID: {}", id);
    // Lógica específica de PATCH (sin validaciones de campos requeridos)
    return actualizarInterno(id, request, false);
}

@Override
public Optional<User> actualizar(int id, UpdateUsuarioRequest request) {
    log.info("Actualizando usuario ID: {}", id);
    // PUT requiere validación completa
    return actualizarInterno(id, request, true);
}

private Optional<User> actualizarInterno(int id, UpdateUsuarioRequest request, boolean fullUpdate) {
    // Lógica compartida
}
```

**Tests a crear:**
- `UsuarioServiceObtenerPorIdentificadorTest.java`
- `UsuarioServiceCrearTest.java` (ampliar HUUSR03)
- `UsuarioServiceActualizarTest.java`
- `UsuarioServiceActualizarParcialTest.java`
- `UsuarioServiceEliminarTest.java`
- `UsuarioServiceValidacionEmailTest.java`

**Meta de cobertura Service:** 85%

---

### **FASE 4: Refactoring Capa Persistence** (Semana 3)

#### Paso 4.1: Refactorizar `partialUpdate`
**Archivo:** `UserJpaPersistence.java`

```java
// Extraer aplicadores de updates
private void applyUpdates(UserEntity entity, Map<String, Object> updates) {
    applyIfPresent(updates, "name", entity::setName);
    applyIfPresent(updates, "password", entity::setPassword);
    applyIfPresent(updates, "mail", entity::setMail);
    applyIfPresent(updates, "active", value -> entity.setActive(parseBoolean(value)));
}

private <T> void applyIfPresent(Map<String, Object> updates, String key, Consumer<T> setter) {
    if (updates.containsKey(key)) {
        setter.accept((T) updates.get(key));
    }
}

private boolean parseBoolean(Object value) {
    if (value instanceof Boolean) return (Boolean) value;
    return Boolean.parseBoolean(String.valueOf(value));
}

// Método principal simplificado
@Override
public User partialUpdate(int id, Map<String, Object> updates) {
    return jpaRepository.findById(id)
        .map(existing -> {
            applyUpdates(existing, updates);
            return mapper.toDomain(jpaRepository.save(existing));
        })
        .orElse(null);
}
```

**Tests a crear:**
- `UserJpaPersistenceUnitTest.java` (con mocks)
- `UserJpaPersistencePartialUpdateTest.java`

**Meta de cobertura Persistence:** 80%

---

### **FASE 5: Tests de DTOs y Validation** (Semana 4)

#### Paso 5.1: Añadir null-safety a UsuarioResponse.from()
**Archivo:** `UsuarioResponse.java`
```java
public static UsuarioResponse from(User usuario) {
    Objects.requireNonNull(usuario, "User cannot be null");
    return UsuarioResponse.builder()
        .id(usuario.getId())
        .nombre(usuario.getName())
        .email(usuario.getMail())
        .activo(usuario.isActive())
        .build();
}
```

#### Paso 5.2: Crear tests de validación
**Tests a crear:**
- `CreateUsuarioRequestValidationTest.java`
- `UpdateUsuarioRequestValidationTest.java`
- `UsuarioResponseTest.java` (factory method + null safety)
- `UsuarioResponseJsonTest.java` (serialización)

**Meta de cobertura DTOs:** 95%

---

### **FASE 6: Tests de Validation Strategy** (Semana 4)

**Tests a crear:**
- `ValidationContextTest.java`
- `LenientValidationStrategyTest.java`
- `StrictValidationStrategyTest.java`
- `ValidationIntegrationTest.java` (Strategy ↔ Service)

**Meta de cobertura Validation:** 85%

---

### **FASE 7: Tests de Exception Handling** (Semana 5)

#### Paso 7.1: Refactorizar construcción de ErrorResponse
**Archivo:** `GlobalExceptionHandler.java`
```java
private ErrorResponse buildErrorResponse(
        HttpStatus status, 
        String error, 
        String message, 
        WebRequest request) {
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(error)
        .message(message)
        .path(extractPath(request))
        .build();
}

private String extractPath(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
}
```

**Tests a crear:**
- `GlobalExceptionHandlerTest.java`
- `GlobalExceptionHandlerIntegrationTest.java`

**Meta de cobertura Exception Handling:** 90%

---

### **FASE 8: Tests de Integración** (Semana 5-6)

**Tests a crear:**
- `UsuarioEndToEndTest.java` - Flujo completo CRUD
- `UsuarioValidationIntegrationTest.java` - Validaciones end-to-end
- `UsuarioExceptionFlowTest.java` - Flujos de error completos

---

## 📈 Metas de Cobertura por Capa

| Capa | Meta Cobertura | Prioridad |
|------|----------------|-----------|
| Controller | 90% | Alta |
| Service | 85% | Alta |
| Persistence | 80% | Media |
| Repository | 90% | Alta (ya existe) |
| DTOs | 95% | Alta |
| Validation | 85% | Media-Alta |
| Exception Handling | 90% | Media |
| **TOTAL PROYECTO** | **85%** | - |

---

## ✅ Checklist de Refactoring

### Controller
- [ ] Extraer mapeo de DTOs a métodos privados
- [ ] Crear tests para todos los endpoints (POST, GET, PUT, PATCH, DELETE)
- [ ] Tests de validación de entrada
- [ ] Tests de manejo de errores

### Service
- [ ] Refactorizar `obtenerPorIdentificador` (extraer `isEmail`, `tryParseId`)
- [ ] Extraer validación de email único
- [ ] Consolidar `actualizar`/`actualizarParcial`
- [ ] Tests completos de todos los métodos
- [ ] Tests de casos edge (null, vacío, etc.)

### Persistence
- [ ] Refactorizar `partialUpdate` (extraer `applyUpdates`, `parseBoolean`)
- [ ] Tests unitarios con mocks
- [ ] Tests de conversión entity ↔ domain

### DTOs
- [ ] Añadir null-safety a `UsuarioResponse.from()`
- [ ] Tests de validación Jakarta
- [ ] Tests de factory methods
- [ ] Tests de serialización JSON

### Validation
- [ ] Añadir validación de estrategias en constructor
- [ ] Tests de ValidationContext
- [ ] Tests de cada estrategia
- [ ] Tests de integración

### Exception Handling
- [ ] Extraer construcción de ErrorResponse
- [ ] Tests unitarios de cada handler
- [ ] Tests de integración

---

## 🚀 Estrategia de Ejecución

### Principios
1. **Un paso a la vez:** Completar cada fase antes de pasar a la siguiente
2. **Tests primero:** Crear tests antes de refactorizar (cuando sea posible)
3. **Refactoring pequeño:** Commits pequeños y atómicos
4. **Verificación continua:** Ejecutar suite completa de tests después de cada cambio
5. **Documentación:** Actualizar javadocs según sea necesario

### Flujo TDD para cada refactoring
```
1. Identificar código a refactorizar
2. Escribir tests del comportamiento actual (characterization tests)
3. Verificar que tests pasan ✅
4. Refactorizar código
5. Verificar que tests siguen pasando ✅
6. Añadir tests adicionales para casos edge
7. Verificar cobertura mejorada ✅
8. Commit
```

---

## 🎓 Beneficios Esperados

### Testabilidad
- ✅ Métodos más pequeños y focalizados
- ✅ Reducción de dependencias ocultas
- ✅ Mayor uso de métodos privados testeables indirectamente

### Mantenibilidad
- ✅ Código más legible y autodocumentado
- ✅ Menor duplicación de lógica
- ✅ Cambios futuros más seguros (respaldo de tests)

### Calidad
- ✅ Detección temprana de bugs
- ✅ Refactoring seguro con red de seguridad
- ✅ Documentación viva del comportamiento esperado

### Cobertura
- ✅ De ~40% actual a 85% objetivo
- ✅ Cobertura de líneas, branches y paths
- ✅ Identificación de dead code

---

## 📝 Notas Importantes

### Lo que NO se cambiará
- ❌ Arquitectura MVC
- ❌ Nombres de endpoints REST
- ❌ Contratos de API (requests/responses)
- ❌ Funcionalidad existente
- ❌ Estructura de base de datos

### Lo que SÍ se cambiará
- ✅ Organización interna de métodos
- ✅ Extracción de lógica a métodos privados
- ✅ Mejora de null-safety
- ✅ Consolidación de código duplicado
- ✅ Adición de tests comprehensivos

---

## 🔧 Herramientas Necesarias

- ✅ JaCoCo (ya configurado en pom.xml)
- ✅ JUnit 5
- ✅ Mockito
- ✅ Spring Boot Test
- ✅ H2 Database (tests)
- ✅ AssertJ (opcional, para assertions más fluidas)

---

## 📅 Timeline Estimado

| Fase | Duración | Entregable |
|------|----------|------------|
| Fase 1 | 1 día | Baseline de cobertura |
| Fase 2 | 3-4 días | Controller refactorizado + tests |
| Fase 3 | 4-5 días | Service refactorizado + tests |
| Fase 4 | 3-4 días | Persistence refactorizado + tests |
| Fase 5 | 2-3 días | DTOs + tests |
| Fase 6 | 2-3 días | Validation + tests |
| Fase 7 | 2 días | Exception handling + tests |
| Fase 8 | 3-4 días | Tests de integración E2E |
| **TOTAL** | **~4 semanas** | Cobertura 85% |

---

## 🎯 Próximos Pasos Inmediatos

1. **Revisar y aprobar este plan** con el equipo
2. **Configurar JaCoCo** y generar reporte baseline
3. **Crear rama de refactoring:** `refactor/coverage-improvement`
4. **Iniciar Fase 2:** Refactoring de Controller
5. **Establecer daily reviews** para seguimiento

---

**Autor:** GitHub Copilot  
**Revisado por:** [Pendiente]  
**Aprobado por:** [Pendiente]

