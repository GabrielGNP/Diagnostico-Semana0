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

## Story ID: HU-ORD-02  
## Story Title  
Consulta de pedido específico por identificador

### Role  
Product Owner

### Objective  
Recuperar un pedido concreto mediante su ID único.

### Benefit  
Validar casos de soporte o auditoría con la certeza de que los datos obtenidos reflejan el estado real en PostgreSQL.

### Detailed Description  
Como Product Owner quiero consultar un pedido por su ID para confirmar que los datos retornados coinciden con el registro correspondiente en PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-02-01**: El sistema debe buscar el pedido por ID en la tabla `orders` de PostgreSQL.
- **FR-ORD-02-02**: Si existe, retornar el pedido completo con todos sus campos.
- **FR-ORD-02-03**: Si no existe, retornar HTTP 404 Not Found.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-02-01 (Performance)**: La consulta por ID debe responder en menos de 50ms.
- **NFR-ORD-02-02 (Data Integrity)**: El ID debe ser único e indexado en PostgreSQL.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Consulta exitosa de pedido existente**
- **Dado** que existe un pedido con ID = 5 en PostgreSQL
- **Cuando** invoco `GET /order/5`
- **Entonces** recibo HTTP 200 OK con los datos completos del pedido

#### Negative Scenarios (Non-Acceptance)

**CA-02: Pedido no encontrado**
- **Dado** que no existe un pedido con ID = 999 en PostgreSQL
- **Cuando** invoco `GET /order/999`
- **Entonces** recibo HTTP 404 Not Found

**CA-03: ID con formato inválido**
- **Dado** que envío un ID no numérico
- **Cuando** invoco `GET /order/abc`
- **Entonces** recibo HTTP 400 Bad Request

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `GET /order/{id}`
- **Tabla**: `orders`
- **Índice requerido**: PRIMARY KEY en columna `id`
