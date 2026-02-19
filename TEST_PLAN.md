# TEST_PLAN.md

## 1. Overview

### User Story Summary

Este plan de pruebas cubre dos módulos principales del sistema:

| Story ID | Título | Endpoint | Operación |
|----------|--------|----------|-----------|
| HU-ORD-01 | Listado completo de pedidos | `GET /order/all` | Listar todos los pedidos |
| HU-ORD-02 | Consulta de pedido por ID | `GET /order/{id}` | Obtener pedido específico |
| HU-ORD-03 | Listado de pedidos por usuario | `GET /order/user/{idUser}` | Filtrar pedidos por usuario |
| HU-ORD-04 | Pedido con información de usuario | `GET /order/{id}/with-user-info` | Pedido enriquecido vía RabbitMQ |
| HU-ORD-05 | Creación de pedidos | `POST /order/add` | Crear nuevo pedido |
| HU-ORD-06 | Eliminación de pedidos | `DELETE /order/{id}` | Eliminar pedido |
| HU-ORD-07 | Cambio de estado de pedido | `PATCH /order/{id}` | Actualizar estado |
| HU-USR-01 | Listado de usuarios | `GET /v1/usuarios` | Listar usuarios activos |
| HU-USR-02 | Consulta de usuario por ID | `GET /v1/usuarios/{id}` | Obtener usuario específico |
| HU-USR-03 | Creación de usuarios | `POST /v1/usuarios` | Crear nuevo usuario |
| HU-USR-04 | Actualización completa | `PUT /v1/usuarios/{id}` | Reemplazar usuario |
| HU-USR-05 | Actualización parcial | `PATCH /v1/usuarios/{id}` | Modificar campos específicos |
| HU-USR-06 | Soft-delete de usuario | `DELETE /v1/usuarios/{id}` | Marcar como eliminado |

### Brownfield Risk Analysis

| Riesgo | Descripción | Impacto | Mitigación |
|--------|-------------|---------|------------|
| **Integración RabbitMQ** | Comunicación asíncrona entre pedido-service y usuario-service con timeout de 3000ms | Alto | Tests de integración con mocks de RabbitMQ; pruebas de timeout |
| **Migración JSON → PostgreSQL** | Sistema actualmente usa archivos JSON; migración a PostgreSQL puede causar inconsistencias | Alto | Tests de integridad de datos; validación de esquemas |
| **Dependencias entre servicios** | pedido-service depende de usuario-service para enriquecer datos | Medio | Tests de resiliencia; manejo de fallos de servicio |
| **Soft-delete vs Hard-delete** | Inconsistencia en políticas de eliminación entre módulos | Medio | Tests de estado de registros post-eliminación |
| **Validación de DTOs** | Cambios en DTOs de mensajería deben sincronizarse entre servicios | Alto | Tests de contrato; validación de esquemas JSON |
| **Estados de pedidos** | Transiciones de estado no documentadas explícitamente | Medio | Tests de máquina de estados |

---

## 2. Applied Testing Principles

### Principio Identificado: **Clustering de Defectos (Defect Clustering)**

### Justificación

En sistemas brownfield con múltiples integraciones (RabbitMQ, PostgreSQL, servicios REST), los defectos tienden a concentrarse en:

1. **Puntos de integración**: La comunicación asíncrona vía RabbitMQ entre servicios representa un área de alta concentración de defectos potenciales.
2. **Validación de entrada**: Los endpoints que reciben datos externos (POST, PUT, PATCH) son propensos a errores de validación.
3. **Manejo de errores**: Timeouts, conexiones fallidas y recursos no encontrados requieren cobertura exhaustiva.
4. **Cambios de estado**: Las transiciones de estado en pedidos son áreas críticas.

**Principios secundarios aplicados:**
- **Testing exhaustivo es imposible**: Priorizamos escenarios basados en riesgo.
- **Testing temprano**: Definimos tests antes de implementar (TDD).
- **Ausencia de errores es falacia**: Validamos tanto escenarios positivos como negativos.

---

## 3. Test Levels Strategy

### Unit Testing

| Componente | Objetivo | Cobertura |
|------------|----------|-----------|
| `OrderService` | Lógica de negocio de pedidos | Validaciones, transformaciones, cálculo de estados |
| `UserService` | Lógica de negocio de usuarios | Validaciones, soft-delete, unicidad |
| `OrderMapper` | Conversión DTO ↔ Entity | Mapeo correcto de todos los campos |
| `OrderRepository` | Operaciones CRUD | Queries, filtros, paginación |
| `UserRepository` | Operaciones CRUD | Queries, soft-delete, filtros |
| `RabbitMQ Producers/Consumers` | Serialización/deserialización | Formato de mensajes, timeouts |

**Estrategia de mocking:**
- Mock de repositorios para aislar lógica de servicio
- Mock de RabbitMQ para pruebas de mensajería
- Mock de conexión PostgreSQL para pruebas de repositorio

### Integration Testing

| Integración | Objetivo | Herramientas |
|-------------|----------|--------------|
| Controller → Service → Repository | Flujo completo de request | Spring Boot Test, TestRestTemplate |
| Service → RabbitMQ → Service | Comunicación asíncrona | Testcontainers (RabbitMQ) |
| Repository → PostgreSQL | Persistencia real | Testcontainers (PostgreSQL) |
| Frontend → Backend | API contracts | Vitest + MSW |

### System Testing

| Escenario | Objetivo | Ambiente |
|-----------|----------|----------|
| E2E Order Flow | Crear → Consultar → Actualizar → Eliminar pedido | Docker Compose |
| E2E User Flow | Crear → Consultar → Actualizar → Soft-delete usuario | Docker Compose |
| Cross-service Flow | Pedido con info de usuario vía RabbitMQ | Docker Compose completo |
| Performance | Validar tiempos de respuesta < 200ms | k6 / JMeter |

---

## 4. Test Design Application

### 4.1 Equivalence Partitioning

#### Pedidos (Orders)

| Campo | Particiones Válidas | Particiones Inválidas |
|-------|--------------------|-----------------------|
| `id` | Enteros positivos existentes (1, 5, 100) | Negativos (-1), cero (0), no numéricos ("abc"), null |
| `idUser` | Enteros positivos (1, 10, 50) | Negativos, cero, no numéricos, null |
| `name` | Strings no vacíos (1-255 chars) | Vacío (""), null, excede límite |
| `description` | Strings (0-1000 chars) | Excede límite |
| `state` | PENDING, PROCESSING, COMPLETED, CANCELLED | Estados inválidos ("INVALID"), null, vacío |
| `active` | true, false | null, valores no booleanos |

#### Usuarios (Users)

| Campo | Particiones Válidas | Particiones Inválidas |
|-------|--------------------|-----------------------|
| `id` | Enteros positivos existentes | Negativos, cero, no numéricos, null |
| `email` | Formato válido (user@domain.com) | Sin @, sin dominio, vacío, null |
| `name` | Strings no vacíos (1-100 chars) | Vacío, null, excede límite |
| `active` | true (no soft-deleted) | false (soft-deleted) |

### 4.2 Boundary Value Analysis

#### Pedidos - ID

| Límite | Valor | Tipo | Escenario |
|--------|-------|------|-----------|
| Mínimo válido | 1 | On-point | ID mínimo aceptado |
| Mínimo inválido | 0 | Off-point | Debe rechazarse |
| Negativo | -1 | Off-point | Debe rechazarse |
| Máximo INT | 2147483647 | On-point | ID máximo aceptado |
| Overflow | 2147483648 | Off-point | Debe rechazarse |

#### Pedidos - name (asumiendo max 255 chars)

| Límite | Valor | Tipo | Escenario |
|--------|-------|------|-----------|
| Mínimo válido | 1 char | On-point | Nombre mínimo |
| Vacío | 0 chars | Off-point | Debe rechazarse |
| Máximo válido | 255 chars | On-point | Nombre máximo |
| Excede máximo | 256 chars | Off-point | Debe rechazarse |

#### RabbitMQ Timeout

| Límite | Valor | Tipo | Escenario |
|--------|-------|------|-----------|
| Dentro del timeout | 2999ms | On-point | Respuesta exitosa |
| En el límite | 3000ms | On-point | Respuesta exitosa (borde) |
| Excede timeout | 3001ms | Off-point | Timeout, error 500 |

### 4.3 Decision Table

#### Tabla de Decisión: Creación de Pedido (POST /order/add)

| Condición | R1 | R2 | R3 | R4 | R5 | R6 |
|-----------|----|----|----|----|----|----|
| name presente | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| name válido | ✓ | - | ✗ | ✓ | ✓ | ✓ |
| idUser presente | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ |
| idUser numérico | ✓ | ✓ | ✓ | - | ✗ | ✓ |
| description presente | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| **Resultado** | 200 OK | 400 | 400 | 400 | 400 | 200* |

*description es opcional

#### Tabla de Decisión: Cambio de Estado (PATCH /order/{id})

| Condición | R1 | R2 | R3 | R4 | R5 |
|-----------|----|----|----|----|-----|
| Pedido existe | ✓ | ✗ | ✓ | ✓ | ✓ |
| Campo state enviado | ✓ | ✓ | ✗ | ✓ | ✓ |
| State válido (enum) | ✓ | - | - | ✗ | ✓ |
| ID numérico | ✓ | ✓ | ✓ | ✓ | ✗ |
| **Resultado** | 200 OK | 404 | 400 | 400 | 400 |

#### Tabla de Decisión: Consulta con Info de Usuario (GET /order/{id}/with-user-info)

| Condición | R1 | R2 | R3 | R4 |
|-----------|----|----|----|----|
| Pedido existe | ✓ | ✗ | ✓ | ✓ |
| Usuario responde vía RabbitMQ | ✓ | - | ✗ (timeout) | ✓ |
| Tiempo respuesta < 3000ms | ✓ | - | ✗ | ✓ |
| **Resultado** | 200 OK | 404 | 500 | 200 OK |

---

## 5. Gherkin Scenarios

```gherkin
Feature: Gestión de Pedidos con Persistencia PostgreSQL
  Como Product Owner
  Quiero gestionar pedidos en el sistema
  Para garantizar la integridad y trazabilidad de datos en PostgreSQL

  Background:
    Given la conexión a PostgreSQL está disponible
    And el servicio pedido-service está activo

  # ============================================
  # HU-ORD-01: Listado completo de pedidos
  # ============================================

  # --- Equivalence Partitioning: Partición válida (pedidos existentes) ---
  @HU-ORD-01 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-01-01 - Listado exitoso con pedidos existentes
    Given existen 5 pedidos registrados en PostgreSQL
    When invoco el endpoint "GET /order/all"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista con 5 pedidos
    And cada pedido incluye los campos: id, name, description, idUser, state, active

  # --- Equivalence Partitioning: Partición válida (sin pedidos) ---
  @HU-ORD-01 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-01-02 - Listado vacío cuando no existen pedidos
    Given no existen pedidos en PostgreSQL
    When invoco el endpoint "GET /order/all"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista vacía "[]"

  # --- Equivalence Partitioning: Partición inválida (error de conexión) ---
  @HU-ORD-01 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-01-03 - Error cuando PostgreSQL no está disponible
    Given la conexión a PostgreSQL no está disponible
    When invoco el endpoint "GET /order/all"
    Then recibo HTTP 500 Internal Server Error
    And la respuesta contiene un mensaje descriptivo del error

  # ============================================
  # HU-ORD-02: Consulta de pedido por ID
  # ============================================

  # --- Equivalence Partitioning: ID válido existente ---
  @HU-ORD-02 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-02-01 - Consulta exitosa de pedido existente
    Given existe un pedido con ID = 5 en PostgreSQL
    And el pedido tiene name = "Pedido Test" y state = "PENDING"
    When invoco el endpoint "GET /order/5"
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido con ID = 5
    And el campo name es "Pedido Test"
    And el campo state es "PENDING"

  # --- Equivalence Partitioning: ID válido no existente ---
  @HU-ORD-02 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-02-02 - Pedido no encontrado
    Given no existe un pedido con ID = 999 en PostgreSQL
    When invoco el endpoint "GET /order/999"
    Then recibo HTTP 404 Not Found

  # --- Equivalence Partitioning: ID inválido (no numérico) ---
  @HU-ORD-02 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-02-03 - ID con formato inválido no numérico
    When invoco el endpoint "GET /order/abc"
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: ID mínimo válido ---
  @HU-ORD-02 @BoundaryValue @Positive
  Scenario: BVA-ORD-02-01 - Consulta con ID mínimo válido (1)
    Given existe un pedido con ID = 1 en PostgreSQL
    When invoco el endpoint "GET /order/1"
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido con ID = 1

  # --- Boundary Value Analysis: ID cero (inválido) ---
  @HU-ORD-02 @BoundaryValue @Negative
  Scenario: BVA-ORD-02-02 - Rechazo de ID cero
    When invoco el endpoint "GET /order/0"
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: ID negativo ---
  @HU-ORD-02 @BoundaryValue @Negative
  Scenario: BVA-ORD-02-03 - Rechazo de ID negativo
    When invoco el endpoint "GET /order/-1"
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: ID máximo INT ---
  @HU-ORD-02 @BoundaryValue @Positive
  Scenario: BVA-ORD-02-04 - Consulta con ID máximo INT
    Given existe un pedido con ID = 2147483647 en PostgreSQL
    When invoco el endpoint "GET /order/2147483647"
    Then recibo HTTP 200 OK

  # ============================================
  # HU-ORD-03: Listado de pedidos por usuario
  # ============================================

  # --- Equivalence Partitioning: Usuario con pedidos ---
  @HU-ORD-03 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-03-01 - Usuario con múltiples pedidos
    Given el usuario con ID = 10 tiene 3 pedidos en PostgreSQL
    When invoco el endpoint "GET /order/user/10"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista con 3 pedidos
    And todos los pedidos tienen idUser = 10

  # --- Equivalence Partitioning: Usuario sin pedidos ---
  @HU-ORD-03 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-03-02 - Usuario sin pedidos
    Given el usuario con ID = 20 no tiene pedidos en PostgreSQL
    When invoco el endpoint "GET /order/user/20"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista vacía "[]"

  # --- Equivalence Partitioning: ID de usuario inválido ---
  @HU-ORD-03 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-03-03 - ID de usuario no numérico
    When invoco el endpoint "GET /order/user/xyz"
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: Usuario con 1 pedido (mínimo) ---
  @HU-ORD-03 @BoundaryValue @Positive
  Scenario: BVA-ORD-03-01 - Usuario con exactamente 1 pedido
    Given el usuario con ID = 15 tiene 1 pedido en PostgreSQL
    When invoco el endpoint "GET /order/user/15"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista con 1 pedido

  # ============================================
  # HU-ORD-04: Pedido con información de usuario (RabbitMQ)
  # ============================================

  # --- Decision Table R1: Pedido existe + Usuario responde ---
  @HU-ORD-04 @DecisionTable @Positive
  Scenario: DT-ORD-04-01 - Pedido con información de usuario exitosa
    Given existe un pedido con ID = 5 en PostgreSQL
    And el pedido tiene idUser = 10
    And el servicio usuario-service responde en menos de 3000ms
    And el usuario con ID = 10 tiene name = "Juan Pérez" y email = "juan@test.com"
    When invoco el endpoint "GET /order/5/with-user-info"
    Then recibo HTTP 200 OK
    And la respuesta contiene los datos del pedido
    And la respuesta contiene userName = "Juan Pérez"
    And la respuesta contiene userEmail = "juan@test.com"

  # --- Decision Table R2: Pedido no existe ---
  @HU-ORD-04 @DecisionTable @Negative
  Scenario: DT-ORD-04-02 - Pedido no encontrado para enriquecimiento
    Given no existe un pedido con ID = 999 en PostgreSQL
    When invoco el endpoint "GET /order/999/with-user-info"
    Then recibo HTTP 404 Not Found

  # --- Decision Table R3: Timeout en usuario-service ---
  @HU-ORD-04 @DecisionTable @Negative
  Scenario: DT-ORD-04-03 - Timeout al consultar usuario vía RabbitMQ
    Given existe un pedido con ID = 5 en PostgreSQL
    And el servicio usuario-service no responde en 3000ms
    When invoco el endpoint "GET /order/5/with-user-info"
    Then recibo HTTP 500 Internal Server Error
    And la respuesta contiene mensaje de timeout

  # --- Boundary Value Analysis: Respuesta justo en el límite del timeout ---
  @HU-ORD-04 @BoundaryValue @Positive
  Scenario: BVA-ORD-04-01 - Respuesta de usuario en exactamente 2999ms
    Given existe un pedido con ID = 5 en PostgreSQL
    And el servicio usuario-service responde en exactamente 2999ms
    When invoco el endpoint "GET /order/5/with-user-info"
    Then recibo HTTP 200 OK

  # --- Boundary Value Analysis: Respuesta excede timeout por 1ms ---
  @HU-ORD-04 @BoundaryValue @Negative
  Scenario: BVA-ORD-04-02 - Respuesta de usuario en 3001ms causa timeout
    Given existe un pedido con ID = 5 en PostgreSQL
    And el servicio usuario-service responde en exactamente 3001ms
    When invoco el endpoint "GET /order/5/with-user-info"
    Then recibo HTTP 500 Internal Server Error

  # ============================================
  # HU-ORD-05: Creación de pedidos
  # ============================================

  # --- Decision Table R1: Todos los campos válidos ---
  @HU-ORD-05 @DecisionTable @Positive
  Scenario: DT-ORD-05-01 - Creación exitosa de pedido con todos los campos
    Given preparo un body con name = "Nuevo Pedido", description = "Descripción", idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido creado con un ID asignado
    And el campo state es "PENDING"
    And el campo active es true

  # --- Decision Table R2: Campo name faltante ---
  @HU-ORD-05 @DecisionTable @Negative
  Scenario: DT-ORD-05-02 - Rechazo por campo name faltante
    Given preparo un body sin el campo name
    And el body tiene description = "Descripción" y idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R3: Campo name vacío ---
  @HU-ORD-05 @DecisionTable @Negative
  Scenario: DT-ORD-05-03 - Rechazo por campo name vacío
    Given preparo un body con name = "", description = "Descripción", idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R4: Campo idUser faltante ---
  @HU-ORD-05 @DecisionTable @Negative
  Scenario: DT-ORD-05-04 - Rechazo por campo idUser faltante
    Given preparo un body con name = "Pedido" y description = "Descripción"
    And el body no tiene el campo idUser
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R5: Campo idUser no numérico ---
  @HU-ORD-05 @DecisionTable @Negative
  Scenario: DT-ORD-05-05 - Rechazo por idUser no numérico
    Given preparo un body con name = "Pedido", description = "Descripción", idUser = "abc"
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R6: Campo description opcional ausente ---
  @HU-ORD-05 @DecisionTable @Positive
  Scenario: DT-ORD-05-06 - Creación exitosa sin description (opcional)
    Given preparo un body con name = "Nuevo Pedido" y idUser = 10
    And el body no tiene el campo description
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido creado con un ID asignado

  # --- Boundary Value Analysis: name con 1 caracter (mínimo) ---
  @HU-ORD-05 @BoundaryValue @Positive
  Scenario: BVA-ORD-05-01 - Creación con name de 1 caracter
    Given preparo un body con name = "A", description = "Desc", idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 200 OK

  # --- Boundary Value Analysis: name con 255 caracteres (máximo) ---
  @HU-ORD-05 @BoundaryValue @Positive
  Scenario: BVA-ORD-05-02 - Creación con name de 255 caracteres
    Given preparo un body con name de 255 caracteres, description = "Desc", idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 200 OK

  # --- Boundary Value Analysis: name con 256 caracteres (excede) ---
  @HU-ORD-05 @BoundaryValue @Negative
  Scenario: BVA-ORD-05-03 - Rechazo de name con 256 caracteres
    Given preparo un body con name de 256 caracteres, description = "Desc", idUser = 10
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: idUser mínimo válido ---
  @HU-ORD-05 @BoundaryValue @Positive
  Scenario: BVA-ORD-05-04 - Creación con idUser = 1 (mínimo)
    Given preparo un body con name = "Pedido", description = "Desc", idUser = 1
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 200 OK

  # --- Boundary Value Analysis: idUser cero (inválido) ---
  @HU-ORD-05 @BoundaryValue @Negative
  Scenario: BVA-ORD-05-05 - Rechazo de idUser = 0
    Given preparo un body con name = "Pedido", description = "Desc", idUser = 0
    When invoco el endpoint "POST /order/add" con el body
    Then recibo HTTP 400 Bad Request

  # ============================================
  # HU-ORD-06: Eliminación de pedidos
  # ============================================

  # --- Equivalence Partitioning: ID existente ---
  @HU-ORD-06 @EquivalencePartitioning @Positive
  Scenario: EP-ORD-06-01 - Eliminación exitosa de pedido existente
    Given existe un pedido con ID = 5 en PostgreSQL
    When invoco el endpoint "DELETE /order/5"
    Then recibo HTTP 200 OK
    And el pedido con ID = 5 ya no existe en PostgreSQL

  # --- Equivalence Partitioning: ID no existente ---
  @HU-ORD-06 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-06-02 - Eliminación de pedido no existente
    Given no existe un pedido con ID = 999 en PostgreSQL
    When invoco el endpoint "DELETE /order/999"
    Then recibo HTTP 404 Not Found

  # --- Equivalence Partitioning: ID inválido ---
  @HU-ORD-06 @EquivalencePartitioning @Negative
  Scenario: EP-ORD-06-03 - Eliminación con ID no numérico
    When invoco el endpoint "DELETE /order/abc"
    Then recibo HTTP 400 Bad Request

  # --- Idempotencia: Segunda eliminación ---
  @HU-ORD-06 @Idempotency @Negative
  Scenario: EP-ORD-06-04 - Segunda eliminación del mismo pedido
    Given existía un pedido con ID = 5 que fue eliminado
    When invoco el endpoint "DELETE /order/5"
    Then recibo HTTP 404 Not Found

  # ============================================
  # HU-ORD-07: Cambio de estado de pedido
  # ============================================

  # --- Decision Table R1: Pedido existe + state válido ---
  @HU-ORD-07 @DecisionTable @Positive
  Scenario: DT-ORD-07-01 - Cambio de estado exitoso a PROCESSING
    Given existe un pedido con ID = 5 en estado "PENDING"
    When invoco el endpoint "PATCH /order/5" con body {"state": "PROCESSING"}
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido con state = "PROCESSING"
    And el pedido en PostgreSQL tiene state = "PROCESSING"

  # --- Equivalence Partitioning: Todos los estados válidos ---
  @HU-ORD-07 @EquivalencePartitioning @Positive
  Scenario Outline: EP-ORD-07-01 - Cambio a todos los estados válidos
    Given existe un pedido con ID = 5 en estado "PENDING"
    When invoco el endpoint "PATCH /order/5" con body {"state": "<nuevo_estado>"}
    Then recibo HTTP 200 OK
    And la respuesta contiene el pedido con state = "<nuevo_estado>"

    Examples:
      | nuevo_estado |
      | PENDING      |
      | PROCESSING   |
      | COMPLETED    |
      | CANCELLED    |

  # --- Decision Table R2: Pedido no existe ---
  @HU-ORD-07 @DecisionTable @Negative
  Scenario: DT-ORD-07-02 - Cambio de estado en pedido inexistente
    Given no existe un pedido con ID = 999 en PostgreSQL
    When invoco el endpoint "PATCH /order/999" con body {"state": "PROCESSING"}
    Then recibo HTTP 404 Not Found

  # --- Decision Table R3: Campo state no enviado ---
  @HU-ORD-07 @DecisionTable @Negative
  Scenario: DT-ORD-07-03 - Rechazo por campo state faltante
    Given existe un pedido con ID = 5 en PostgreSQL
    When invoco el endpoint "PATCH /order/5" con body {}
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R4: Estado inválido ---
  @HU-ORD-07 @DecisionTable @Negative
  Scenario: DT-ORD-07-04 - Rechazo por estado no válido en enum
    Given existe un pedido con ID = 5 en PostgreSQL
    When invoco el endpoint "PATCH /order/5" con body {"state": "INVALID_STATE"}
    Then recibo HTTP 400 Bad Request

  # --- Decision Table R5: ID no numérico ---
  @HU-ORD-07 @DecisionTable @Negative
  Scenario: DT-ORD-07-05 - Rechazo por ID no numérico
    When invoco el endpoint "PATCH /order/abc" con body {"state": "PROCESSING"}
    Then recibo HTTP 400 Bad Request

Feature: Gestión de Usuarios con Persistencia PostgreSQL
  Como Product Owner
  Quiero gestionar usuarios en el sistema
  Para garantizar la integridad y trazabilidad de datos en PostgreSQL con soporte de soft-delete

  Background:
    Given la conexión a PostgreSQL está disponible
    And el servicio usuario-service está activo

  # ============================================
  # HU-USR-01: Listado completo de usuarios
  # ============================================

  # --- Equivalence Partitioning: Usuarios activos existentes ---
  @HU-USR-01 @EquivalencePartitioning @Positive
  Scenario: EP-USR-01-01 - Listado exitoso de usuarios activos
    Given existen 10 usuarios activos en PostgreSQL
    And existen 2 usuarios con soft-delete
    When invoco el endpoint "GET /v1/usuarios"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista con 10 usuarios
    And ningún usuario tiene estado soft-deleted

  # --- Equivalence Partitioning: Sin usuarios activos ---
  @HU-USR-01 @EquivalencePartitioning @Positive
  Scenario: EP-USR-01-02 - Listado vacío sin usuarios activos
    Given no existen usuarios activos en PostgreSQL
    When invoco el endpoint "GET /v1/usuarios"
    Then recibo HTTP 200 OK
    And la respuesta contiene una lista vacía "[]"

  # --- Equivalence Partitioning: Error de conexión ---
  @HU-USR-01 @EquivalencePartitioning @Negative
  Scenario: EP-USR-01-03 - Error cuando PostgreSQL no está disponible
    Given la conexión a PostgreSQL no está disponible
    When invoco el endpoint "GET /v1/usuarios"
    Then recibo HTTP 503 Service Unavailable

  # ============================================
  # HU-USR-02: Consulta de usuario por ID
  # ============================================

  # --- Equivalence Partitioning: ID válido existente ---
  @HU-USR-02 @EquivalencePartitioning @Positive
  Scenario: EP-USR-02-01 - Consulta exitosa de usuario existente
    Given existe un usuario activo con ID = 5 en PostgreSQL
    And el usuario tiene name = "Juan Pérez" y email = "juan@test.com"
    When invoco el endpoint "GET /v1/usuarios/5"
    Then recibo HTTP 200 OK
    And la respuesta contiene el usuario con ID = 5
    And el campo name es "Juan Pérez"
    And el campo email es "juan@test.com"

  # --- Equivalence Partitioning: ID no existente ---
  @HU-USR-02 @EquivalencePartitioning @Negative
  Scenario: EP-USR-02-02 - Usuario no encontrado
    Given no existe un usuario con ID = 999 en PostgreSQL
    When invoco el endpoint "GET /v1/usuarios/999"
    Then recibo HTTP 404 Not Found

  # --- Equivalence Partitioning: Usuario soft-deleted ---
  @HU-USR-02 @EquivalencePartitioning @Negative
  Scenario: EP-USR-02-03 - Usuario con soft-delete no encontrado
    Given existe un usuario con ID = 5 marcado como soft-deleted
    When invoco el endpoint "GET /v1/usuarios/5"
    Then recibo HTTP 404 Not Found

  # --- Boundary Value Analysis: ID mínimo válido ---
  @HU-USR-02 @BoundaryValue @Positive
  Scenario: BVA-USR-02-01 - Consulta con ID = 1 (mínimo)
    Given existe un usuario activo con ID = 1 en PostgreSQL
    When invoco el endpoint "GET /v1/usuarios/1"
    Then recibo HTTP 200 OK

  # --- Boundary Value Analysis: ID negativo ---
  @HU-USR-02 @BoundaryValue @Negative
  Scenario: BVA-USR-02-02 - Rechazo de ID negativo
    When invoco el endpoint "GET /v1/usuarios/-1"
    Then recibo HTTP 400 Bad Request

  # ============================================
  # HU-USR-03: Creación de usuarios
  # ============================================

  # --- Equivalence Partitioning: Datos válidos ---
  @HU-USR-03 @EquivalencePartitioning @Positive
  Scenario: EP-USR-03-01 - Creación exitosa de usuario
    Given preparo un body con name = "Nuevo Usuario" y email = "nuevo@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 201 Created
    And la respuesta contiene el usuario creado con un ID asignado
    And el usuario existe en PostgreSQL con los mismos datos

  # --- Equivalence Partitioning: Email duplicado ---
  @HU-USR-03 @EquivalencePartitioning @Negative
  Scenario: EP-USR-03-02 - Rechazo por email duplicado
    Given existe un usuario con email = "existente@test.com" en PostgreSQL
    And preparo un body con name = "Otro Usuario" y email = "existente@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 400 Bad Request
    And no se crea un nuevo registro en PostgreSQL

  # --- Equivalence Partitioning: Email con formato inválido ---
  @HU-USR-03 @EquivalencePartitioning @Negative
  Scenario: EP-USR-03-03 - Rechazo por email sin formato válido
    Given preparo un body con name = "Usuario" y email = "correo-invalido"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 400 Bad Request

  # --- Equivalence Partitioning: Campo name vacío ---
  @HU-USR-03 @EquivalencePartitioning @Negative
  Scenario: EP-USR-03-04 - Rechazo por name vacío
    Given preparo un body con name = "" y email = "valido@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 400 Bad Request

  # --- Boundary Value Analysis: name con 1 caracter ---
  @HU-USR-03 @BoundaryValue @Positive
  Scenario: BVA-USR-03-01 - Creación con name de 1 caracter
    Given preparo un body con name = "A" y email = "a@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 201 Created

  # --- Boundary Value Analysis: name con 100 caracteres (máximo) ---
  @HU-USR-03 @BoundaryValue @Positive
  Scenario: BVA-USR-03-02 - Creación con name de 100 caracteres
    Given preparo un body con name de 100 caracteres y email = "largo@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 201 Created

  # --- Boundary Value Analysis: name con 101 caracteres (excede) ---
  @HU-USR-03 @BoundaryValue @Negative
  Scenario: BVA-USR-03-03 - Rechazo de name con 101 caracteres
    Given preparo un body con name de 101 caracteres y email = "excede@test.com"
    When invoco el endpoint "POST /v1/usuarios" con el body
    Then recibo HTTP 400 Bad Request

  # ============================================
  # HU-USR-04: Actualización completa (PUT)
  # ============================================

  # --- Decision Table: Usuario existe + datos válidos ---
  @HU-USR-04 @DecisionTable @Positive
  Scenario: DT-USR-04-01 - Actualización completa exitosa
    Given existe un usuario activo con ID = 5 en PostgreSQL
    And preparo un body con name = "Nombre Actualizado" y email = "actualizado@test.com"
    When invoco el endpoint "PUT /v1/usuarios/5" con el body
    Then recibo HTTP 200 OK
    And la respuesta contiene name = "Nombre Actualizado"
    And el usuario en PostgreSQL tiene los valores actualizados

  # --- Decision Table: Usuario no existe ---
  @HU-USR-04 @DecisionTable @Negative
  Scenario: DT-USR-04-02 - Actualización de usuario inexistente
    Given no existe un usuario con ID = 999 en PostgreSQL
    When invoco el endpoint "PUT /v1/usuarios/999" con body válido
    Then recibo HTTP 404 Not Found

  # --- Decision Table: Datos inválidos ---
  @HU-USR-04 @DecisionTable @Negative
  Scenario: DT-USR-04-03 - Actualización con email duplicado
    Given existe un usuario activo con ID = 5 en PostgreSQL
    And existe otro usuario con email = "ocupado@test.com"
    And preparo un body con email = "ocupado@test.com"
    When invoco el endpoint "PUT /v1/usuarios/5" con el body
    Then recibo HTTP 400 Bad Request

  # ============================================
  # HU-USR-05: Actualización parcial (PATCH)
  # ============================================

  # --- Equivalence Partitioning: Actualización parcial válida ---
  @HU-USR-05 @EquivalencePartitioning @Positive
  Scenario: EP-USR-05-01 - Actualización parcial solo de name
    Given existe un usuario activo con ID = 5 con name = "Original" y email = "original@test.com"
    When invoco el endpoint "PATCH /v1/usuarios/5" con body {"name": "Actualizado"}
    Then recibo HTTP 200 OK
    And la respuesta contiene name = "Actualizado"
    And la respuesta contiene email = "original@test.com"
    And el usuario en PostgreSQL tiene name = "Actualizado" y email = "original@test.com"

  # --- Equivalence Partitioning: Usuario no existe ---
  @HU-USR-05 @EquivalencePartitioning @Negative
  Scenario: EP-USR-05-02 - Actualización parcial de usuario inexistente
    Given no existe un usuario con ID = 999 en PostgreSQL
    When invoco el endpoint "PATCH /v1/usuarios/999" con body {"name": "Nuevo"}
    Then recibo HTTP 404 Not Found

  # --- Equivalence Partitioning: Campo inválido ---
  @HU-USR-05 @EquivalencePartitioning @Negative
  Scenario: EP-USR-05-03 - Actualización parcial con campo vacío
    Given existe un usuario activo con ID = 5 en PostgreSQL
    When invoco el endpoint "PATCH /v1/usuarios/5" con body {"name": ""}
    Then recibo HTTP 400 Bad Request

  # ============================================
  # HU-USR-06: Soft-delete de usuario
  # ============================================

  # --- Equivalence Partitioning: Usuario activo existente ---
  @HU-USR-06 @EquivalencePartitioning @Positive
  Scenario: EP-USR-06-01 - Soft-delete exitoso de usuario activo
    Given existe un usuario activo con ID = 5 en PostgreSQL
    When invoco el endpoint "DELETE /v1/usuarios/5"
    Then recibo HTTP 204 No Content
    And el usuario con ID = 5 está marcado como soft-deleted en PostgreSQL
    And el usuario no aparece en el listado de usuarios activos

  # --- Equivalence Partitioning: Usuario no existente ---
  @HU-USR-06 @EquivalencePartitioning @Negative
  Scenario: EP-USR-06-02 - Soft-delete de usuario inexistente
    Given no existe un usuario con ID = 999 en PostgreSQL
    When invoco el endpoint "DELETE /v1/usuarios/999"
    Then recibo HTTP 404 Not Found

  # --- Equivalence Partitioning: Usuario ya soft-deleted ---
  @HU-USR-06 @EquivalencePartitioning @Negative
  Scenario: EP-USR-06-03 - Segundo soft-delete del mismo usuario
    Given existe un usuario con ID = 5 marcado como soft-deleted
    When invoco el endpoint "DELETE /v1/usuarios/5"
    Then recibo HTTP 404 Not Found

  # --- Verificación de trazabilidad ---
  @HU-USR-06 @Traceability @Positive
  Scenario: EP-USR-06-04 - Usuario soft-deleted mantiene registro histórico
    Given existe un usuario activo con ID = 5 en PostgreSQL
    When invoco el endpoint "DELETE /v1/usuarios/5"
    Then recibo HTTP 204 No Content
    And la fila del usuario con ID = 5 existe en PostgreSQL
    And la fila tiene el campo de soft-delete marcado como true
```

---

## 6. TDD Alignment

### Tests a Implementar Primero (RED Phase)

#### Prioridad Alta (Core Business Logic)

| Orden | Test | Componente | Razón |
|-------|------|------------|-------|
| 1 | `OrderRepository.findAll()` | Repository | Base para HU-ORD-01 |
| 2 | `OrderRepository.findById()` | Repository | Base para HU-ORD-02, HU-ORD-04 |
| 3 | `OrderRepository.save()` | Repository | Base para HU-ORD-05 |
| 4 | `OrderService.createOrder()` | Service | Validaciones de negocio |
| 5 | `OrderService.updateState()` | Service | Máquina de estados |
| 6 | `UserRepository.findAllActive()` | Repository | Filtro soft-delete |
| 7 | `UserService.softDelete()` | Service | Lógica soft-delete |

#### Prioridad Media (Integration Points)

| Orden | Test | Componente | Razón |
|-------|------|------------|-------|
| 8 | `UserServiceProducer.requestUser()` | Messaging | Comunicación RabbitMQ |
| 9 | `OrderService.getOrderWithUserInfo()` | Service | Integración async |
| 10 | `OrderController.getOrderWithUserInfo()` | Controller | Manejo timeout |

#### Prioridad Baja (Edge Cases)

| Orden | Test | Componente | Razón |
|-------|------|------------|-------|
| 11 | Boundary tests para IDs | Controller | Validación entrada |
| 12 | Tests de idempotencia | Service | Comportamiento delete |
| 13 | Tests de concurrencia | Repository | Integridad datos |

### Mocking Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                        TEST ISOLATION                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Controller Tests:                                              │
│  ├── Mock: Service layer                                        │
│  ├── Tool: @WebMvcTest + @MockBean                              │
│  └── Focus: HTTP status, request/response mapping               │
│                                                                 │
│  Service Tests:                                                 │
│  ├── Mock: Repository, RabbitMQ Template                        │
│  ├── Tool: @ExtendWith(MockitoExtension.class)                  │
│  └── Focus: Business logic, validations                         │
│                                                                 │
│  Repository Tests:                                              │
│  ├── Mock: None (use Testcontainers PostgreSQL)                 │
│  ├── Tool: @DataJpaTest + Testcontainers                        │
│  └── Focus: Query correctness, transactions                     │
│                                                                 │
│  Messaging Tests:                                               │
│  ├── Mock: RabbitTemplate (unit) or Testcontainers (integration)│
│  ├── Tool: @MockBean or Testcontainers RabbitMQ                 │
│  └── Focus: Message format, timeout handling                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Isolation Strategy

| Nivel | Estrategia | Herramientas |
|-------|------------|--------------|
| **Unit** | Mocking completo de dependencias | Mockito, @MockBean |
| **Integration** | Containers aislados por test | Testcontainers, @DirtiesContext |
| **E2E** | Ambiente completo docker-compose | docker-compose.test.yml |

### Risk Areas for Regression

| Área | Riesgo | Tests de Regresión Recomendados |
|------|--------|--------------------------------|
| **RabbitMQ Timeout** | Cambios en `USER_REQUEST_TIMEOUT` afectan comportamiento | Test parametrizado con valores límite |
| **State Transitions** | Agregar nuevos estados al enum `State` | Test exhaustivo de todas las transiciones |
| **Soft-Delete Logic** | Inconsistencias entre listados y consultas individuales | Tests de consistencia entre endpoints |
| **DTO Mappings** | Cambios en `OrderMapper` pueden romper API | Tests de snapshot para respuestas JSON |
| **Validation Rules** | Añadir nuevas validaciones puede rechazar datos válidos previos | Tests con datos históricos de producción |
| **PostgreSQL Migration** | Cambios de esquema pueden afectar queries | Tests de migración Flyway/Liquibase |

### Test Data Management

```
┌─────────────────────────────────────────────────────────────────┐
│                    TEST DATA STRATEGY                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Unit Tests:                                                    │
│  └── Builder Pattern + Test Fixtures (in-memory)                │
│                                                                 │
│  Integration Tests:                                             │
│  └── SQL scripts + @Sql annotation                              │
│  └── Testcontainers with initialized data                       │
│                                                                 │
│  E2E Tests:                                                     │
│  └── Seeded database via docker-compose                         │
│  └── API calls for setup in BeforeAll                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Appendix: Traceability Matrix

| Acceptance Criteria | Scenario ID | Test Type | Priority |
|---------------------|-------------|-----------|----------|
| HU-ORD-01 CA-01 | EP-ORD-01-01 | Integration | Alta |
| HU-ORD-01 CA-02 | EP-ORD-01-02 | Integration | Media |
| HU-ORD-01 CA-03 | EP-ORD-01-03 | Integration | Alta |
| HU-ORD-02 CA-01 | EP-ORD-02-01 | Unit + Integration | Alta |
| HU-ORD-02 CA-02 | EP-ORD-02-02 | Unit | Alta |
| HU-ORD-02 CA-03 | EP-ORD-02-03 | Unit | Media |
| HU-ORD-03 CA-01 | EP-ORD-03-01 | Integration | Alta |
| HU-ORD-03 CA-02 | EP-ORD-03-02 | Integration | Media |
| HU-ORD-03 CA-03 | EP-ORD-03-03 | Unit | Media |
| HU-ORD-04 CA-01 | DT-ORD-04-01 | Integration | Alta |
| HU-ORD-04 CA-02 | DT-ORD-04-02 | Unit | Alta |
| HU-ORD-04 CA-03 | DT-ORD-04-03 | Integration | Alta |
| HU-ORD-05 CA-01 | DT-ORD-05-01 | Unit + Integration | Alta |
| HU-ORD-05 CA-02 | DT-ORD-05-02 | Unit | Alta |
| HU-ORD-05 CA-03 | DT-ORD-05-05 | Unit | Media |
| HU-ORD-06 CA-01 | EP-ORD-06-01 | Integration | Alta |
| HU-ORD-06 CA-02 | EP-ORD-06-02 | Unit | Media |
| HU-ORD-06 CA-03 | EP-ORD-06-03 | Unit | Media |
| HU-ORD-07 CA-01 | DT-ORD-07-01 | Unit + Integration | Alta |
| HU-ORD-07 CA-02 | DT-ORD-07-02 | Unit | Alta |
| HU-ORD-07 CA-03 | DT-ORD-07-03 | Unit | Alta |
| HU-ORD-07 CA-04 | DT-ORD-07-04 | Unit | Alta |
| HU-USR-01 AC-1 | EP-USR-01-01 | Integration | Alta |
| HU-USR-01 AC-3 | EP-USR-01-02 | Integration | Media |
| HU-USR-02 AC-1 | EP-USR-02-01 | Unit + Integration | Alta |
| HU-USR-02 AC-2 | EP-USR-02-02 | Unit | Alta |
| HU-USR-03 AC-1 | EP-USR-03-01 | Unit + Integration | Alta |
| HU-USR-03 AC-2 | EP-USR-03-02 | Unit | Alta |
| HU-USR-03 AC-3 | EP-USR-03-03 | Unit | Media |
| HU-USR-04 AC-1 | DT-USR-04-01 | Unit + Integration | Alta |
| HU-USR-04 AC-2 | DT-USR-04-02 | Unit | Alta |
| HU-USR-05 AC-1 | EP-USR-05-01 | Unit + Integration | Alta |
| HU-USR-05 AC-2 | EP-USR-05-02 | Unit | Alta |
| HU-USR-06 AC-1 | EP-USR-06-01 | Integration | Alta |
| HU-USR-06 AC-2 | EP-USR-06-02 | Unit | Alta |
| HU-USR-06 AC-4 | EP-USR-06-03 | Unit | Media |
