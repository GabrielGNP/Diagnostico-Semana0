# 1️⃣ EPIC

## Epic Title  
Persistencia de pedidos en PostgreSQL para el microservicio pedido-service

## Epic Purpose  
Implementar la capa de persistencia en PostgreSQL para todas las operaciones CRUD del módulo de pedidos, garantizando integridad de datos, trazabilidad y consistencia transaccional.

## Business Objective  
Habilitar una base de datos relacional robusta (PostgreSQL) como fuente de verdad para los pedidos del sistema, permitiendo consultas eficientes, auditoría y escalabilidad del negocio.

## Success Metrics  
- 100% de los endpoints de pedidos operan correctamente contra PostgreSQL.
- Cero pérdidas de datos en operaciones transaccionales.
- Tiempo de respuesta de consultas < 200ms para operaciones estándar.

---

# 2️⃣ USER STORY

## Story ID: HU-ORD-06  
## Story Title  
Eliminación de pedidos del sistema

### Role  
Product Owner

### Objective  
Eliminar pedidos que ya no son necesarios.

### Benefit  
Mantener la base de datos limpia y cumplir con políticas de retención de datos.

### Detailed Description  
Como Product Owner quiero eliminar pedidos para gestionar el ciclo de vida de los datos en PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-06-01**: El sistema debe eliminar el registro del pedido de la tabla `orders` en PostgreSQL.
- **FR-ORD-06-02**: Si el pedido no existe, retornar HTTP 404 Not Found (o HTTP 200 si se prefiere idempotencia).
- **FR-ORD-06-03**: El endpoint `DELETE /order/{id}` debe retornar HTTP 200 OK si la eliminación es exitosa.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-06-01 (Data Integrity)**: La eliminación debe ser transaccional.
- **NFR-ORD-06-02 (Auditability)**: Considerar logging de eliminaciones para trazabilidad.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Eliminación exitosa**
- **Dado** que existe un pedido con ID = 5
- **Cuando** invoco `DELETE /order/5`
- **Entonces** recibo HTTP 200 OK y el pedido ya no existe en PostgreSQL

#### Negative Scenarios (Non-Acceptance)

**CA-02: Pedido no encontrado**
- **Dado** que no existe un pedido con ID = 999
- **Cuando** invoco `DELETE /order/999`
- **Entonces** recibo HTTP 404 Not Found (o HTTP 200 si idempotente)

**CA-03: ID inválido**
- **Dado** que envío un ID no numérico
- **Cuando** invoco `DELETE /order/abc`
- **Entonces** recibo HTTP 400 Bad Request

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `DELETE /order/{id}`
- **Tabla**: `orders`
- **Consideración**: Evaluar si se requiere soft-delete (actualizar campo `active` a false) en lugar de hard-delete
