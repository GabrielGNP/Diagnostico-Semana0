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

## Story ID: HU-ORD-01  
## Story Title  
Listado completo de pedidos almacenados en PostgreSQL

### Role  
Product Owner

### Objective  
Consultar todos los pedidos registrados en el sistema.

### Benefit  
Validar que el catálogo de pedidos visible en la aplicación refleja fielmente los registros persistidos en PostgreSQL.

### Detailed Description  
Como Product Owner quiero obtener la lista completa de pedidos para verificar que la información mostrada coincide con lo almacenado en la base de datos PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-01-01**: El sistema debe recuperar todos los pedidos de la tabla `orders` en PostgreSQL.
- **FR-ORD-01-02**: La respuesta debe incluir los campos: id, name, description, idUser, state, active.
- **FR-ORD-01-03**: El endpoint `GET /order/all` debe retornar código HTTP 200 OK.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-01-01 (Performance)**: La consulta debe responder en menos de 200ms para hasta 1000 registros.
- **NFR-ORD-01-02 (Scalability)**: La consulta debe soportar paginación futura sin cambios de contrato.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Listado exitoso con pedidos existentes**
- **Dado** que existen pedidos registrados en PostgreSQL
- **Cuando** invoco el endpoint `GET /order/all`
- **Entonces** recibo HTTP 200 OK con la lista completa de pedidos

**CA-02: Listado vacío**
- **Dado** que no existen pedidos en PostgreSQL
- **Cuando** invoco el endpoint `GET /order/all`
- **Entonces** recibo HTTP 200 OK con una lista vacía `[]`

#### Negative Scenarios (Non-Acceptance)

**CA-03: Error de conexión a base de datos**
- **Dado** que la conexión a PostgreSQL no está disponible
- **Cuando** invoco el endpoint `GET /order/all`
- **Entonces** recibo HTTP 500 Internal Server Error con mensaje descriptivo

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `GET /order/all`
- **Tabla**: `orders`
