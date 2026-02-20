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

## Story ID: HU-ORD-07  
## Story Title  
Cambio de estado de un pedido

### Role  
Product Owner

### Objective  
Actualizar el estado de un pedido existente.

### Benefit  
Gestionar el flujo de vida de los pedidos (PENDING → PROCESSING → COMPLETED → CANCELLED) con trazabilidad en PostgreSQL.

### Detailed Description  
Como Product Owner quiero cambiar el estado de un pedido para reflejar su progreso en el proceso de negocio, asegurando que el cambio se persista correctamente en PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-07-01**: El sistema debe actualizar el campo `state` del pedido en PostgreSQL.
- **FR-ORD-07-02**: Los estados válidos son los definidos en el enum `State`.
- **FR-ORD-07-03**: Si el pedido no existe, retornar HTTP 404 Not Found.
- **FR-ORD-07-04**: Si no se envía el campo `state`, retornar HTTP 400 Bad Request.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-07-01 (Data Integrity)**: La actualización debe ser transaccional.
- **NFR-ORD-07-02 (Auditability)**: Considerar registro del cambio de estado para trazabilidad futura.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Cambio de estado exitoso**
- **Dado** que existe un pedido con ID = 5 en estado PENDING
- **Cuando** invoco `PATCH /order/5` con body `{"state": "PROCESSING"}`
- **Entonces** recibo HTTP 200 OK con el pedido actualizado y el estado es PROCESSING en PostgreSQL

#### Negative Scenarios (Non-Acceptance)

**CA-02: Pedido no encontrado**
- **Dado** que no existe el pedido con ID = 999
- **Cuando** invoco `PATCH /order/999`
- **Entonces** recibo HTTP 404 Not Found

**CA-03: Campo state no enviado**
- **Dado** que envío un body sin el campo `state`
- **Cuando** invoco `PATCH /order/5`
- **Entonces** recibo HTTP 400 Bad Request

**CA-04: Estado inválido**
- **Dado** que envío un estado que no existe en el enum
- **Cuando** invoco `PATCH /order/5` con body `{"state": "INVALID_STATE"}`
- **Entonces** recibo HTTP 400 Bad Request

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `PATCH /order/{id}`
- **Tabla**: `orders`
- **Estados válidos**: Definidos en enum `State` (PENDING, PROCESSING, COMPLETED, CANCELLED, etc.)
