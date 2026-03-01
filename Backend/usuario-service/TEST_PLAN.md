# TEST_PLAN.md - Usuario Service

## 1. Overview

| Campo | Valor |
|-------|-------|
| **Versión del documento** | 1.0 |
| **Fecha** | 26 de febrero de 2026 |
| **Microservicio** | usuario-service |
| **Cobertura actual (JaCoCo)** | 49% (1.464/2.907 instrucciones) |
| **Meta de cobertura** | ≥80% |
| **Instrucciones a cubrir** | ~893 adicionales |

### Resumen de Estado Actual

El servicio `usuario-service` gestiona órdenes de compra con persistencia PostgreSQL y comunicación asíncrona vía RabbitMQ con `usuario-service`. Actualmente solo `OrderService` tiene cobertura significativa (93.6%), mientras que componentes críticos como `UserController`, `GlobalExceptionHandler`, y toda la capa de mensajería están **sin cobertura (0%)**.

### Riesgos Brownfield Identificados

- Código legacy con tests de integración deshabilitados (`@Disabled`)
- Dependencias de RabbitMQ sin mocks adecuados
- GlobalExceptionHandler sin cobertura = errores no validados en producción
- OrderController 0% = endpoints HTTP sin validación de comportamiento

---

## 2. Alcance
# TEST_PLAN.md - Usuario Service (corregido)

Resumen de pasos:
- Analizar JaCoCo (`target/site/jacoco/index.html`) y priorizar brechas.
- Reescribir alcance para reflejar clases reales del módulo.
- Implementar tests dirigidos a `persistence` y `mapper`.

Fecha: 2026-03-01

## 1. Overview
- Microservicio: `usuario-service`
- Cobertura actual (JaCoCo): 82% instrucciones, 65% branches (informe local)
- Objetivo inmediato: subir cobertura crítica a ≥80% enfocando paquetes con brechas.

Resumen ejecutivo:
- Paquetes con cobertura baja detectados en JaCoCo:
  - `com.example.usuarioservice.persistence` — 65% (riesgo alto por I/O, init, partialUpdate)
  - `com.example.usuarioservice.mapper` — 67% (null handling y paths no cubiertos)
- Resto de paquetes (`controller`, `service`, `dto`, `validation`, `exception`, `messaging`) presentan cobertura alta y no son prioritarios ahora.

## 2. Alcance

### 2.1 En alcance (prioridad)
- `persistence`:
  - `UserRepository` (JSON persistence) — escenarios: `init()`, `writeToFile()`, `loadUsers()`, `partialUpdate()`.
  - `UserJpaPersistence` — `partialUpdate`, `update`, `deleteById`, parseBoolean edge-cases.
  - `CachedUserPersistenceDecorator` — comportamientos de cache si aplica.
- `mapper`:
  - `UserEntityMapper`, `UsuarioMapper` — `toDomain`, `toEntity`, null / campos parciales, builder mapping.

### 2.2 Fuera de alcance (por ahora)
- `controller`, `service`, `dto`, `validation`, `exception`, `messaging` (alta cobertura actual).

## 3. Niveles de Prueba y Herramientas
- Unitarias: JUnit 5, Mockito, AssertJ — tests para mappers y lógica interna de `persistence`.
- Integración ligera: `@DataJpaTest` o `@SpringBootTest` con H2 para `UserJpaPersistence`.
- Tests filesystem: usar `java.nio.file.Files.createTempDirectory` y cleanup en `@AfterEach`.

## 4. Priorización JaCoCo-driven

| Prioridad | Paquete | Cobertura | Nota |
|---|---:|---:|---|
| 🔴 CRÍTICO | `persistence` | 65% | Mucha lógica I/O y branches no cubiertos; foco primero |
| 🟡 ALTO | `mapper` | 67% | Null/edge cases faltantes |

Estimación: tests dirigidos a `persistence` + `mapper` → ganancia esperada +12–18% instrucciones.

## 5. Técnicas de diseño y escenarios mapeados
- Partición de Equivalencia: archivo JSON válido / ausente / corrupto; updates con keys válidas/invalidas.
- Valores límite: `null` inputs en mappers; `id` nulo/negativo en `loadUsers`.
- Decisión: partialUpdate aplica únicamente keys presentes; parseBoolean maneja Boolean/String/null.

## 6. Escenarios Gherkin prioritarios

Unitarios - Mapper
```gherkin
Feature: UserEntityMapper
  @high
  Scenario: toDomain maps correctly
    Given a UserEntity fully populated
    When toDomain(entity)
    Then result has same field values

  @high
  Scenario: toDomain with null returns null
```

Unitarios/Integración - Persistence (JSON)
```gherkin
Feature: UserRepository JSON persistence
  @critical
  Scenario: init loads existing file into memory

  @critical
  Scenario: init creates file when missing

  @high
  Scenario: writeToFile wraps IOException as RuntimeException

  @high
  Scenario: partialUpdate updates only provided keys
```

## 7. Plan de ejecución (pasos)
1. Implementar tests unitarios para `UserEntityMapper`. (≈1h)
2. Implementar tests unitarios para `UserRepository` usando temp files y mocks de `ObjectMapper`. (≈2–3h)
3. Añadir `@DataJpaTest` para `UserJpaPersistence` (partialUpdate/update/delete). (≈2h)
4. Ejecutar `mvn test jacoco:report` y ajustar según brechas restantes. (iterar)

## 8. Riesgos específicos y mitigaciones
- Filesystem tests: usar temp dir y limpiar; no escribir fuera de `/tmp`.
- JPA tests: usar H2 con `create-drop` y aislamiento por test.
- Mocking de ObjectMapper: preferir `@Spy` o envolver en wrapper para no alterar producción.

## 9. Calendario reducido
- Día 1: mapper tests + init/create file tests.
- Día 2: partialUpdate, writeToFile error path, JPA tests + ejecutar JaCoCo.

## 10. Criterios de éxito
- Reducir missed instructions en `persistence` y `mapper` hasta llevar cobertura global ≥80%.
- No añadir más tests si cobertura supera 90% (regla del request).

Fin del plan corregido — foco en `persistence` y `mapper` del `usuario-service`.
    And el OrderService está configurado

  # Cubre: controller 0% — POST /orders endpoint
  @critical
  Scenario: IC-01 - Crear pedido con datos válidos retorna 201
    Given un OrderDto válido con name="Test Order", description="Desc", idUser=1
    When se envía POST a "/orders" con el OrderDto
    Then el status de respuesta debe ser 201 Created
    And el header Location debe contener "/orders/{id}"
    And el body debe contener el pedido creado con state=PROCESSING

  # Cubre: controller 0% — POST /orders validation
  @critical
  Scenario: IC-02 - Crear pedido con name vacío retorna 400
    Given un OrderDto con name="" (vacío)
    When se envía POST a "/orders" con el OrderDto
    Then el status de respuesta debe ser 400 Bad Request
    And el body debe contener errores de validación

  # Cubre: controller 0% — POST /orders null idUser
  @high
  Scenario: IC-03 - Crear pedido sin idUser retorna 400
    Given un OrderDto con idUser=null
    When se envía POST a "/orders" con el OrderDto
    Then el status de respuesta debe ser 400 Bad Request

  # Cubre: controller 0% — GET /orders/{id} found
  @critical
  Scenario: IC-04 - Obtener pedido por ID existente retorna 200
    Given existe un pedido con id=1 en el repositorio
    When se envía GET a "/orders/1"
    Then el status de respuesta debe ser 200 OK
    And el body debe contener el pedido con id=1

  # Cubre: controller 0% — GET /orders/{id} not found
  @critical
  Scenario: IC-05 - Obtener pedido por ID inexistente retorna 404
    Given no existe pedido con id=999
    When se envía GET a "/orders/999"
    Then el status de respuesta debe ser 404 Not Found
    And el body debe contener mensaje de error

  # Cubre: controller 0% — GET /orders list active
  @high
  Scenario: IC-06 - Listar pedidos activos retorna lista
    Given existen 3 pedidos activos en el repositorio
    When se envía GET a "/orders"
    Then el status de respuesta debe ser 200 OK
    And el body debe contener 3 pedidos

  # Cubre: controller 0% — GET /orders empty
  @high
  Scenario: IC-07 - Listar pedidos sin datos retorna lista vacía
    Given no existen pedidos activos
    When se envía GET a "/orders"
    Then el status de respuesta debe ser 200 OK
    And el body debe ser una lista vacía

  # Cubre: controller 0% — GET /orders/user/{userId}
  @high
  Scenario: IC-08 - Listar pedidos por usuario retorna filtrado
    Given existen pedidos para userId=5
    When se envía GET a "/orders/user/5"
    Then el status de respuesta debe ser 200 OK
    And todos los pedidos deben tener idUser=5

  # Cubre: controller 0% — GET /orders/all
  @medium
  Scenario: IC-09 - Listar todos los pedidos (admin) incluye inactivos
    Given existen pedidos activos e inactivos
    When se envía GET a "/orders/all"
    Then el status de respuesta debe ser 200 OK
    And el body debe incluir pedidos con active=false

  # Cubre: controller 0% — DELETE /orders/{id} success
  @critical
  Scenario: IC-10 - Eliminar pedido (soft-delete) retorna 204
    Given existe un pedido con id=1
    When se envía DELETE a "/orders/1"
    Then el status de respuesta debe ser 204 No Content
    And el pedido debe tener active=false

  # Cubre: controller 0% — DELETE /orders/{id} not found
  @high
  Scenario: IC-11 - Eliminar pedido inexistente retorna 404
    Given no existe pedido con id=999
    When se envía DELETE a "/orders/999"
    Then el status de respuesta debe ser 404 Not Found

  # Cubre: controller 0% — PATCH /orders/{id} success
  @high
  Scenario: IC-12 - Cambiar estado de pedido retorna 200
    Given existe un pedido con id=1 y state=PROCESSING
    And un OrderStateUpdateDto con state=DELIVERED
    When se envía PATCH a "/orders/1" con el DTO
    Then el status de respuesta debe ser 200 OK
    And el pedido debe tener state=DELIVERED

  # Cubre: controller 0% — GET /orders/{id}/user enriched
  @high
  Scenario: IC-13 - Obtener pedido con información de usuario
    Given existe un pedido con id=1 y idUser=10
    And el servicio de usuario retorna datos para userId=10
    When se envía GET a "/orders/1/user"
    Then el status de respuesta debe ser 200 OK
    And el body debe ser OrderWithUserDto con datos de usuario
```

#### Feature: GlobalExceptionHandler - Manejo de errores HTTP

```gherkin
Feature: GlobalExceptionHandler - HTTP Error Handling
  As an API consumer
  I want consistent error responses
  So that I can handle errors predictably

  Background:
    Given el GlobalExceptionHandler está configurado
    And MockMvc está disponible

  # Cubre: GlobalExceptionHandler 0% — OrderNotFoundException
  @critical
  Scenario: EH-01 - OrderNotFoundException retorna 404 con ErrorResponse
    Given el servicio lanza OrderNotFoundException("Pedido con ID 999 no encontrado")
    When se procesa la excepción
    Then el status debe ser 404
    And el error debe ser "Not Found"
    And el message debe contener "999"

  # Cubre: GlobalExceptionHandler 0% — IllegalArgumentException
  @high
  Scenario: EH-02 - IllegalArgumentException retorna 400 con ErrorResponse
    Given el servicio lanza IllegalArgumentException("Parámetro inválido")
    When se procesa la excepción
    Then el status debe ser 400
    And el error debe ser "Bad Request"

  # Cubre: GlobalExceptionHandler 0% — MethodArgumentNotValidException
  @critical
  Scenario: EH-03 - Validación fallida retorna 400 con errores específicos
    Given una request con campos inválidos (name="", idUser=null)
    When se envía POST a crear orden
    Then el status debe ser 400
    And el error debe ser "Validation Failed"
    And validationErrors debe contener los campos inválidos

  # Cubre: GlobalExceptionHandler 0% — HttpMessageNotReadableException
  @high
  Scenario: EH-04 - JSON malformado retorna 400
    Given un body con JSON inválido
    When se envía POST a "/orders"
    Then el status debe ser 400
    And el message debe ser "Request body is missing or malformed"

  # Cubre: GlobalExceptionHandler 0% — OrderCreationException
  @high
  Scenario: EH-05 - Error de creación retorna 500
    Given el servicio lanza OrderCreationException("Failed to persist order")
    When se procesa la excepción
    Then el status debe ser 500
    And el error debe ser "Internal Server Error"

  # Cubre: GlobalExceptionHandler 0% — Generic Exception
  @medium
  Scenario: EH-06 - Excepción genérica retorna 500
    Given el servicio lanza RuntimeException("Unexpected error")
    When se procesa la excepción
    Then el status debe ser 500
    And el message debe ser "An unexpected error occurred"
```

#### Feature: Messaging - RabbitMQ Integration

```gherkin
Feature: RabbitMQ Messaging Components
  As a messaging system
  I want to send and receive user info requests/responses
  So that orders can be enriched asynchronously

  Background:
    Given RabbitMQ está mockeado con @MockBean RabbitTemplate
    And los componentes de mensajería están configurados

  # Cubre: UserServiceProducer 0% — requestUserInfo
  @critical
  Scenario: MSG-01 - Enviar solicitud de información de usuario
    Given un UserServiceProducer con RabbitTemplate mockeado
    When se invoca producer.requestUserInfo(userId=10)
    Then rabbitTemplate.convertAndSend debe ser invocado
    And el exchange debe ser "user-exchange"
    And el routing key debe ser "user.request"
    And el payload debe ser UserRequest con userId=10

  # Cubre: UserServiceConsumer 0% — receiveUserResponse
  @critical
  Scenario: MSG-02 - Recibir respuesta de usuario via listener
    Given un UserServiceConsumer con cache mockeado
    And un UserResponse(id=10, name="John")
    When se invoca consumer.receiveUserResponse(userResponse)
    Then cache.store debe ser invocado con el UserResponse

  # Cubre: UserServiceConsumer 0% — getUserResponse delegation
  @high
  Scenario: MSG-03 - Obtener respuesta delegando a cache
    Given un UserServiceConsumer con cache mockeado
    And cache.awaitResponse retorna UserResponse
    When se invoca consumer.getUserResponse(10, 3000)
    Then debe retornar el UserResponse del cache

  # Cubre: RabbitMQUserInfoClient 0% — fetchUserInfo orchestration
  @critical
  Scenario: MSG-04 - Orquestar solicitud y espera de respuesta
    Given un RabbitMQUserInfoClient con producer y consumer mockeados
    And consumer.getUserResponse retorna UserResponse
    When se invoca client.fetchUserInfo(10, 3000)
    Then producer.requestUserInfo(10) debe ser invocado
    And consumer.getUserResponse(10, 3000) debe ser invocado
    And debe retornar el UserResponse

  # Cubre: RabbitMQUserInfoClient 0% — fetchUserInfo error handling
  @high
  Scenario: MSG-05 - Manejar error en orquestación
    Given un RabbitMQUserInfoClient con producer que lanza excepción
    When se invoca client.fetchUserInfo(10, 3000)
    Then debe retornar null
    And debe logear advertencia
```

#### Feature: Integration Flow - Controller to Service to Repository

```gherkin
Feature: Full Integration Flow Tests
  As a system
  I want end-to-end integration between components
  So that the full request lifecycle works correctly

  Background:
    Given @SpringBootTest con H2 in-memory database
    And RabbitMQ deshabilitado via spring.autoconfigure.exclude
    And el contexto de Spring está inicializado

  # Cubre: Flujo completo Controller → Service → Repository
  @critical
  Scenario: FLOW-01 - Crear y recuperar pedido end-to-end
    Given la base de datos está vacía
    When se crea un pedido con name="E2E Test", idUser=1
    And se obtiene el pedido por su ID generado
    Then el pedido recuperado debe tener name="E2E Test"
    And state=PROCESSING
    And active=true

  # Cubre: Flujo de soft-delete
  @high
  Scenario: FLOW-02 - Soft-delete no aparece en listado activo
    Given existe un pedido activo con id=1
    When se elimina el pedido con id=1 (soft-delete)
    And se listan los pedidos activos
    Then el pedido con id=1 no debe aparecer en la lista

  # Cubre: Flujo de cambio de estado
  @high
  Scenario: FLOW-03 - Transición de estados completa
    Given existe un pedido con state=PROCESSING
    When se cambia el estado a TRAVELING_TO_WAREHOUSE
    And se cambia el estado a DELIVERED
    Then el pedido debe tener state=DELIVERED
```

---

## 7. Priorización por Cobertura (JaCoCo-driven)

### 7.1 Análisis de Brechas

| Prioridad | Paquete/Clase | Cobertura Actual | Inst. Missed | Tipo Prueba | Escenarios |
|-----------|---------------|------------------|--------------|-------------|------------|
| 🔴 CRÍTICO | `GlobalExceptionHandler` | 0% | 212 | Integración | 6 |
| 🔴 CRÍTICO | `OrderController` | 0% | 76 | Integración | 13 |
| 🟡 ALTO | `UserResponseCache` | 0% | 86 | Unitaria | 5 |
| 🟡 ALTO | `ErrorResponse` + `Builder` | 0% | 119 | Unitaria | 2 |
| 🟡 ALTO | `OrderEnrichmentFacade` | 6.7% | 56 | Unitaria | 3 |
| 🟡 ALTO | `OrderMapper` | 0% | 43 | Unitaria | 4 |
| 🟡 ALTO | `RabbitMQUserInfoClient` | 0% | 33 | Unitaria | 2 |
| 🟡 ALTO | `UserServiceProducer` | 0% | 27 | Integración | 1 |
| 🟡 ALTO | `UserServiceConsumer` | 0% | 25 | Integración | 2 |
| 🟢 MEDIO | `OrderWithUserDto` | 47.9% | 49 | Unitaria | 1 |
| 🟢 MEDIO | `UserEnrichmentService` | 16% | 21 | Unitaria | 2 |
| 🟢 MEDIO | `UserRequest` | 0% | 20 | Unitaria | 1 |
| 🟢 MEDIO | `OrderStateUpdateDto` | 0% | 16 | Unitaria | 2 |
| 🟢 MEDIO | `UserResponse` | 37.5% | 35 | Unitaria | 1 |

**Total instrucciones faltantes priorizadas:** ~818

### 7.2 Ganancia Estimada de Cobertura

| Grupo de Tests | Instrucciones Cubiertas | Ganancia Estimada |
|----------------|-------------------------|-------------------|
| Controller tests (`@WebMvcTest`) | ~76 | +5% |
| Exception Handler tests | ~212 | +14% |
| Mapper tests (Unitarios) | ~43 | +3% |
| Cache + Messaging tests | ~171 | +11% |
| Enrichment facade/service tests | ~77 | +5% |
| DTO tests | ~139 | +9% |
| Integration flow tests | ~50 (incrementales) | +3% |

**Total ganancia estimada:** ~50% adicional → Cobertura final proyectada: **~75%**

---

## 8. Gestión de Riesgos

### 8.1 Registro de Riesgos

| ID | Riesgo | Probabilidad | Impacto | Severidad | Mitigación |
|----|--------|--------------|---------|-----------|------------|
| R01 | Tests pasan localmente pero fallan en CI por RabbitMQ | Alta | Alto | 🔴 Alto | `spring.autoconfigure.exclude=RabbitAutoConfiguration` |
| R02 | Tests de integración lentos por contexto Spring | Media | Medio | 🟡 Medio | Usar `@WebMvcTest` en vez de `@SpringBootTest` donde posible |
| R03 | Contaminación de estado de BD entre tests | Media | Alto | 🔴 Alto | `@Transactional` + `@Rollback` + H2 in-memory |
| R04 | Meta de cobertura no alcanzada | Media | Alto | 🔴 Alto | Priorizar escenarios CRÍTICOS primero |
| R05 | Mocks incorrectos no detectan bugs reales | Media | Alto | 🔴 Alto | Combinar tests unitarios con tests de integración |
| R06 | `UserResponseCache` concurrencia difícil de testear | Alta | Medio | 🟡 Medio | Tests con threads controlados, timeouts cortos |
| R07 | Cambios en DTOs rompen serialización | Media | Alto | 🔴 Alto | Tests de serialización JSON explícitos |

### 8.2 Estrategia de Respuesta a Riesgos

- **R01:** Configurar `application-test.yml` excluyendo RabbitMQ auto-configuration
- **R02:** Separar tests `@WebMvcTest` (controller) de `@SpringBootTest` (full integration)
- **R03:** Usar perfil `test` con H2, cada test en transacción con rollback
- **R04:** Ejecutar primero: `GlobalExceptionHandler` + `OrderController` = +19% cobertura
- **R05:** Mantener proporción 60% unitarias / 40% integración
- **R06:** Usar `CountDownLatch` y timeouts < 500ms en tests de cache
- **R07:** Agregar tests de serialización/deserialización con ObjectMapper

### 8.3 Umbrales de Riesgo por Cobertura

| Umbral | Acción |
|--------|--------|
| < 50% | 🔴 Pipeline bloqueado — release no permitido |
| 50%–69% | 🟡 Advertencia — requiere aprobación manual |
| ≥80% | ✅ Aceptable — CI/CD continúa |

---

## 9. Calendario de Pruebas

| Fase | Actividad | Esfuerzo Est. | Prioridad |
|------|-----------|---------------|-----------|
| **Fase 1** | Tests unitarios: `OrderMapper`, `UserResponseCache` | 2-3h | 🔴 Crítica |
| **Fase 2** | Tests integración: `OrderController` (`@WebMvcTest`) | 3-4h | 🔴 Crítica |
| **Fase 3** | Tests integración: `GlobalExceptionHandler` | 2-3h | 🔴 Crítica |
| **Fase 4** | Tests unitarios: `OrderEnrichmentFacade`, `UserEnrichmentService` | 2h | 🟡 Alta |
| **Fase 5** | Tests unitarios: Messaging components | 2h | 🟡 Alta |
| **Fase 6** | Tests unitarios: DTOs (`ErrorResponse`, etc.) | 1-2h | 🟢 Media |
| **Fase 7** | Tests integración: Full flow (`@SpringBootTest`) | 2h | 🟢 Media |
| **Fase 8** | Ejecución completa + análisis JaCoCo | 30min | — |

**Esfuerzo total estimado:** 15-19 horas

---

## 10. Herramientas y Entorno

| Herramienta | Propósito | Configuración |
|-------------|-----------|---------------|
| JUnit 5 | Framework de pruebas | Via Spring Boot Starter Test |
| Mockito | Framework de mocking | Via Spring Boot Starter Test |
| MockMvc | Testing capa HTTP | `@WebMvcTest(OrderController.class)` |
| `@MockBean` | Mocking de beans Spring | Para Repository, RabbitTemplate |
| H2 Database | BD in-memory para tests | `spring.datasource.url=jdbc:h2:mem:testdb` |
| JaCoCo | Reporte de cobertura | Plugin Maven configurado |
| AssertJ | Assertions fluidas | Opcional, mejora legibilidad |

### Configuración de Test Profile

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

pedido:
  migration:
    enabled: false

user:
  service:
    timeout: 100
```

---

## 11. Trazabilidad de Escenarios

| ID Escenario | Criterio/Brecha | Técnica Aplicada | Tipo Test |
|--------------|-----------------|------------------|-----------|
| IC-01 | controller 0%, POST 201 | Partición Equivalencia | Integración |
| IC-02 | controller 0%, Validación | Partición Inválida | Integración |
| IC-04, IC-05 | controller 0%, GET by ID | Partición Válida/Inválida | Integración |
| EH-01 to EH-06 | GlobalExceptionHandler 0% | Tabla Decisión | Integración |
| UM-01 to UM-04 | mapper 0% | Partición + Límite | Unitaria |
| UC-01 to UC-05 | UserResponseCache 0% | Partición + Límite | Unitaria |
| MSG-01 to MSG-05 | messaging 0% | Partición | Integración |
| UE-01 to UE-03 | OrderEnrichmentFacade 6.7% | Partición | Unitaria |
| DTO-01 to DTO-06 | DTOs 0-47% | Partición | Unitaria |

---

## 12. Criterios de Éxito

| Métrica | Valor Objetivo |
|---------|----------------|
| Cobertura de instrucciones | ≥80% |
| Cobertura de branches | ≥60% |
| Tests pasando | 100% |
| Tiempo de ejecución total | < 60 segundos |
| Escenarios CRÍTICOS implementados | 100% |

---

**Fin del documento TEST_PLAN.md**
