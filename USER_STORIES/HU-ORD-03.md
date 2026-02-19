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

## Story ID: HU-ORD-03  
## Story Title  
Listado de pedidos por usuario

### Role  
Product Owner

### Objective  
Consultar todos los pedidos asociados a un usuario específico.

### Benefit  
Facilitar el seguimiento del historial de pedidos de un cliente para análisis de comportamiento y soporte.

### Detailed Description  
Como Product Owner quiero obtener todos los pedidos de un usuario específico para validar su historial de compras almacenado en PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-03-01**: El sistema debe filtrar pedidos por el campo `idUser` en PostgreSQL.
- **FR-ORD-03-02**: Retornar lista de pedidos asociados al usuario consultado.
- **FR-ORD-03-03**: El endpoint `GET /order/user/{idUser}` debe retornar HTTP 200 OK.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-03-01 (Performance)**: La consulta debe responder en menos de 100ms.
- **NFR-ORD-03-02 (Scalability)**: El campo `idUser` debe estar indexado para consultas eficientes.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Usuario con pedidos**
- **Dado** que el usuario con ID = 10 tiene 3 pedidos en PostgreSQL
- **Cuando** invoco `GET /order/user/10`
- **Entonces** recibo HTTP 200 OK con lista de 3 pedidos

**CA-02: Usuario sin pedidos**
- **Dado** que el usuario con ID = 20 no tiene pedidos
- **Cuando** invoco `GET /order/user/20`
- **Entonces** recibo HTTP 200 OK con lista vacía `[]`

#### Negative Scenarios (Non-Acceptance)

**CA-03: ID de usuario inválido**
- **Dado** que envío un ID de usuario no numérico
- **Cuando** invoco `GET /order/user/xyz`
- **Entonces** recibo HTTP 400 Bad Request

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `GET /order/user/{idUser}`
- **Tabla**: `orders`
- **Índice recomendado**: INDEX en columna `id_user`
