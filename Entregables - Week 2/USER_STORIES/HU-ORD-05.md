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

## Story ID: HU-ORD-05  
## Story Title  
Creación de nuevos pedidos con persistencia en PostgreSQL

### Role  
Product Owner

### Objective  
Registrar nuevos pedidos en el sistema.

### Benefit  
Garantizar que cada pedido creado se persiste correctamente en PostgreSQL para su posterior gestión.

### Detailed Description  
Como Product Owner quiero crear pedidos nuevos y asegurar que cada entidad se almacena correctamente en PostgreSQL con todos sus atributos.

---

### 🔹 Functional Requirements

- **FR-ORD-05-01**: El sistema debe insertar el nuevo pedido en la tabla `orders` de PostgreSQL.
- **FR-ORD-05-02**: Los campos requeridos son: name, description, idUser.
- **FR-ORD-05-03**: El sistema debe asignar automáticamente: id (autogenerado), state (PENDING por defecto), active (true).
- **FR-ORD-05-04**: El endpoint `POST /order/add` debe retornar HTTP 200 OK con el pedido creado.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-05-01 (Data Integrity)**: La inserción debe ser transaccional (commit/rollback).
- **NFR-ORD-05-02 (Performance)**: La creación debe completarse en menos de 100ms.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Creación exitosa de pedido**
- **Dado** que envío datos válidos: name, description, idUser
- **Cuando** invoco `POST /order/add`
- **Entonces** recibo HTTP 200 OK con el pedido creado incluyendo ID asignado

#### Negative Scenarios (Non-Acceptance)

**CA-02: Campos requeridos faltantes**
- **Dado** que envío un body sin el campo `name`
- **Cuando** invoco `POST /order/add`
- **Entonces** recibo HTTP 400 Bad Request

**CA-03: Tipo de dato inválido**
- **Dado** que envío `idUser` como texto en lugar de número
- **Cuando** invoco `POST /order/add`
- **Entonces** recibo HTTP 400 Bad Request

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `POST /order/add`
- **Tabla**: `orders`
- **Valores por defecto**: 
  - `id`: Auto-generado (SERIAL)
  - `state`: PENDING
  - `active`: true
