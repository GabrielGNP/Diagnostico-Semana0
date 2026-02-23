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

## Story ID: HU-USR-04
## Story Title
Actualizacion completa de un usuario existente

### Role
Product Owner

### Objective
Verificar que reemplazar un usuario actualiza su registro en PostgreSQL.

### Benefit
Aprobar cambios masivos sabiendo que la base refleja el estado actualizado sin residuos del estado anterior.

### Detailed Description
Como Product Owner quiero realizar una actualizacion completa de un usuario especifico para que todos los valores persistidos en PostgreSQL se reemplacen correctamente. Los requisitos de datos concretos los definira el arquitecto.

---

### 🔹 Functional Requirements
- FR-04: El endpoint PUT /v1/usuarios/{id} debe reemplazar completamente el registro en PostgreSQL si existe, o responder 404 sin impacto si no.

### 🔹 Non-Functional Requirements
- Rendimiento: respuestas en <= 500 ms bajo carga normal.
- Escalabilidad: actualizaciones consistentes con crecimiento de la tabla.
- Seguridad: solo usuarios autorizados pueden actualizar.
- Integridad de datos: commit o rollback completo; sin dejar campos obsoletos.
- Observabilidad: logs por actualizacion con confirmacion de PostgreSQL.
- Mantenibilidad: reglas de campos documentadas por el arquitecto.

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)
- Dado que el usuario existe y envio datos validos, cuando llamo PUT /v1/usuarios/{id}, entonces recibo 200 OK y la fila en PostgreSQL contiene exactamente los nuevos valores.

#### Negative Scenarios (Non-Acceptance)
- Usuario inexistente: responder 404 y PostgreSQL permanece sin cambios.
- Validacion fallida o formato invalido: 4xx sin modificar la fila.
- Acceso no autorizado: 401/403 sin impacto.
- Timeout: si PostgreSQL no responde, 503 sin aplicar cambios.

---

## 3️⃣ NOTES
- Base de datos: PostgreSQL.
- Endpoint: PUT /v1/usuarios/{id}.
- Tabla: usuarios (se reemplaza completamente la fila activa; soft-deletes excluidos de actualizacion).
- Flujo base: validar payload completo, verificar existencia, reemplazar transaccionalmente, auditar.
- Dependencias: Spring Boot/Spring Data, autenticacion/autorizacion, reglas del arquitecto.
- Supuestos: PostgreSQL como fuente de verdad; modelo soporta soft-delete pero no debe quedar estado previo.
- Open questions: definicion exacta de campos obligatorios y reglas de unicidad para PUT.
