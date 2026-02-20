# 1️⃣ EPIC

## Epic Title
Gestion completa de usuarios con persistencia en PostgreSQL

## Epic Purpose
Capacitar al backend para gestionar usuarios (listado, consulta, creacion, actualizacion y eliminacion con soft-delete) con garantias de consistencia y trazabilidad directa en PostgreSQL, de modo que cada interaccion refleje fielmente el estado de la base de datos.

## Business Objective
Reducir la brecha entre la API REST de usuarios y la fuente de verdad en PostgreSQL para que los Product Owners confien en la integridad de los datos del catalogo de usuarios.

## Success Metrics
- 100% de los endpoints de usuario reflejan transacciones exitosas en PostgreSQL segun auditorias.
- Cero discrepancias entre respuestas API y registros en PostgreSQL.

---

# 2️⃣ USER STORY

## Story ID: HU-USR-01
## Story Title
Listado completo de usuarios registrados en PostgreSQL

### Role
Product Owner

### Objective
Verificar que puedo consultar todos los usuarios almacenados (activos o marcados).

### Benefit
Asegurarme de que el catalogo visible refleja la lista real registrada en la base de datos sin saltos ni datos desactualizados.

### Detailed Description
Como Product Owner quiero obtener la lista completa de usuarios para confirmar que la informacion mostrada coincide con lo persistido en PostgreSQL. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definira el arquitecto.

---

### 🔹 Functional Requirements
- FR-01: El endpoint GET /v1/usuarios debe devolver el conjunto completo de usuarios activos en PostgreSQL, omitiendo soft-deletes.

### 🔹 Non-Functional Requirements
- Rendimiento: respuestas en <= 500 ms bajo carga normal.
- Escalabilidad: la tabla de usuarios debe poder crecer sin degradar consistencia ni el control de soft-delete.
- Seguridad: solo usuarios autorizados acceden a los endpoints.
- Integridad de datos: transacciones completas sin estados intermedios.
- Observabilidad: logs por operacion indicando confirmacion de PostgreSQL.
- Mantenibilidad: campos y reglas de soft-delete documentados por el arquitecto.

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)
- Dado que existen usuarios activos, cuando llamo GET /v1/usuarios, entonces recibo 200 OK con exactamente las filas activas (sin soft-deletes).
- Dado que no hay usuarios activos, cuando llamo GET /v1/usuarios, entonces recibo 200 OK con coleccion vacia y la tabla no contiene filas activas.

#### Negative Scenarios (Non-Acceptance)
- Acceso no autorizado: sin credenciales validas retorna 401/403 sin cambios en PostgreSQL.
- Timeout: si PostgreSQL no responde en la ventana esperada, retornar 503 sin alterar datos.
- Formato invalido: peticiones mal formadas responden 400 sin afectar la base.

---

## 3️⃣ NOTES
- Base de datos: PostgreSQL.
- Endpoint: GET /v1/usuarios.
- Tabla: usuarios (activos); los soft-deleted se omiten en listados activos.
- Flujo base: el backend valida, consulta PostgreSQL, retorna coleccion consistente y registra auditoria.
- Dependencias: Spring Boot/Spring Data, autenticacion/autorizacion, reglas definidas por el arquitecto.
- Supuestos: PostgreSQL es la fuente de verdad; el modelo soporta soft-delete.
- Open questions: definir campos obligatorios y reglas de unicidad; confirmar si se requiere endpoint administrativo para soft-deleted.
