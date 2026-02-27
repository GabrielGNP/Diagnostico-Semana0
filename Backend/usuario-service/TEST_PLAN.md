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

El servicio `usuario-service` gestiona órdenes de compra con persistencia PostgreSQL y comunicación asíncrona vía RabbitMQ con `usuario-service`. Actualmente solo `OrderService` tiene cobertura significativa (93.6%), mientras que componentes críticos como `OrderController`, `GlobalExceptionHandler`, y toda la capa de mensajería están **sin cobertura (0%)**.

### Riesgos Brownfield Identificados

- Código legacy con tests de integración deshabilitados (`@Disabled`)
- Dependencias de RabbitMQ sin mocks adecuados
- GlobalExceptionHandler sin cobertura = errores no validados en producción
- OrderController 0% = endpoints HTTP sin validación de comportamiento

---

## 2. Alcance

### 2.1 En Alcance

| Componente | Tipo | Cobertura Actual | Prioridad |
|------------|------|------------------|-----------|
| `OrderController` | Controller | 0% (76 inst) | 🔴 CRÍTICO |
| `GlobalExceptionHandler` | Exception Handler | 0% (212 inst) | 🔴 CRÍTICO |
| `OrderMapper` | Mapper | 0% (43 inst) | 🟡 ALTO |
| `UserResponseCache` | Messaging | 0% (86 inst) | 🟡 ALTO |
| `UserServiceProducer` | Messaging | 0% (27 inst) | 🟡 ALTO |
| `UserServiceConsumer` | Messaging | 0% (25 inst) | 🟡 ALTO |
| `RabbitMQUserInfoClient` | Messaging | 0% (33 inst) | 🟡 ALTO |
| `OrderEnrichmentFacade` | Service | 6.7% (56 inst missed) | 🟡 ALTO |
| `UserEnrichmentService` | Service | 16% (21 inst missed) | 🟡 ALTO |
| `ErrorResponse` + `Builder` | DTO | 0% (119 inst) | 🟢 MEDIO |
| `OrderStateUpdateDto` | DTO | 0% (16 inst) | 🟢 MEDIO |
| `UserRequest` | DTO | 0% (20 inst) | 🟢 MEDIO |
| `OrderWithUserDto` | DTO | 47.9% (49 inst missed) | 🟢 MEDIO |

### 2.2 Fuera de Alcance

| Componente | Razón |
|------------|-------|
| `Order` (model) | 100% cobertura |
| `State` (enum) | 100% cobertura |
| `OrderDto` | 100% cobertura |
| `OrderNotFoundException` | 100% cobertura |
| `OrderService` | 93.6% cobertura — solo 11 inst missed |
| `RabbitMQConfig` | Configuración declarativa de beans |
| `CorsConfig` | Configuración declarativa |
| `RabbitMQMessageConverterConfig` | Configuración declarativa |
| `PedidoServiceApplication` | Clase main de Spring Boot |

---

## 3. Niveles de Prueba

### 3.1 Pruebas Unitarias

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Validar lógica aislada de mappers, servicios de enriquecimiento, cache y DTOs |
| **Herramientas** | JUnit 5, Mockito, AssertJ |
| **Alcance** | `OrderMapper`, `OrderEnrichmentFacade`, `UserEnrichmentService`, `UserResponseCache`, DTOs |
| **Estrategia de aislamiento** | Mock de `IUserEnrichmentClient`, `IUserInfoClient`, `UserResponseCache` |
| **Contribución estimada** | +15-18% cobertura (~230 instrucciones) |

### 3.2 Pruebas de Integración

| Campo | Descripción |
|-------|-------------|
| **Objetivo** | Validar integración Controller↔Service, manejo de excepciones HTTP, flujos de mensajería |
| **Herramientas** | `@WebMvcTest`, `MockMvc`, `@MockBean`, `@SpringBootTest` con H2 |
| **Alcance** | `OrderController`, `GlobalExceptionHandler`, `UserServiceProducer`, `UserServiceConsumer` |
| **Contribución estimada** | +25-30% cobertura (~400 instrucciones) |

---

## 4. Principios de Testing Aplicados

| Principio | Aplicación |
|-----------|------------|
| **Testing shows presence of defects** | Los tests verifican comportamiento esperado pero no garantizan ausencia de bugs |
| **Exhaustive testing is impossible** | Enfocamos en particiones de equivalencia y valores límite críticos |
| **Early testing** | Priorizamos componentes 0% cobertura que bloquean CI |
| **Defect clustering** | Priorizamos `GlobalExceptionHandler` (212 inst) y `OrderController` (76 inst) |
| **Pesticide paradox** | Variamos escenarios entre particiones válidas e inválidas |
| **Testing is context dependent** | Adaptamos técnicas al contexto brownfield con RabbitMQ |
| **Absence-of-errors fallacy** | Tests de integración validan el sistema completo, no solo unidades |

---

## 5. Aplicación de Técnicas de Diseño

### 5.1 Partición de Equivalencia

| Campo | Partición | Tipo | Válida/Inválida | Escenario Mapeado |
|-------|-----------|------|-----------------|-------------------|
| `name` | String no vacío ("Test Order") | Dato | ✅ Válida | UC-01-01 |
| `name` | String vacío ("") | Dato | ❌ Inválida | UC-01-02 |
| `name` | null | Dato | ❌ Inválida | UC-01-03 |
| `name` | Solo espacios ("   ") | Dato | ❌ Inválida | UC-01-04 |
| `description` | String válido | Dato | ✅ Válida | UC-01-01 |
| `description` | null | Dato | ❌ Inválida | UC-01-05 |
| `idUser` | Entero positivo (1, 100) | Dato | ✅ Válida | UC-01-01 |
| `idUser` | Cero (0) | Dato | ❌ Inválida | UC-01-06 |
| `idUser` | Entero negativo (-1) | Dato | ❌ Inválida | UC-01-07 |
| `idUser` | null | Dato | ❌ Inválida | UC-01-08 |
| `orderId` | ID existente | Dato | ✅ Válida | UC-02-01 |
| `orderId` | ID no existente | Dato | ❌ Inválida | UC-02-02 |
| `state` | Estado válido (PROCESSING, DELIVERED) | Dato | ✅ Válida | UC-03-01 |
| `state` | null | Dato | ❌ Inválida | UC-03-02 |
| `userId` timeout | Respuesta dentro de timeout | Tiempo | ✅ Válida | UC-04-01 |
| `userId` timeout | Respuesta después de timeout | Tiempo | ❌ Inválida | UC-04-02 |

### 5.2 Análisis de Valores Límite

| Campo | Mínimo | Máximo | Valores Límite | Escenario Mapeado |
|-------|--------|--------|----------------|-------------------|
| `idUser` | 1 | Integer.MAX_VALUE | 0, 1, 2, MAX-1, MAX | BVA-01, BVA-02 |
| `orderId` | 1 | - | 0, 1, -1 | BVA-03, BVA-04 |
| `name.length` | 1 | 255 (asumido) | 0, 1, 254, 255, 256 | BVA-05, BVA-06 |
| `timeout` (ms) | 0 | 3000 | 0, 1, 2999, 3000, 3001 | BVA-07, BVA-08 |
| `userId` en cache | - | - | userId presente, userId ausente | BVA-09, BVA-10 |

### 5.3 Tabla de Decisión - Creación de Orden

| Condición / Regla | R1 | R2 | R3 | R4 | R5 | R6 |
|-------------------|----|----|----|----|----|----|
| `name` válido | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| `description` válido | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ |
| `idUser` > 0 | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| **Acción** | 201 Created | 400 Bad Request | 400 Bad Request | 400 Bad Request | 201 Created | 400 Bad Request |
| **Escenario** | DT-01 | DT-02 | DT-03 | DT-04 | DT-01 | DT-05 |

### 5.4 Tabla de Decisión - Manejo de Excepciones

| Condición / Regla | R1 | R2 | R3 | R4 | R5 | R6 |
|-------------------|----|----|----|----|----|----|
| Excepción tipo | OrderNotFound | IllegalArgument | ValidationError | MalformedJSON | OrderCreation | Generic |
| **HTTP Status** | 404 | 400 | 400 | 400 | 500 | 500 |
| **Escenario** | EH-01 | EH-02 | EH-03 | EH-04 | EH-05 | EH-06 |

---

## 6. Escenarios Gherkin

### 6.1 Escenarios de Pruebas Unitarias

#### Feature: OrderMapper - Conversión de entidades a DTOs

```gherkin
Feature: OrderMapper - Entity to DTO Conversion
  As a developer
  I want to ensure OrderMapper correctly converts between Order and OrderDto
  So that data integrity is maintained across layers

  # Cubre: mapper 0% — toDto method
  @critical
  Scenario: UM-01 - Convertir Order entity a OrderDto exitosamente
    Given una entidad Order con id=1, name="Test", description="Desc", idUser=10, state=PROCESSING, active=true
    When se invoca orderMapper.toDto(order)
    Then el OrderDto resultante debe tener los mismos valores
    And el id debe ser 1
    And el state debe ser PROCESSING

  # Cubre: mapper 0% — toDto null handling
  @high
  Scenario: UM-02 - Convertir Order null retorna null
    Given una entidad Order null
    When se invoca orderMapper.toDto(null)
    Then el resultado debe ser null

  # Cubre: mapper 0% — toEntity method
  @critical
  Scenario: UM-03 - Convertir OrderDto a Order entity exitosamente
    Given un OrderDto con name="New Order", description="New Desc", idUser=5
    When se invoca orderMapper.toEntity(orderDto)
    Then la entidad Order resultante debe tener los mismos valores

  # Cubre: mapper 0% — toEntity null handling
  @high
  Scenario: UM-04 - Convertir OrderDto null retorna null
    Given un OrderDto null
    When se invoca orderMapper.toEntity(null)
    Then el resultado debe ser null
```

#### Feature: OrderEnrichmentFacade - Enriquecimiento de pedidos

```gherkin
Feature: OrderEnrichmentFacade - Order Enrichment with User Data
  As a system
  I want to enrich order data with user information
  So that clients receive complete order+user payloads

  Background:
    Given un mock de IUserEnrichmentClient configurado

  # Cubre: OrderEnrichmentFacade 6.7% — enrich happy path
  @critical
  Scenario: UE-01 - Enriquecer orden con datos de usuario exitosamente
    Given un OrderDto válido con idUser=10
    And el userEnrichmentClient retorna UserResponse(id=10, name="John", mail="john@test.com")
    When se invoca orderEnrichmentFacade.enrich(orderDto)
    Then el OrderWithUserDto resultante debe contener los datos del pedido
    And debe contener el UserResponse con id=10

  # Cubre: OrderEnrichmentFacade — enrich with null orderDto
  @high
  Scenario: UE-02 - Enriquecer orden null retorna null
    Given un OrderDto null
    When se invoca orderEnrichmentFacade.enrich(null)
    Then el resultado debe ser null

  # Cubre: OrderEnrichmentFacade — enrich when user service fails
  @high
  Scenario: UE-03 - Enriquecer orden cuando el servicio de usuario falla
    Given un OrderDto válido con idUser=999
    And el userEnrichmentClient lanza una excepción
    When se invoca orderEnrichmentFacade.enrich(orderDto)
    Then el OrderWithUserDto resultante debe contener los datos del pedido
    And el campo user debe ser null
```

#### Feature: UserEnrichmentService - Servicio de enriquecimiento

```gherkin
Feature: UserEnrichmentService - User Info Fetching
  As a service
  I want to fetch user information from external service
  So that orders can be enriched with user data

  # Cubre: UserEnrichmentService 16% — fetchUserInfo success
  @high
  Scenario: US-01 - Obtener información de usuario exitosamente
    Given un mock de IUserInfoClient
    And el cliente retorna UserResponse para userId=5
    When se invoca userEnrichmentService.fetchUserInfo(5)
    Then debe retornar el UserResponse correspondiente

  # Cubre: UserEnrichmentService — fetchUserInfo exception handling
  @high
  Scenario: US-02 - Manejar error al obtener información de usuario
    Given un mock de IUserInfoClient
    And el cliente lanza una excepción
    When se invoca userEnrichmentService.fetchUserInfo(5)
    Then debe retornar null
    And debe logear una advertencia
```

#### Feature: UserResponseCache - Cache de respuestas

```gherkin
Feature: UserResponseCache - In-Memory Response Caching
  As a messaging component
  I want to cache user responses temporarily
  So that async responses can be retrieved by waiting callers

  # Cubre: UserResponseCache 0% — store method
  @critical
  Scenario: UC-01 - Almacenar respuesta de usuario en cache
    Given un UserResponseCache vacío
    And un UserResponse con id=10, name="Test User"
    When se invoca cache.store(userResponse)
    Then la respuesta debe estar disponible para userId=10

  # Cubre: UserResponseCache 0% — store null ignored
  @high
  Scenario: UC-02 - Ignorar almacenamiento de respuesta null
    Given un UserResponseCache vacío
    When se invoca cache.store(null)
    Then el cache debe permanecer vacío

  # Cubre: UserResponseCache 0% — awaitResponse success
  @critical
  Scenario: UC-03 - Esperar y obtener respuesta dentro del timeout
    Given un UserResponseCache con UserResponse para userId=10
    When se invoca cache.awaitResponse(10, 1000)
    Then debe retornar el UserResponse para userId=10
    And la respuesta debe ser removida del cache

  # Cubre: UserResponseCache 0% — awaitResponse timeout
  @critical
  Scenario: UC-04 - Timeout al esperar respuesta no disponible
    Given un UserResponseCache vacío
    When se invoca cache.awaitResponse(999, 100)
    Then debe retornar null después del timeout

  # Cubre: UserResponseCache 0% — awaitResponse interrupted
  @medium
  Scenario: UC-05 - Manejar interrupción durante espera
    Given un UserResponseCache vacío
    And el thread será interrumpido durante la espera
    When se invoca cache.awaitResponse(10, 5000)
    Then debe retornar null
    And el thread debe tener el flag interrupted activo
```

#### Feature: DTOs - Validación de modelos de datos

```gherkin
Feature: DTO Constructors and Accessors
  As a developer
  I want DTOs to correctly store and retrieve data
  So that data transfer between layers works correctly

  # Cubre: OrderStateUpdateDto 0% — constructor and accessors
  @medium
  Scenario: DTO-01 - OrderStateUpdateDto constructor y getters
    Given un state DELIVERED
    When se crea OrderStateUpdateDto(DELIVERED)
    Then getState() debe retornar DELIVERED

  # Cubre: OrderStateUpdateDto 0% — setter
  @medium
  Scenario: DTO-02 - OrderStateUpdateDto setter
    Given un OrderStateUpdateDto con state PROCESSING
    When se invoca setState(TRAVELING_TO_WAREHOUSE)
    Then getState() debe retornar TRAVELING_TO_WAREHOUSE

  # Cubre: UserRequest 0% — all methods
  @medium
  Scenario: DTO-03 - UserRequest constructor y accessors
    Given un userId=25
    When se crea UserRequest(25)
    Then getUserId() debe retornar 25
    And toString() debe contener "userId=25"

  # Cubre: OrderWithUserDto 47.9% — remaining accessors
  @medium
  Scenario: DTO-04 - OrderWithUserDto setters
    Given un OrderWithUserDto vacío
    When se setean todos los campos
    Then los getters deben retornar los valores seteados

  # Cubre: ErrorResponse 0% — builder pattern
  @high
  Scenario: DTO-05 - ErrorResponse builder completo
    Given valores para timestamp, status=400, error="Bad Request", message="Invalid"
    When se construye con ErrorResponse.builder()
    Then el ErrorResponse debe tener todos los campos correctos

  # Cubre: ErrorResponse 0% — validationErrors map
  @high
  Scenario: DTO-06 - ErrorResponse con validationErrors
    Given un mapa de errores de validación
    When se construye ErrorResponse con validationErrors
    Then getValidationErrors() debe retornar el mapa
```

### 6.2 Escenarios de Pruebas de Integración

#### Feature: OrderController - Endpoints REST

```gherkin
Feature: OrderController - REST API Integration
  As an API consumer
  I want to interact with order endpoints
  So that I can manage orders via HTTP

  Background:
    Given el servicio de pedidos está disponible
    And el repositorio está mockeado con @MockBean
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
