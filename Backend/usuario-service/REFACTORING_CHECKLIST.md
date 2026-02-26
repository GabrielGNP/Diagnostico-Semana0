# ✅ Checklist Práctico de Refactorización
## usuario-service - Guía Paso a Paso

**Fecha de inicio:** _________  
**Objetivo:** Cobertura de 85% en 4 semanas  
**Baseline actual:** ~40%

---

## 🚀 FASE 1: Preparación (Día 1)

### ✅ Configuración Inicial

- [ ] **1.1** Verificar que JaCoCo está en `pom.xml`
  ```bash
  grep -A 5 "jacoco-maven-plugin" pom.xml
  ```

- [ ] **1.2** Generar reporte baseline de cobertura
  ```bash
  cd Backend/usuario-service
  ./mvnw clean test jacoco:report
  ```

- [ ] **1.3** Abrir reporte en navegador
  ```bash
  open target/site/jacoco/index.html
  ```

- [ ] **1.4** Documentar cobertura inicial
  - Controller: _____% 
  - Service: _____%
  - Persistence: _____%
  - Total: _____%

- [ ] **1.5** Crear rama de refactoring
  ```bash
  git checkout -b refactor/coverage-improvement
  ```

- [ ] **1.6** Commit inicial
  ```bash
  git add .
  git commit -m "docs: Add refactoring plan and baseline coverage report"
  ```

---

## 🎮 FASE 2: Refactoring Controller (Días 2-5)

### ✅ Paso 2.1: Extraer Mapeo de DTOs

- [ ] **2.1.1** Abrir `UsuarioController.java`

- [ ] **2.1.2** Añadir método privado `mapToResponses()`
  ```java
  private List<UsuarioResponse> mapToResponses(Collection<User> users) {
      return users.stream()
          .map(UsuarioResponse::from)
          .collect(Collectors.toList());
  }
  ```

- [ ] **2.1.3** Añadir método privado `mapToResponse()`
  ```java
  private UsuarioResponse mapToResponse(User user) {
      return UsuarioResponse.from(user);
  }
  ```

- [ ] **2.1.4** Refactorizar `obtenerTodos()` para usar `mapToResponses()`

- [ ] **2.1.5** Refactorizar `obtenerPorIdentificador()` para usar `mapToResponse()`

- [ ] **2.1.6** Ejecutar tests existentes
  ```bash
  ./mvnw test -Dtest=UsuarioControllerObtenerTodosTest
  ```

- [ ] **2.1.7** Verificar que pasan ✅

- [ ] **2.1.8** Commit
  ```bash
  git add src/main/java/com/example/usuarioservice/controller/UsuarioController.java
  git commit -m "refactor(controller): Extract DTO mapping to private methods"
  ```

---

### ✅ Paso 2.2: Tests POST Endpoint

- [ ] **2.2.1** Crear archivo `UsuarioControllerCrearTest.java`

- [ ] **2.2.2** Test: Crear usuario exitosamente (201 Created)
  ```java
  @Test
  @DisplayName("POST /v1/usuarios crea usuario y devuelve 201 Created")
  void crear_shouldReturn201Created_whenRequestIsValid() {
      // Given, When, Then
  }
  ```

- [ ] **2.2.3** Test: Validación falla (400 Bad Request)

- [ ] **2.2.4** Test: Email duplicado (409 Conflict)

- [ ] **2.2.5** Ejecutar tests
  ```bash
  ./mvnw test -Dtest=UsuarioControllerCrearTest
  ```

- [ ] **2.2.6** Verificar que pasan ✅

- [ ] **2.2.7** Commit
  ```bash
  git add src/test/java/com/example/usuarioservice/controller/UsuarioControllerCrearTest.java
  git commit -m "test(controller): Add POST endpoint tests (HU-USR-03)"
  ```

---

### ✅ Paso 2.3: Tests GET por ID Endpoint

- [ ] **2.3.1** Crear archivo `UsuarioControllerObtenerPorIdTest.java`

- [ ] **2.3.2** Test: Obtener por ID exitoso (200 OK)

- [ ] **2.3.3** Test: Obtener por email exitoso (200 OK)

- [ ] **2.3.4** Test: Usuario no encontrado (404 Not Found)

- [ ] **2.3.5** Test: Identificador inválido (404 Not Found)

- [ ] **2.3.6** Ejecutar tests y verificar ✅

- [ ] **2.3.7** Commit
  ```bash
  git commit -m "test(controller): Add GET /{id} endpoint tests (HU-USR-02)"
  ```

---

### ✅ Paso 2.4: Tests PUT Endpoint

- [ ] **2.4.1** Crear archivo `UsuarioControllerActualizarTest.java`

- [ ] **2.4.2** Test: Actualizar exitosamente (200 OK)

- [ ] **2.4.3** Test: Usuario no encontrado (404 Not Found)

- [ ] **2.4.4** Test: Email duplicado (409 Conflict)

- [ ] **2.4.5** Ejecutar tests y verificar ✅

- [ ] **2.4.6** Commit
  ```bash
  git commit -m "test(controller): Add PUT endpoint tests (HU-USR-04)"
  ```

---

### ✅ Paso 2.5: Tests PATCH Endpoint

- [ ] **2.5.1** Crear archivo `UsuarioControllerActualizarParcialTest.java`

- [ ] **2.5.2** Test: Actualizar parcialmente (200 OK)

- [ ] **2.5.3** Test: Solo un campo (200 OK)

- [ ] **2.5.4** Test: Usuario no encontrado (404 Not Found)

- [ ] **2.5.5** Ejecutar tests y verificar ✅

- [ ] **2.5.6** Commit
  ```bash
  git commit -m "test(controller): Add PATCH endpoint tests"
  ```

---

### ✅ Paso 2.6: Tests DELETE Endpoint

- [ ] **2.6.1** Crear archivo `UsuarioControllerEliminarTest.java`

- [ ] **2.6.2** Test: Eliminar exitosamente (204 No Content)

- [ ] **2.6.3** Test: Usuario no encontrado (404 Not Found)

- [ ] **2.6.4** Ejecutar tests y verificar ✅

- [ ] **2.6.5** Commit
  ```bash
  git commit -m "test(controller): Add DELETE endpoint tests (HU-USR-06)"
  ```

---

### ✅ Paso 2.7: Verificar Cobertura Controller

- [ ] **2.7.1** Generar reporte JaCoCo
  ```bash
  ./mvnw clean test jacoco:report
  ```

- [ ] **2.7.2** Verificar cobertura Controller
  - **Meta:** 90%
  - **Actual:** _____%

- [ ] **2.7.3** Si < 90%, identificar métodos sin cobertura

- [ ] **2.7.4** Añadir tests faltantes

- [ ] **2.7.5** Commit final de fase
  ```bash
  git commit -m "test(controller): Complete controller test coverage (90%)"
  ```

---

## 🧠 FASE 3: Refactoring Service (Días 6-10)

### ✅ Paso 3.1: Refactorizar `obtenerPorIdentificador`

- [ ] **3.1.1** Abrir `UsuarioService.java`

- [ ] **3.1.2** Añadir método privado `isEmail()`
  ```java
  private boolean isEmail(String identificador) {
      return identificador != null && identificador.contains("@");
  }
  ```

- [ ] **3.1.3** Añadir método privado `tryParseId()`
  ```java
  private Optional<Integer> tryParseId(String identificador) {
      try {
          return Optional.of(Integer.parseInt(identificador));
      } catch (NumberFormatException e) {
          log.warn("Identificador inválido: {}", identificador);
          return Optional.empty();
      }
  }
  ```

- [ ] **3.1.4** Refactorizar `obtenerPorIdentificador()` usando métodos privados

- [ ] **3.1.5** Ejecutar tests existentes
  ```bash
  ./mvnw test -Dtest=UsuarioService*Test
  ```

- [ ] **3.1.6** Verificar que pasan ✅

- [ ] **3.1.7** Commit
  ```bash
  git commit -m "refactor(service): Simplify obtenerPorIdentificador logic"
  ```

---

### ✅ Paso 3.2: Extraer Validación Email Único

- [ ] **3.2.1** Añadir método privado `validateEmailUniqueness()`
  ```java
  private void validateEmailUniqueness(String email, Integer excludeUserId) {
      User existingUser = userRepository.findByEmail(email);
      if (existingUser != null && !existingUser.getId().equals(excludeUserId)) {
          throw new UsuarioYaExisteException("El email " + email + " ya está registrado");
      }
  }
  ```

- [ ] **3.2.2** Refactorizar `crear()` para usar `validateEmailUniqueness(email, null)`

- [ ] **3.2.3** Refactorizar `actualizar()` para usar `validateEmailUniqueness(email, id)`

- [ ] **3.2.4** Ejecutar tests y verificar ✅

- [ ] **3.2.5** Commit
  ```bash
  git commit -m "refactor(service): Extract email uniqueness validation (DRY)"
  ```

---

### ✅ Paso 3.3: Consolidar actualizar/actualizarParcial

- [ ] **3.3.1** Añadir método privado `actualizarInterno()`
  ```java
  private Optional<User> actualizarInterno(int id, UpdateUsuarioRequest request, boolean fullUpdate)
  ```

- [ ] **3.3.2** Añadir método privado `applyUpdates()`
  ```java
  private void applyUpdates(User usuario, UpdateUsuarioRequest request)
  ```

- [ ] **3.3.3** Refactorizar `actualizar()` para usar `actualizarInterno(id, request, true)`

- [ ] **3.3.4** Refactorizar `actualizarParcial()` para usar `actualizarInterno(id, request, false)`

- [ ] **3.3.5** Ejecutar tests y verificar ✅

- [ ] **3.3.6** Commit
  ```bash
  git commit -m "refactor(service): Consolidate actualizar/actualizarParcial logic"
  ```

---

### ✅ Paso 3.4: Tests Service - obtenerPorIdentificador

- [ ] **3.4.1** Crear archivo `UsuarioServiceObtenerPorIdentificadorTest.java`

- [ ] **3.4.2** Test: Buscar por email válido

- [ ] **3.4.3** Test: Buscar por ID válido

- [ ] **3.4.4** Test: Identificador inválido (no email ni ID)

- [ ] **3.4.5** Test: Identificador null

- [ ] **3.4.6** Test: Identificador vacío

- [ ] **3.4.7** Ejecutar tests y verificar ✅

- [ ] **3.4.8** Commit
  ```bash
  git commit -m "test(service): Add obtenerPorIdentificador comprehensive tests"
  ```

---

### ✅ Paso 3.5: Tests Service - crear

- [ ] **3.5.1** Ampliar `HUUSR03_CreateUserTests.java` o crear nuevo archivo

- [ ] **3.5.2** Test: Crear usuario exitosamente

- [ ] **3.5.3** Test: Email duplicado lanza excepción

- [ ] **3.5.4** Test: Validación falla lanza excepción

- [ ] **3.5.5** Test: Usuario se marca como activo por defecto

- [ ] **3.5.6** Ejecutar tests y verificar ✅

- [ ] **3.5.7** Commit
  ```bash
  git commit -m "test(service): Expand crear method test coverage"
  ```

---

### ✅ Paso 3.6: Tests Service - actualizar

- [ ] **3.6.1** Crear archivo `UsuarioServiceActualizarTest.java`

- [ ] **3.6.2** Test: Actualizar exitosamente

- [ ] **3.6.3** Test: Cambiar email a uno no duplicado

- [ ] **3.6.4** Test: Mantener mismo email (permitido)

- [ ] **3.6.5** Test: Email duplicado lanza excepción

- [ ] **3.6.6** Test: Usuario no encontrado retorna vacío

- [ ] **3.6.7** Ejecutar tests y verificar ✅

- [ ] **3.6.8** Commit
  ```bash
  git commit -m "test(service): Add actualizar method tests"
  ```

---

### ✅ Paso 3.7: Tests Service - eliminar

- [ ] **3.7.1** Crear archivo `UsuarioServiceEliminarTest.java`

- [ ] **3.7.2** Test: Eliminar usuario existente retorna true

- [ ] **3.7.3** Test: Eliminar usuario no existente retorna false

- [ ] **3.7.4** Ejecutar tests y verificar ✅

- [ ] **3.7.5** Commit
  ```bash
  git commit -m "test(service): Add eliminar method tests"
  ```

---

### ✅ Paso 3.8: Verificar Cobertura Service

- [ ] **3.8.1** Generar reporte JaCoCo
  ```bash
  ./mvnw clean test jacoco:report
  ```

- [ ] **3.8.2** Verificar cobertura Service
  - **Meta:** 85%
  - **Actual:** _____%

- [ ] **3.8.3** Si < 85%, identificar métodos sin cobertura

- [ ] **3.8.4** Añadir tests faltantes

- [ ] **3.8.5** Commit final de fase
  ```bash
  git commit -m "test(service): Complete service test coverage (85%)"
  ```

---

## 💾 FASE 4: Refactoring Persistence (Días 11-14)

### ✅ Paso 4.1: Refactorizar `partialUpdate`

- [ ] **4.1.1** Abrir `UserJpaPersistence.java`

- [ ] **4.1.2** Añadir método `applyUpdates()`
  ```java
  private void applyUpdates(UserEntity entity, Map<String, Object> updates)
  ```

- [ ] **4.1.3** Añadir método `applyIfPresent()`
  ```java
  private <T> void applyIfPresent(Map<String, Object> updates, String key, Consumer<T> setter)
  ```

- [ ] **4.1.4** Añadir método `applyIfPresentBoolean()`

- [ ] **4.1.5** Añadir método `parseBoolean()`
  ```java
  private Boolean parseBoolean(Object value)
  ```

- [ ] **4.1.6** Refactorizar `partialUpdate()` usando métodos extraídos

- [ ] **4.1.7** Ejecutar tests de integración existentes
  ```bash
  ./mvnw test -Dtest=UserJpaPersistenceIntegrationTest
  ```

- [ ] **4.1.8** Verificar que pasan ✅

- [ ] **4.1.9** Commit
  ```bash
  git commit -m "refactor(persistence): Refactor partialUpdate to use generic appliers"
  ```

---

### ✅ Paso 4.2: Tests Persistence - Unit Tests

- [ ] **4.2.1** Crear archivo `UserJpaPersistenceUnitTest.java`

- [ ] **4.2.2** Mock `UserJpaRepository`

- [ ] **4.2.3** Test: findById con entidad existente

- [ ] **4.2.4** Test: findById con entidad no existente

- [ ] **4.2.5** Test: findByEmail

- [ ] **4.2.6** Test: save

- [ ] **4.2.7** Test: update

- [ ] **4.2.8** Test: deleteById exitoso

- [ ] **4.2.9** Test: deleteById fallido

- [ ] **4.2.10** Ejecutar tests y verificar ✅

- [ ] **4.2.11** Commit
  ```bash
  git commit -m "test(persistence): Add unit tests with mocked repository"
  ```

---

### ✅ Paso 4.3: Tests Persistence - partialUpdate

- [ ] **4.3.1** Crear archivo `UserJpaPersistencePartialUpdateTest.java`

- [ ] **4.3.2** Test: Actualizar solo nombre

- [ ] **4.3.3** Test: Actualizar solo email

- [ ] **4.3.4** Test: Actualizar múltiples campos

- [ ] **4.3.5** Test: Actualizar active con Boolean

- [ ] **4.3.6** Test: Actualizar active con String

- [ ] **4.3.7** Test: Usuario no encontrado retorna null

- [ ] **4.3.8** Ejecutar tests y verificar ✅

- [ ] **4.3.9** Commit
  ```bash
  git commit -m "test(persistence): Add partialUpdate comprehensive tests"
  ```

---

### ✅ Paso 4.4: Verificar Cobertura Persistence

- [ ] **4.4.1** Generar reporte JaCoCo

- [ ] **4.4.2** Verificar cobertura Persistence
  - **Meta:** 80%
  - **Actual:** _____%

- [ ] **4.4.3** Commit final de fase
  ```bash
  git commit -m "test(persistence): Complete persistence test coverage (80%)"
  ```

---

## 📝 FASE 5: DTOs y Validation (Días 15-17)

### ✅ Paso 5.1: Null-safety en UsuarioResponse

- [ ] **5.1.1** Abrir `UsuarioResponse.java`

- [ ] **5.1.2** Añadir validación null en `from()`
  ```java
  Objects.requireNonNull(usuario, "User cannot be null");
  ```

- [ ] **5.1.3** Añadir Javadoc al método

- [ ] **5.1.4** Commit
  ```bash
  git commit -m "refactor(dto): Add null-safety to UsuarioResponse.from()"
  ```

---

### ✅ Paso 5.2: Tests DTOs

- [ ] **5.2.1** Crear `CreateUsuarioRequestValidationTest.java`

- [ ] **5.2.2** Tests de validación Jakarta:
  - [ ] Nombre null/blank
  - [ ] Nombre muy corto/largo
  - [ ] Email null/inválido
  - [ ] Contraseña null/corta
  - [ ] Contraseña sin mayúsculas/minúsculas/números

- [ ] **5.2.3** Crear `UpdateUsuarioRequestValidationTest.java`

- [ ] **5.2.4** Tests similares para UpdateUsuarioRequest

- [ ] **5.2.5** Crear `UsuarioResponseTest.java`

- [ ] **5.2.6** Tests:
  - [ ] from() con usuario válido
  - [ ] from() con usuario null lanza excepción
  - [ ] Builder funciona correctamente

- [ ] **5.2.7** Crear `UsuarioResponseJsonTest.java`

- [ ] **5.2.8** Tests de serialización/deserialización JSON

- [ ] **5.2.9** Ejecutar todos los tests y verificar ✅

- [ ] **5.2.10** Commit
  ```bash
  git commit -m "test(dto): Add comprehensive DTO validation and mapping tests"
  ```

---

### ✅ Paso 5.3: Tests Validation Strategy

- [ ] **5.3.1** Crear `ValidationContextTest.java`

- [ ] **5.3.2** Tests:
  - [ ] Selección de estrategia LENIENT
  - [ ] Selección de estrategia STRICT
  - [ ] Estrategia inexistente lanza excepción
  - [ ] validateForCreation delega correctamente
  - [ ] validateForUpdate delega correctamente

- [ ] **5.3.3** Crear `LenientValidationStrategyTest.java`

- [ ] **5.3.4** Tests de reglas lenient

- [ ] **5.3.5** Crear `StrictValidationStrategyTest.java`

- [ ] **5.3.6** Tests de reglas strict

- [ ] **5.3.7** Crear `ValidationIntegrationTest.java`

- [ ] **5.3.8** Tests de integración Strategy ↔ Service

- [ ] **5.3.9** Ejecutar tests y verificar ✅

- [ ] **5.3.10** Commit
  ```bash
  git commit -m "test(validation): Add validation strategy pattern tests"
  ```

---

### ✅ Paso 5.4: Verificar Cobertura DTOs y Validation

- [ ] **5.4.1** Generar reporte JaCoCo

- [ ] **5.4.2** Verificar cobertura
  - **DTOs Meta:** 95% | **Actual:** _____%
  - **Validation Meta:** 85% | **Actual:** _____%

- [ ] **5.4.3** Commit final de fase
  ```bash
  git commit -m "test(dto+validation): Complete DTO and validation test coverage"
  ```

---

## ⚠️ FASE 6: Exception Handling (Días 18-19)

### ✅ Paso 6.1: Refactorizar GlobalExceptionHandler

- [ ] **6.1.1** Abrir `GlobalExceptionHandler.java`

- [ ] **6.1.2** Añadir método `buildErrorResponse()`

- [ ] **6.1.3** Añadir método `extractPath()`

- [ ] **6.1.4** Refactorizar todos los `@ExceptionHandler` para usar métodos extraídos

- [ ] **6.1.5** Commit
  ```bash
  git commit -m "refactor(exception): Extract ErrorResponse builder method (DRY)"
  ```

---

### ✅ Paso 6.2: Tests Exception Handler

- [ ] **6.2.1** Crear `GlobalExceptionHandlerTest.java`

- [ ] **6.2.2** Tests unitarios:
  - [ ] handleUsuarioNotFound retorna 404
  - [ ] handleUsuarioYaExiste retorna 409
  - [ ] handleValidationException retorna 400
  - [ ] handleMethodArgumentNotValidException retorna 400
  - [ ] handleGenericException retorna 500

- [ ] **6.2.3** Crear `GlobalExceptionHandlerIntegrationTest.java`

- [ ] **6.2.4** Tests de integración con controllers

- [ ] **6.2.5** Ejecutar tests y verificar ✅

- [ ] **6.2.6** Commit
  ```bash
  git commit -m "test(exception): Add exception handler tests (unit + integration)"
  ```

---

### ✅ Paso 6.3: Verificar Cobertura Exception Handling

- [ ] **6.3.1** Generar reporte JaCoCo

- [ ] **6.3.2** Verificar cobertura
  - **Meta:** 90%
  - **Actual:** _____%

- [ ] **6.3.3** Commit final de fase
  ```bash
  git commit -m "test(exception): Complete exception handling test coverage (90%)"
  ```

---

## 🔗 FASE 7: Tests E2E (Días 20-23)

### ✅ Paso 7.1: Test End-to-End CRUD

- [ ] **7.1.1** Crear `UsuarioEndToEndTest.java`

- [ ] **7.1.2** Configurar con `@SpringBootTest` y `@AutoConfigureMockMvc`

- [ ] **7.1.3** Test flujo completo:
  - [ ] 1. Crear usuario (POST)
  - [ ] 2. Obtener usuario creado (GET)
  - [ ] 3. Actualizar usuario (PUT)
  - [ ] 4. Actualizar parcialmente (PATCH)
  - [ ] 5. Listar todos (GET)
  - [ ] 6. Eliminar usuario (DELETE)
  - [ ] 7. Verificar eliminación (GET 404)

- [ ] **7.1.4** Ejecutar test y verificar ✅

- [ ] **7.1.5** Commit
  ```bash
  git commit -m "test(e2e): Add end-to-end CRUD flow test"
  ```

---

### ✅ Paso 7.2: Test E2E Validaciones

- [ ] **7.2.1** Crear `UsuarioValidationIntegrationTest.java`

- [ ] **7.2.2** Tests de validación end-to-end:
  - [ ] Crear con email duplicado (409)
  - [ ] Crear con datos inválidos (400)
  - [ ] Actualizar con email duplicado (409)
  - [ ] Obtener usuario inexistente (404)
  - [ ] Eliminar usuario inexistente (404)

- [ ] **7.2.3** Ejecutar tests y verificar ✅

- [ ] **7.2.4** Commit
  ```bash
  git commit -m "test(e2e): Add end-to-end validation flow tests"
  ```

---

### ✅ Paso 7.3: Test E2E Excepciones

- [ ] **7.3.1** Crear `UsuarioExceptionFlowTest.java`

- [ ] **7.3.2** Tests de flujos de error completos:
  - [ ] ErrorResponse tiene formato correcto
  - [ ] Timestamp está presente
  - [ ] Path está presente
  - [ ] Diferentes excepciones tienen códigos HTTP correctos

- [ ] **7.3.3** Ejecutar tests y verificar ✅

- [ ] **7.3.4** Commit
  ```bash
  git commit -m "test(e2e): Add exception flow E2E tests"
  ```

---

## 🎯 FASE 8: Verificación Final (Día 24-25)

### ✅ Paso 8.1: Reporte de Cobertura Final

- [ ] **8.1.1** Limpiar y reconstruir
  ```bash
  ./mvnw clean
  ```

- [ ] **8.1.2** Ejecutar TODOS los tests
  ```bash
  ./mvnw test
  ```

- [ ] **8.1.3** Verificar que todos pasan ✅

- [ ] **8.1.4** Generar reporte JaCoCo final
  ```bash
  ./mvnw jacoco:report
  ```

- [ ] **8.1.5** Abrir reporte
  ```bash
  open target/site/jacoco/index.html
  ```

- [ ] **8.1.6** Documentar cobertura final por capa:
  - Controller: _____% (meta: 90%)
  - Service: _____% (meta: 85%)
  - Persistence: _____% (meta: 80%)
  - Repository: _____% (meta: 90%)
  - DTOs: _____% (meta: 95%)
  - Validation: _____% (meta: 85%)
  - Exception: _____% (meta: 90%)
  - **TOTAL: _____% (meta: 85%)**

---

### ✅ Paso 8.2: Validación de No-Regresión

- [ ] **8.2.1** Ejecutar aplicación localmente
  ```bash
  ./mvnw spring-boot:run
  ```

- [ ] **8.2.2** Verificar endpoints con curl/Postman:
  - [ ] GET `/api/v1/usuarios`
  - [ ] POST `/api/v1/usuarios`
  - [ ] GET `/api/v1/usuarios/{id}`
  - [ ] PUT `/api/v1/usuarios/{id}`
  - [ ] PATCH `/api/v1/usuarios/{id}`
  - [ ] DELETE `/api/v1/usuarios/{id}`

- [ ] **8.2.3** Verificar que funcionalidad NO cambió ✅

---

### ✅ Paso 8.3: Code Review y Documentación

- [ ] **8.3.1** Revisar todos los commits
  ```bash
  git log --oneline refactor/coverage-improvement
  ```

- [ ] **8.3.2** Actualizar CHANGELOG.md (si existe)

- [ ] **8.3.3** Actualizar README.md con instrucciones de testing

- [ ] **8.3.4** Commit final de documentación
  ```bash
  git commit -m "docs: Update testing documentation and changelog"
  ```

---

### ✅ Paso 8.4: Merge a Main

- [ ] **8.4.1** Push de rama de refactoring
  ```bash
  git push origin refactor/coverage-improvement
  ```

- [ ] **8.4.2** Crear Pull Request

- [ ] **8.4.3** Code review por equipo

- [ ] **8.4.4** Aprobar y mergear a main

- [ ] **8.4.5** Celebrar 🎉

---

## 📊 Métricas Finales

### Cobertura por Capa

| Capa | Antes | Después | Mejora |
|------|-------|---------|--------|
| Controller | ~30% | ___% | +___% |
| Service | ~40% | ___% | +___% |
| Persistence | ~50% | ___% | +___% |
| Repository | ~90% | ___% | +___% |
| DTOs | ~0% | ___% | +___% |
| Validation | ~0% | ___% | +___% |
| Exception | ~0% | ___% | +___% |
| **TOTAL** | **~40%** | **___%** | **+___%** |

### Tests Creados

- [ ] **Antes:** ~6 archivos de test
- [ ] **Después:** ~___ archivos de test
- [ ] **Incremento:** ___x

### Líneas de Código

- [ ] **Código producción antes:** ~979 líneas
- [ ] **Código producción después:** ~___ líneas
- [ ] **Código tests antes:** ~___ líneas
- [ ] **Código tests después:** ~___ líneas

---

## 🎓 Lecciones Aprendidas

### ✅ Lo que funcionó bien

1. _________________________________
2. _________________________________
3. _________________________________

### ⚠️ Desafíos enfrentados

1. _________________________________
2. _________________________________
3. _________________________________

### 💡 Mejoras futuras

1. _________________________________
2. _________________________________
3. _________________________________

---

## 🚀 Próximos Pasos Post-Refactoring

- [ ] Aplicar mismo proceso a `pedido-service`
- [ ] Configurar CI/CD con umbral de cobertura mínimo (80%)
- [ ] Añadir mutation testing (PIT)
- [ ] Implementar tests de performance
- [ ] Documentar patrones de testing en wiki del equipo

---

**Checklist completado:** ___ / 200 pasos  
**Progreso:** ___%  
**Estado:** 🟢 En progreso / 🟡 Bloqueado / 🔴 Cancelado  
**Fecha de finalización:** _________

---

**¡Éxito en tu refactorización!** 🚀

