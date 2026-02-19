# TEST_PLAN.md — Migración de Persistencia a PostgreSQL

## 1. Overview

### 1.1 User Story Summary

Este plan de pruebas cubre la **Migración de Persistencia de JSON a PostgreSQL** para los microservicios `usuario-service` y `pedido-service`. El Epic incluye:

| ID | Historia de Usuario | Alcance |
|----|---------------------|---------|
| HU-DB-01 | Configuración de Infraestructura PostgreSQL en Docker | Infraestructura |
| HU-DB-02 | Entidad JPA Usuario con Integer | Backend - usuario-service |
| HU-DB-03 | Entidad JPA Order con Integer | Backend - pedido-service |
| HU-DB-04 | Repositorio JPA UsuarioRepository con Soft-Delete | Backend - usuario-service |
| HU-DB-05 | Repositorio JPA OrderRepository con Soft-Delete | Backend - pedido-service |
| HU-DB-06 | Configuración de Conexión JPA para usuario-service | Configuración |
| HU-DB-07 | Configuración de Conexión JPA para pedido-service | Configuración |
| HU-DB-08 | Dependencias Maven para JPA y PostgreSQL | Build |
| HU-DB-09 | Implementación de Soft-Delete en Servicios | Backend - Services |

### 1.2 Brownfield Risk Analysis

| Riesgo | Descripción | Impacto | Mitigación |
|--------|-------------|---------|------------|
| **R1: Migración a JPA** | Sistema cambia de persistencia JSON a PostgreSQL con JPA. Requiere refactorizar repositorios y servicios. | Alto | Tests de regresión en endpoints y persistencia |
| **R2: Eliminación de JSON fallback** | Sistema actual carga datos desde `users.json`/`orders.json`. Migración elimina este fallback. | Alto | Tests de arranque sin archivos JSON |
| **R3: Dependencias de persistencia ocultas** | `UserPersistenceFactory`, `IUserPersistence` y configuraciones de inicialización pueden tener dependencias al sistema JSON. | Medio | Tests de inyección de dependencias |
| **R4: Comportamiento de soft-delete** | `deleteById` actual hace hard-delete físico. Código cliente puede asumir borrado real. | Alto | Tests de queries sobre registros soft-deleted |
| **R5: Mensajes RabbitMQ** | DTOs `UserRequest`/`UserResponse` usan IDs numéricos. Mantener consistencia. | Medio | Tests de integración de mensajería |
| **R6: Frontend API clients** | `Frontend/src/services/` usa IDs numéricos. Mantener compatibilidad. | Bajo | Tests E2E tras migración |
| **R7: Constraints de BD no existentes** | Validación actual es a nivel de servicio, no de BD. Unicidad de mail puede fallar en escenarios de concurrencia. | Medio | Tests de constraint violation |
| **R8: Variables de entorno** | Credenciales deben externalizarse. Ausencia de variables debe fallar de forma clara. | Medio | Tests de configuración errónea |

---

## 2. Applied Testing Principles

### 2.1 Principle Identified

**Principio #2: Las pruebas exhaustivas son imposibles** — Dado el espacio de entrada complejo (integers, strings con longitudes variables, enums de 7 valores, booleanos) y las múltiples rutas de ejecución (CRUD × soft-delete × constraints), se deben aplicar técnicas de diseño de prueba formales para maximizar cobertura con un conjunto manejable de casos.

**Principio #7: La falacia de la ausencia de errores** — Verificar solo casos positivos no garantiza calidad. El sistema debe rechazar correctamente entradas inválidas (emails duplicados, usuarios inexistentes, credenciales faltantes).

### 2.2 Justification

La migración de persistencia es de alto riesgo porque:
1. Cambia el sistema de persistencia (JSON → PostgreSQL con JPA)
2. Altera el comportamiento de eliminación (hard → soft delete)
3. Introduce dependencias externas (PostgreSQL, variables de entorno)
4. Afecta contratos de integración (RabbitMQ, API REST)

Por tanto, se requiere aplicación rigurosa de **Equivalence Partitioning**, **Boundary Value Analysis** y **Decision Tables** para cubrir sistemáticamente el espacio de entrada.

---

## 3. Test Levels Strategy

### 3.1 Unit Testing

| Componente | Responsabilidad | Técnica de Aislamiento |
|------------|-----------------|------------------------|
| `Usuario` (Entity) | Validar mapeo JPA, getters/setters, field constraints | Mock de EntityManager |
| `Order` (Entity) | Validar mapeo JPA, enum State, field constraints | Mock de EntityManager |
| `UsuarioRepository` | Queries soft-delete, métodos derivados | `@DataJpaTest` con H2 in-memory |
| `OrderRepository` | Queries soft-delete, filtros por estado | `@DataJpaTest` con H2 in-memory |
| `UsuarioService` | Lógica de soft-delete, validación de unicidad | Mock de Repository |
| `OrderService` | Lógica de soft-delete, mapeo de estados | Mock de Repository |

**Cobertura esperada**: ≥ 80% de líneas en entidades, repositorios y servicios.

### 3.2 Integration Testing

| Escenario de Integración | Componentes Involucrados | Entorno |
|-------------------------|--------------------------|---------|
| JPA ↔ PostgreSQL | Entity + Repository + PostgreSQL | Testcontainers PostgreSQL |
| Service ↔ Repository | Service + JPA Repository + PostgreSQL | Testcontainers PostgreSQL |
| Configuración externalizada | Application + Environment Variables | Spring Test con `@TestPropertySource` |
| RabbitMQ con IDs | UserServiceProducer/Consumer + DTOs Integer | Testcontainers RabbitMQ |
| Docker Compose startup | PostgreSQL containers + Services | Docker Compose test |

**Cobertura esperada**: Flujos completos CRUD para ambas entidades, incluyendo soft-delete y constraints.

### 3.3 System Testing

| Escenario End-to-End | Validación |
|---------------------|------------|
| Crear usuario vía API → Persistencia en PostgreSQL | ID generado, respuesta HTTP 201 |
| Crear orden vía API → Persistencia en PostgreSQL | ID generado, idUser como Integer |
| Soft-delete usuario → Query retorna 404 | Registro existe con active=false |
| Email duplicado → HTTP 409 | Constraint violation manejada |
| Restart contenedores → Datos persisten | Volumen PostgreSQL funcional |
| Credenciales ausentes → Fallo de arranque | Error descriptivo |

---

## 4. Test Design Application

### 4.1 Equivalence Partitioning

#### 4.1.1 Entidad Usuario — Campo `name`

| Partición | Descripción | Valores Representativos | Resultado Esperado |
|-----------|-------------|------------------------|-------------------|
| **VP1** | Nombre válido (2-50 chars) | "Juan", "María García", "AB" | Válido |
| **IP1** | Nombre vacío | "", null | Rechazo (NOT NULL) |
| **IP2** | Nombre muy corto (<2 chars) | "A" | Rechazo (min 2) |
| **IP3** | Nombre muy largo (>50 chars) | "A"×51 | Rechazo (max 50) |

#### 4.1.2 Entidad Usuario — Campo `password`

| Partición | Descripción | Valores Representativos | Resultado Esperado |
|-----------|-------------|------------------------|-------------------|
| **VP2** | Password válido (≥8 chars) | "password123", "12345678" | Válido |
| **IP4** | Password vacío | "", null | Rechazo (NOT NULL) |
| **IP5** | Password corto (<8 chars) | "abc", "1234567" | Rechazo (min 8) |

#### 4.1.3 Entidad Usuario — Campo `mail`

| Partición | Descripción | Valores Representativos | Resultado Esperado |
|-----------|-------------|------------------------|-------------------|
| **VP3** | Email válido único | "test@example.com", "user@domain.org" | Válido |
| **IP6** | Email vacío | "", null | Rechazo (NOT NULL) |
| **IP7** | Email duplicado (activo) | Email ya existente con active=true | Rechazo (UNIQUE) |
| **VP4** | Email "duplicado" pero inactivo | Email existe con active=false | Válido (UNIQUE solo activos) |

#### 4.1.4 Entidad Order — Campo `state`

| Partición | Descripción | Valores Representativos | Resultado Esperado |
|-----------|-------------|------------------------|-------------------|
| **VP5** | Estado válido del enum | PROCESSING, DELIVERED, CANCELED | Válido |
| **IP8** | Estado nulo | null | Rechazo (NOT NULL) |
| **IP9** | Estado inválido (no enum) | "UNKNOWN", "PENDING" | Rechazo (formato inválido) |

#### 4.1.5 Entidad Order — Campo `idUser`

| Partición | Descripción | Valores Representativos | Resultado Esperado |
|-----------|-------------|------------------------|-------------------|
| **VP6** | ID válido de usuario existente | ID activo (ej: 1, 5, 100) | Válido |
| **VP7** | ID válido de usuario inexistente | ID numérico pero no existe | Válido (no FK física) |
| **IP10** | idUser nulo | null | Rechazo (NOT NULL) |

### 4.2 Boundary Value Analysis

#### 4.2.1 Campo `name` — Longitud (2-50 caracteres)

| Frontera | Valor | Resultado Esperado |
|----------|-------|-------------------|
| `n-1` del mínimo | 1 caracter ("A") | Rechazo |
| `n` mínimo | 2 caracteres ("AB") | Válido |
| `n+1` del mínimo | 3 caracteres ("ABC") | Válido |
| `n-1` del máximo | 49 caracteres | Válido |
| `n` máximo | 50 caracteres | Válido |
| `n+1` del máximo | 51 caracteres | Rechazo |

#### 4.2.2 Campo `password` — Longitud mínima 8 caracteres

| Frontera | Valor | Resultado Esperado |
|----------|-------|-------------------|
| `n-1` del mínimo | 7 caracteres ("1234567") | Rechazo |
| `n` mínimo | 8 caracteres ("12345678") | Válido |
| `n+1` del mínimo | 9 caracteres ("123456789") | Válido |

#### 4.2.3 Query Performance — Tiempo de respuesta (≤ 100ms)

| Frontera | Valor | Resultado Esperado |
|----------|-------|-------------------|
| Dentro de límite | 50ms, 99ms | Aceptable |
| En el límite | 100ms | Aceptable |
| Fuera de límite | 101ms, 200ms | No aceptable (alerta) |

### 4.3 Decision Table

#### 4.3.1 Tabla de Decisión: Creación de Usuario

| Condiciones | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 |
|-------------|----|----|----|----|----|----|----|----|
| Name válido (2-50 chars) | Y | Y | Y | Y | N | Y | Y | Y |
| Password válido (≥8 chars) | Y | Y | Y | Y | Y | N | Y | Y |
| Mail no vacío | Y | Y | Y | Y | Y | Y | N | Y |
| Mail único (entre activos) | Y | Y | N | Y | Y | Y | Y | N |
| BD conectada | Y | N | Y | Y | Y | Y | Y | Y |
| **Acciones** | | | | | | | | |
| Usuario creado con ID | ✓ | | | | | | | |
| HTTP 201 | ✓ | | | | | | | |
| HTTP 500 (BD error) | | ✓ | | | | | | |
| HTTP 409 (Conflict) | | | ✓ | | | | | ✓ |
| HTTP 400 (Validation) | | | | | ✓ | ✓ | ✓ | |

#### 4.3.2 Tabla de Decisión: Soft-Delete Usuario

| Condiciones | R1 | R2 | R3 |
|-------------|----|----|----| 
| Usuario existe en BD | Y | Y | N |
| Usuario está activo (active=true) | Y | N | - |
| **Acciones** | | | |
| active → false | ✓ | | |
| HTTP 204/200 | ✓ | | |
| HTTP 404 Not Found | | ✓ | ✓ |
| Registro físico permanece | ✓ | ✓ | |

#### 4.3.3 Tabla de Decisión: Consulta de Usuarios

| Condiciones | R1 | R2 | R3 |
|-------------|----|----|----| 
| Existen usuarios activos | Y | Y | N |
| Existen usuarios inactivos | Y | N | Y |
| **Acciones** | | | |
| Retorna solo activos | ✓ | ✓ | |
| Lista vacía | | | ✓ |
| Inactivos excluidos | ✓ | | ✓ |

#### 4.3.4 Tabla de Decisión: Configuración de Conexión

| Condiciones | R1 | R2 | R3 | R4 | R5 |
|-------------|----|----|----|----|----| 
| DB_USER definido | Y | N | Y | Y | Y |
| DB_PASSWORD definido | Y | Y | N | Y | Y |
| DB_HOST alcanzable | Y | Y | Y | N | Y |
| PostgreSQL disponible | Y | Y | Y | Y | N |
| **Acciones** | | | | | |
| Conexión exitosa | ✓ | | | | |
| Error config claro | | ✓ | ✓ | | |
| Error conexión | | | | ✓ | ✓ |
| Servicio no inicia | | ✓ | ✓ | ✓ | ✓ |

---

## 5. Gherkin Scenarios

```gherkin
Feature: Persistencia de Usuarios con PostgreSQL y JPA
  Como desarrollador backend
  Quiero migrar la persistencia de usuarios de JSON a PostgreSQL
  Para garantizar integridad transaccional y escalabilidad

  Background:
    Given el servicio usuario-service está corriendo con PostgreSQL
    And la base de datos "usuarios_db" está accesible
    And las variables de entorno DB_USER y DB_PASSWORD están configuradas

  # ============================================================
  # ESCENARIOS DERIVADOS DE EQUIVALENCE PARTITIONING
  # ============================================================

  @EquivalencePartitioning @VP1 @HU-DB-02
  Scenario: Crear usuario con nombre válido dentro del rango permitido
    Given no existe un usuario con mail "validname@test.com"
    When se envía POST /api/v1/usuarios con:
      | name     | password     | mail               |
      | Juan     | password123  | validname@test.com |
    Then se retorna HTTP 201 Created
    And el response contiene un campo "id" de tipo Integer
    And el usuario se persiste en la tabla "usuarios" con active=true

  @EquivalencePartitioning @IP1 @HU-DB-02
  Scenario: Rechazar usuario con nombre vacío
    When se envía POST /api/v1/usuarios con:
      | name | password    | mail          |
      |      | password123 | empty@test.com|
    Then se retorna HTTP 400 Bad Request
    And el mensaje de error indica "name es requerido"

  @EquivalencePartitioning @IP1 @HU-DB-02
  Scenario: Rechazar usuario con nombre nulo
    When se envía POST /api/v1/usuarios con body JSON:
      """
      {
        "name": null,
        "password": "password123",
        "mail": "null@test.com"
      }
      """
    Then se retorna HTTP 400 Bad Request
    And el mensaje de error indica violación de constraint NOT NULL

  @EquivalencePartitioning @VP2 @HU-DB-02
  Scenario: Crear usuario con password válido de 8 caracteres mínimo
    Given no existe un usuario con mail "validpass@test.com"
    When se envía POST /api/v1/usuarios con:
      | name  | password | mail              |
      | Pedro | 12345678 | validpass@test.com|
    Then se retorna HTTP 201 Created
    And el usuario se persiste correctamente

  @EquivalencePartitioning @IP4 @HU-DB-02
  Scenario: Rechazar usuario con password vacío
    When se envía POST /api/v1/usuarios con:
      | name  | password | mail             |
      | Pedro |          | nopass@test.com  |
    Then se retorna HTTP 400 Bad Request
    And el mensaje de error indica "password es requerido"

  @EquivalencePartitioning @IP5 @HU-DB-02
  Scenario: Rechazar usuario con password menor a 8 caracteres
    When se envía POST /api/v1/usuarios con:
      | name  | password | mail              |
      | Pedro | abc123   | shortpwd@test.com |
    Then se retorna HTTP 400 Bad Request
    And el mensaje de error indica "password debe tener mínimo 8 caracteres"

  @EquivalencePartitioning @VP3 @HU-DB-02
  Scenario: Crear usuario con email válido único
    Given no existe un usuario con mail "unique@domain.com"
    When se envía POST /api/v1/usuarios con:
      | name   | password    | mail              |
      | Carlos | password123 | unique@domain.com |
    Then se retorna HTTP 201 Created
    And el campo "mail" se almacena como "unique@domain.com"

  @EquivalencePartitioning @IP6 @HU-DB-02
  Scenario: Rechazar usuario con email vacío
    When se envía POST /api/v1/usuarios con:
      | name   | password    | mail |
      | Carlos | password123 |      |
    Then se retorna HTTP 400 Bad Request
    And el mensaje de error indica "mail es requerido"

  @EquivalencePartitioning @IP7 @HU-DB-04 @AC-07
  Scenario: Rechazar email duplicado entre usuarios activos
    Given existe un usuario activo con mail "duplicate@test.com"
    When se envía POST /api/v1/usuarios con:
      | name  | password    | mail               |
      | Maria | password123 | duplicate@test.com |
    Then se retorna HTTP 409 Conflict
    And el mensaje de error indica "email ya existe"

  @EquivalencePartitioning @VP4 @HU-DB-04
  Scenario: Permitir email que existe solo en usuario inactivo (soft-deleted)
    Given existe un usuario con mail "reused@test.com" y active=false
    And no existe usuario activo con mail "reused@test.com"
    When se envía POST /api/v1/usuarios con:
      | name    | password    | mail             |
      | NewUser | password123 | reused@test.com  |
    Then se retorna HTTP 201 Created
    And el nuevo usuario tiene active=true

  @EquivalencePartitioning @VP5 @HU-DB-03
  Scenario: Crear orden con estado válido del enum
    Given existe un usuario activo con ID 1
    When se crea una orden con:
      | name        | description    | idUser | state      |
      | Orden Test  | Descripción    | 1      | PROCESSING |
    Then la orden se persiste con ID generado
    And el campo "state" se almacena como "PROCESSING" (STRING en BD)

  @EquivalencePartitioning @VP5 @HU-DB-03
  Scenario Outline: Crear orden con cada estado válido del enum State
    Given existe un usuario activo
    When se crea una orden con estado "<state>"
    Then la orden se persiste correctamente con state="<state>"

    Examples:
      | state                   |
      | PROCESSING              |
      | TRAVELING_TO_WAREHOUSE  |
      | IN_WAREHOUSE            |
      | TRAVELING_TO_YOUR_HOUSE |
      | ON_THE_STREET           |
      | DELIVERED               |
      | CANCELED                |

  @EquivalencePartitioning @IP8 @HU-DB-03
  Scenario: Rechazar orden con estado nulo
    Given existe un usuario activo
    When se intenta crear una orden con state=null
    Then se retorna error de validación
    And el mensaje indica "state es requerido"

  # ============================================================
  # ESCENARIOS DERIVADOS DE BOUNDARY VALUE ANALYSIS
  # ============================================================

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 1 caracter (bajo el mínimo)
    When se envía POST /api/v1/usuarios con:
      | name | password    | mail           |
      | A    | password123 | onechar@test.com|
    Then se retorna HTTP 400 Bad Request
    And el mensaje indica "name debe tener mínimo 2 caracteres"

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 2 caracteres (límite inferior válido)
    Given no existe usuario con mail "twochar@test.com"
    When se envía POST /api/v1/usuarios con:
      | name | password    | mail            |
      | AB   | password123 | twochar@test.com|
    Then se retorna HTTP 201 Created

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 3 caracteres (sobre el mínimo)
    Given no existe usuario con mail "threechar@test.com"
    When se envía POST /api/v1/usuarios con:
      | name | password    | mail              |
      | ABC  | password123 | threechar@test.com|
    Then se retorna HTTP 201 Created

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 49 caracteres (bajo el máximo)
    Given no existe usuario con mail "fortynine@test.com"
    When se envía POST /api/v1/usuarios con:
      | name                                              | password    | mail               |
      | AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA  | password123 | fortynine@test.com |
    Then se retorna HTTP 201 Created

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 50 caracteres (límite superior válido)
    Given no existe usuario con mail "fifty@test.com"
    When se envía POST /api/v1/usuarios con:
      | name                                               | password    | mail           |
      | AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA | password123 | fifty@test.com |
    Then se retorna HTTP 201 Created

  @BoundaryValue @HU-DB-02
  Scenario: Nombre con exactamente 51 caracteres (sobre el máximo)
    When se envía POST /api/v1/usuarios con:
      | name                                                | password    | mail              |
      | AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA | password123 | fiftyone@test.com |
    Then se retorna HTTP 400 Bad Request
    And el mensaje indica "name debe tener máximo 50 caracteres"

  @BoundaryValue @HU-DB-02
  Scenario: Password con exactamente 7 caracteres (bajo el mínimo)
    When se envía POST /api/v1/usuarios con:
      | name  | password | mail             |
      | Test  | 1234567  | sevenchar@test.com|
    Then se retorna HTTP 400 Bad Request
    And el mensaje indica "password debe tener mínimo 8 caracteres"

  @BoundaryValue @HU-DB-02
  Scenario: Password con exactamente 8 caracteres (límite válido)
    Given no existe usuario con mail "eightchar@test.com"
    When se envía POST /api/v1/usuarios con:
      | name  | password | mail               |
      | Test  | 12345678 | eightchar@test.com |
    Then se retorna HTTP 201 Created

  @BoundaryValue @HU-DB-02
  Scenario: Password con exactamente 9 caracteres (sobre el mínimo)
    Given no existe usuario con mail "ninechar@test.com"
    When se envía POST /api/v1/usuarios con:
      | name  | password  | mail              |
      | Test  | 123456789 | ninechar@test.com |
    Then se retorna HTTP 201 Created

  # ============================================================
  # ESCENARIOS DERIVADOS DE DECISION TABLE - CREACIÓN USUARIO
  # ============================================================

  @DecisionTable @R1 @HU-DB-02 @AC-01
  Scenario: DT-Usuario-R1: Creación exitosa con todos los campos válidos
    Given la base de datos PostgreSQL está disponible
    And no existe usuario con mail "dtvalid@test.com"
    When se envía POST /api/v1/usuarios con:
      | name       | password    | mail            |
      | ValidUser  | password123 | dtvalid@test.com|
    Then se retorna HTTP 201 Created
    And el response contiene ID (Integer) en campo "id"
    And el usuario existe en tabla "usuarios" con active=true

  @DecisionTable @R2 @HU-DB-06
  Scenario: DT-Usuario-R2: Fallo cuando BD no está conectada
    Given la base de datos PostgreSQL NO está disponible
    When se envía POST /api/v1/usuarios con datos válidos
    Then se retorna HTTP 500 Internal Server Error
    And el log indica error de conexión a base de datos

  @DecisionTable @R3 @HU-DB-04 @AC-07
  Scenario: DT-Usuario-R3: Conflicto por email duplicado
    Given existe usuario activo con mail "conflict@test.com"
    When se envía POST /api/v1/usuarios con:
      | name     | password    | mail              |
      | NewUser  | password123 | conflict@test.com |
    Then se retorna HTTP 409 Conflict

  @DecisionTable @R5 @HU-DB-02
  Scenario: DT-Usuario-R5: Rechazo por nombre inválido
    When se envía POST /api/v1/usuarios con:
      | name | password    | mail              |
      | X    | password123 | invalid@test.com  |
    Then se retorna HTTP 400 Bad Request

  @DecisionTable @R6 @HU-DB-02
  Scenario: DT-Usuario-R6: Rechazo por password inválido
    When se envía POST /api/v1/usuarios con:
      | name      | password | mail             |
      | ValidName | short    | invalid@test.com |
    Then se retorna HTTP 400 Bad Request

  @DecisionTable @R7 @HU-DB-02
  Scenario: DT-Usuario-R7: Rechazo por mail vacío
    When se envía POST /api/v1/usuarios con:
      | name      | password    | mail |
      | ValidName | password123 |      |
    Then se retorna HTTP 400 Bad Request

  # ============================================================
  # ESCENARIOS DERIVADOS DE DECISION TABLE - SOFT-DELETE
  # ============================================================

  @DecisionTable @SoftDelete @R1 @HU-DB-09 @AC-03
  Scenario: DT-Delete-R1: Soft-delete exitoso de usuario activo
    Given existe un usuario activo con ID 1
    When se envía DELETE /api/v1/usuarios/1
    Then se retorna HTTP 200 OK o 204 No Content
    And el campo "active" del usuario cambia a false
    And el registro físico permanece en la tabla "usuarios"
    And posteriores GET /api/v1/usuarios no incluyen este usuario

  @DecisionTable @SoftDelete @R2 @HU-DB-09 @AC-08
  Scenario: DT-Delete-R2: Error al intentar eliminar usuario ya inactivo
    Given existe un usuario con ID 2 y active=false
    When se envía DELETE /api/v1/usuarios/2
    Then se retorna HTTP 404 Not Found

  @DecisionTable @SoftDelete @R3 @HU-DB-09
  Scenario: DT-Delete-R3: Error al intentar eliminar usuario inexistente
    Given no existe usuario con ID 9999
    When se envía DELETE /api/v1/usuarios/9999
    Then se retorna HTTP 404 Not Found

  # ============================================================
  # ESCENARIOS DERIVADOS DE DECISION TABLE - CONSULTAS
  # ============================================================

  @DecisionTable @Query @R1 @HU-DB-04 @AC-02
  Scenario: DT-Query-R1: Listar retorna solo usuarios activos
    Given existen usuarios:
      | id | name    | active |
      | 10 | Active1 | true   |
      | 11 | Active2 | true   |
      | 12 | Deleted | false  |
    When se envía GET /api/v1/usuarios
    Then se retorna HTTP 200 OK
    And el response contiene 2 usuarios
    And el response NO contiene usuario "Deleted"

  @DecisionTable @Query @R2 @HU-DB-04
  Scenario: DT-Query-R2: Listar cuando solo hay usuarios activos
    Given existen solo usuarios con active=true
    When se envía GET /api/v1/usuarios
    Then se retorna HTTP 200 OK
    And se retornan todos los usuarios

  @DecisionTable @Query @R3 @HU-DB-04
  Scenario: DT-Query-R3: Lista vacía cuando todos están inactivos
    Given todos los usuarios tienen active=false
    When se envía GET /api/v1/usuarios
    Then se retorna HTTP 200 OK
    And el response es una lista vacía

  @DecisionTable @Query @HU-DB-04 @AC-08
  Scenario: Consulta por ID de usuario soft-deleted retorna 404
    Given existe usuario con ID 20 y active=false
    When se envía GET /api/v1/usuarios/20
    Then se retorna HTTP 404 Not Found
    And el registro existe en BD pero no es visible via API

  # ============================================================
  # ESCENARIOS DERIVADOS DE DECISION TABLE - CONFIGURACIÓN
  # ============================================================

  @DecisionTable @Config @R1 @HU-DB-06 @AC-05
  Scenario: DT-Config-R1: Conexión exitosa con variables correctas
    Given el archivo .env contiene:
      | variable    | valor       |
      | DB_USER     | admin       |
      | DB_PASSWORD | secure_pass |
      | DB_HOST     | localhost   |
      | DB_PORT     | 5432        |
      | DB_NAME     | usuarios_db |
    And PostgreSQL está corriendo en localhost:5432
    When el servicio usuario-service inicia
    Then la conexión a BD se establece correctamente
    And no hay credenciales hardcodeadas en application.properties

  @DecisionTable @Config @R2 @HU-DB-06 @AC-09
  Scenario: DT-Config-R2: Fallo por DB_USER no definido
    Given la variable DB_USER NO está definida
    And DB_PASSWORD está definida
    When el servicio usuario-service intenta iniciar
    Then el servicio falla con error de configuración
    And el mensaje indica "DB_USER is required"

  @DecisionTable @Config @R3 @HU-DB-06 @AC-09
  Scenario: DT-Config-R3: Fallo por DB_PASSWORD no definido
    Given la variable DB_USER está definida
    And la variable DB_PASSWORD NO está definida
    When el servicio usuario-service intenta iniciar
    Then el servicio falla con error de configuración
    And el mensaje indica "DB_PASSWORD is required"

  @DecisionTable @Config @R4 @HU-DB-06
  Scenario: DT-Config-R4: Fallo por host inalcanzable
    Given las variables DB_USER y DB_PASSWORD están definidas
    And DB_HOST apunta a "nonexistent-host"
    When el servicio usuario-service intenta iniciar
    Then el servicio falla con error de conexión
    And el mensaje indica "Connection refused" o "Unknown host"

  @DecisionTable @Config @R5 @HU-DB-06
  Scenario: DT-Config-R5: Fallo por PostgreSQL no disponible
    Given las variables de entorno están correctamente configuradas
    And PostgreSQL NO está corriendo
    When el servicio usuario-service intenta iniciar
    Then el servicio falla con error de conexión

  # ============================================================
  # ESCENARIOS DE PERSISTENCIA Y ÓRDENES
  # ============================================================

  @EquivalencePartitioning @HU-DB-03 @AC-04
  Scenario: Crear orden con ID generado automáticamente
    Given el servicio pedido-service está corriendo con PostgreSQL
    And existe un usuario activo
    When se crea una orden con:
      | name         | description      | state      |
      | Orden Nueva  | Desc de prueba   | PROCESSING |
    Then la orden se persiste con ID generado automáticamente
    And idUser se almacena como Integer
    And active=true por defecto

  @SoftDelete @HU-DB-05
  Scenario: Soft-delete de orden activa
    Given existe una orden activa con ID 1
    When se elimina la orden con ID 1
    Then el campo active de la orden cambia a false
    And el registro físico permanece en tabla "orders"

  @Query @HU-DB-05
  Scenario: Listar órdenes retorna solo activas
    Given existen órdenes activas e inactivas en la BD
    When se consulta GET /order/all
    Then se retornan solo las órdenes con active=true

  @Query @HU-DB-05
  Scenario: Filtrar órdenes por estado retorna solo activas de ese estado
    Given existen órdenes:
      | state      | active |
      | PROCESSING | true   |
      | PROCESSING | false  |
      | DELIVERED  | true   |
    When se consulta órdenes con state=PROCESSING
    Then se retorna solo 1 orden (la activa con PROCESSING)

  @Query @HU-DB-05
  Scenario: Filtrar órdenes por usuario retorna solo activas del usuario
    Given existe usuario con ID 1
    And existen órdenes para ese usuario:
      | active |
      | true   |
      | true   |
      | false  |
    When se consulta órdenes por idUser=1
    Then se retornan 2 órdenes (solo las activas)

  # ============================================================
  # ESCENARIOS DE INFRAESTRUCTURA DOCKER
  # ============================================================

  @Infrastructure @HU-DB-01
  Scenario: Contenedores PostgreSQL inician correctamente
    Given el archivo docker-compose.yml contiene servicios postgres-usuarios y postgres-pedidos
    When se ejecuta "docker-compose up -d"
    Then el contenedor postgres-usuarios está healthy en puerto 5432
    And el contenedor postgres-pedidos está healthy en puerto 5433
    And cada contenedor tiene su volumen persistente

  @Infrastructure @HU-DB-01 @AC-06
  Scenario: Datos persisten tras restart de contenedores
    Given se han creado usuarios y órdenes en PostgreSQL
    When se reinician los contenedores con "docker-compose restart"
    Then los datos de usuarios persisten
    And los datos de órdenes persisten
    And los IDs permanecen iguales

  @Infrastructure @HU-DB-01
  Scenario: Bases de datos aisladas por servicio
    Given postgres-usuarios contiene base "usuarios_db"
    And postgres-pedidos contiene base "pedidos_db"
    When usuario-service intenta conectar
    Then solo accede a usuarios_db
    And NO puede acceder a pedidos_db

  # ============================================================
  # ESCENARIOS DE DEPENDENCIAS Y BUILD
  # ============================================================

  @Build @HU-DB-08
  Scenario: Dependencias JPA y PostgreSQL en usuario-service
    Given el archivo Backend/usuario-service/pom.xml existe
    When se verifica el contenido de pom.xml
    Then contiene dependencia "spring-boot-starter-data-jpa"
    And contiene dependencia "postgresql" con scope "runtime"

  @Build @HU-DB-08
  Scenario: Dependencias JPA y PostgreSQL en pedido-service
    Given el archivo Backend/pedido-service/pom.xml existe
    When se verifica el contenido de pom.xml
    Then contiene dependencia "spring-boot-starter-data-jpa"
    And contiene dependencia "postgresql" con scope "runtime"

  @Build @HU-DB-08
  Scenario: Build exitoso de ambos servicios con nuevas dependencias
    When se ejecuta "mvn -f Backend/usuario-service/pom.xml compile"
    And se ejecuta "mvn -f Backend/pedido-service/pom.xml compile"
    Then ambos builds completan sin errores

  # ============================================================
  # ESCENARIOS DE DDL Y ESQUEMA
  # ============================================================

  @Schema @HU-DB-02 @FR-01
  Scenario: DDL auto crea tabla usuarios al iniciar
    Given la base de datos usuarios_db está vacía
    And spring.jpa.hibernate.ddl-auto=update
    When el servicio usuario-service inicia
    Then la tabla "usuarios" se crea automáticamente
    And tiene columnas: id (SERIAL/Integer), name, password, mail, active

  @Schema @HU-DB-03 @FR-01
  Scenario: DDL auto crea tabla orders al iniciar
    Given la base de datos pedidos_db está vacía
    And spring.jpa.hibernate.ddl-auto=update
    When el servicio pedido-service inicia
    Then la tabla "orders" se crea automáticamente
    And tiene columnas: id (SERIAL/Integer), name, description, id_user (Integer), state, active

  @Schema @HU-DB-02 @FR-05
  Scenario: Constraint UNIQUE en mail se aplica a nivel de BD
    Given la tabla usuarios tiene constraint unique en "mail"
    And existe usuario con mail "constrained@test.com" y active=true
    When se intenta INSERT directo en BD con mail "constrained@test.com"
    Then la BD rechaza con constraint violation
    And el error indica "unique constraint"

  # ============================================================
  # ESCENARIOS DE PERFORMANCE (NFR)
  # ============================================================

  @Performance @NFR-01
  Scenario: Query simple de usuarios responde en menos de 100ms
    Given existen 100 usuarios en la base de datos
    When se envía GET /api/v1/usuarios
    Then el tiempo de respuesta es menor o igual a 100ms

  @Performance @NFR-01
  Scenario: Query por ID responde en menos de 100ms
    Given existe un usuario con ID conocido
    When se envía GET /api/v1/usuarios/{id}
    Then el tiempo de respuesta es menor o igual a 100ms

  @Performance @NFR-01
  Scenario: Creación de usuario responde en menos de 100ms
    When se envía POST /api/v1/usuarios con datos válidos
    Then el tiempo de respuesta es menor o igual a 100ms
```

---

## 6. TDD Alignment

### 6.1 Tests to Implement First (RED Phase)

El orden de implementación TDD recomendado sigue la dependencia de componentes:

#### Fase 1: Entidades y Repositorios (Foundation)

| Test | Componente | Prioridad |
|------|------------|-----------|
| `UsuarioEntityTest` | Usuario.java | 1 |
| `OrderEntityTest` | Order.java | 1 |
| `UsuarioRepositoryTest` | UsuarioRepository interface | 2 |
| `OrderRepositoryTest` | OrderRepository interface | 2 |

**Tests iniciales en RED:**

```java
// UsuarioRepositoryTest.java
@Test
void findByActiveTrue_shouldReturnOnlyActiveUsers() {
    // RED: Repository interface doesn't exist yet
}

@Test
void findByIdAndActiveTrue_shouldReturnEmptyForInactiveUser() {
    // RED: Method doesn't exist yet
}

@Test
void existsByMailAndActiveTrue_shouldReturnFalseForInactiveMail() {
    // RED: Method doesn't exist yet
}
```

#### Fase 2: Servicios con Soft-Delete

| Test | Componente | Prioridad |
|------|------------|-----------|
| `UsuarioServiceSoftDeleteTest` | UsuarioService.delete() | 3 |
| `OrderServiceSoftDeleteTest` | OrderService.delete() | 3 |

**Tests iniciales en RED:**

```java
// UsuarioServiceTest.java
@Test
void delete_shouldSetActiveFalse_notPhysicalDelete() {
    // RED: Current delete does hard delete
}

@Test
void findAll_shouldExcludeSoftDeletedUsers() {
    // RED: Current findAll returns all
}
```

#### Fase 3: Integración con PostgreSQL

| Test | Componente | Prioridad |
|------|------------|-----------|
| `UsuarioRepositoryPostgresIT` | JPA + PostgreSQL | 4 |
| `OrderRepositoryPostgresIT` | JPA + PostgreSQL | 4 |
| `ApplicationStartupIT` | Config + Connection | 5 |

### 6.2 Mocking Strategy

| Capa | Qué Mockear | Framework |
|------|-------------|-----------|
| **Controller Tests** | Service layer | `@MockBean` |
| **Service Unit Tests** | Repository interfaces | `Mockito.mock()` |
| **Repository Unit Tests** | EntityManager (si es necesario) | `@DataJpaTest` con H2 |
| **Integration Tests** | Nada (usar Testcontainers) | `@Testcontainers` |

**Ejemplo de Mock para Service:**

```java
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @InjectMocks
    private UsuarioService usuarioService;
    
    @Test
    void delete_shouldCallSaveWithActiveFalse() {
        Integer id = 1;
        Usuario usuario = new Usuario(id, "Test", "password", "test@mail.com", true);
        
        when(usuarioRepository.findByIdAndActiveTrue(id))
            .thenReturn(Optional.of(usuario));
        
        usuarioService.delete(id);
        
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
    }
}
```

### 6.3 Isolation Strategy

| Nivel | Estrategia de Aislamiento |
|-------|--------------------------|
| **Unit Tests** | H2 in-memory DB con `@DataJpaTest` o mocks puros |
| **Integration Tests** | Testcontainers PostgreSQL con `@Container` |
| **System Tests** | Docker Compose con BD limpia por suite |

**Testcontainers Setup:**

```java
@Testcontainers
@SpringBootTest
class UsuarioRepositoryPostgresIT {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("usuarios_db")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### 6.4 Risk Areas for Regression

| Área de Riesgo | Tipo de Regresión | Tests de Cobertura |
|----------------|-------------------|-------------------|
| **Migración JSON → PostgreSQL** | Fallo en persistencia, pérdida de datos | Tests de integración con Testcontainers |
| **RabbitMQ DTOs** | Mensajes con formato incorrecto | Tests de integración de mensajería |
| **Frontend API clients** | Llamadas fallidas por cambio de persistencia | Tests de contrato (Pact) |
| **Eliminación de JSON fallback** | Fallo en inicialización | Tests de arranque sin archivos JSON |
| **Queries sin filtro soft-delete** | Exposición de datos eliminados | Tests de seguridad/privacidad |
| **Constraint violations no manejadas** | HTTP 500 en lugar de 409 | Tests de manejo de excepciones |

**Tests de Regresión Críticos:**

```java
// Verificar que IDs son Integer
@Test
void apiContract_shouldUseIntegerIds() {
    ResponseEntity<UsuarioResponse> response = restTemplate.getForEntity(
        "/api/v1/usuarios/{id}",
        UsuarioResponse.class,
        1
    );
    // Verify Integer format in response
}

// Verificar que JSON files no se usan
@Test
void startup_shouldNotRequireJsonFiles() {
    // Delete users.json and orders.json
    // Start application
    // Assert no errors
}

// Verificar filtro soft-delete en todas las queries
@Test
void findAll_shouldNeverReturnInactiveRecords() {
    // Create active and inactive records
    // Query via all public methods
    // Assert inactive never returned
}
```

---

## Appendix: Traceability Matrix

| Acceptance Criteria | Gherkin Scenarios |
|--------------------|-------------------|
| AC-01 | DT-Usuario-R1, VP1, VP2, VP3 |
| AC-02 | DT-Query-R1, DT-Query-R2, DT-Query-R3 |
| AC-03 | DT-Delete-R1 |
| AC-04 | Crear orden con ID generado |
| AC-05 | DT-Config-R1 |
| AC-06 | Datos persisten tras restart |
| AC-07 | IP7, DT-Usuario-R3 |
| AC-08 | DT-Delete-R2, Consulta por ID soft-deleted |
| AC-09 | DT-Config-R2, DT-Config-R3 |
| FR-01 | DDL auto crea tabla usuarios/orders |
| FR-05 | Constraint UNIQUE en mail |
| NFR-01 | Performance scenarios |
