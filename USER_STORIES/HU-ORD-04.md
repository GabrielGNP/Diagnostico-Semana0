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

## Story ID: HU-ORD-04  
## Story Title  
Consulta de pedido con información enriquecida del usuario

### Role  
Product Owner

### Objective  
Obtener un pedido con los datos completos del usuario asociado.

### Benefit  
Visualizar información consolidada de pedido y cliente en una sola consulta para facilitar decisiones de negocio.

### Detailed Description  
Como Product Owner quiero consultar un pedido incluyendo la información del usuario propietario, obtenida mediante comunicación asíncrona con el microservicio `usuario-service` vía RabbitMQ, garantizando que los datos del pedido provienen de PostgreSQL.

---

### 🔹 Functional Requirements

- **FR-ORD-04-01**: El sistema debe consultar el pedido por ID en PostgreSQL.
- **FR-ORD-04-02**: El sistema debe solicitar información del usuario via RabbitMQ al `usuario-service`.
- **FR-ORD-04-03**: La respuesta debe combinar datos del pedido (PostgreSQL) con datos del usuario (RabbitMQ).
- **FR-ORD-04-04**: El timeout máximo de espera por respuesta de usuario es 3000ms.

---

### 🔹 Non-Functional Requirements

- **NFR-ORD-04-01 (Performance)**: La respuesta total debe completarse en menos de 3500ms.
- **NFR-ORD-04-02 (Resilience)**: Si el servicio de usuarios no responde, manejar gracefully el timeout.

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)

**CA-01: Pedido con información de usuario exitosa**
- **Dado** que existe un pedido con ID = 5 y el usuario asociado está disponible
- **Cuando** invoco `GET /order/5/with-user-info`
- **Entonces** recibo HTTP 200 OK con datos del pedido y del usuario

#### Negative Scenarios (Non-Acceptance)

**CA-02: Pedido no encontrado**
- **Dado** que no existe el pedido con ID = 999
- **Cuando** invoco `GET /order/999/with-user-info`
- **Entonces** recibo HTTP 404 Not Found

**CA-03: Timeout en servicio de usuarios**
- **Dado** que el servicio `usuario-service` no responde en 3000ms
- **Cuando** invoco `GET /order/5/with-user-info`
- **Entonces** recibo HTTP 500 Internal Server Error con mensaje descriptivo

---

## 3️⃣ NOTES

- **Base de datos**: PostgreSQL
- **Endpoint**: `GET /order/{id}/with-user-info`
- **Tabla**: `orders`
- **Integración**: RabbitMQ con `usuario-service`
- **Timeout**: 3000ms (constante `USER_REQUEST_TIMEOUT` en `OrderService`)
