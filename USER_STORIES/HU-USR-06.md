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

## Story ID: HU-USR-06
## Story Title
Soft-delete seguro de un usuario

### Role
Product Owner

### Objective
Confirmar que la eliminacion de usuarios marca su registro como borrado en PostgreSQL.

### Benefit
Evitar inconsistencias entre la interfaz y la base mientras se mantiene trazabilidad legal de los usuarios eliminados.

### Detailed Description
Como Product Owner quiero eliminar un usuario y verificar que su fila queda marcada como soft-delete en PostgreSQL, de modo que desaparezca de los listados activos sin perder la referencia historica. Los requisitos de datos concretos los definira el arquitecto.

---

### 🔹 Functional Requirements
- FR-06: El endpoint DELETE /v1/usuarios/{id} debe marcar un registro existente como soft-delete en PostgreSQL o devolver 404 sin cambios si ya estaba borrado o no existe.

### 🔹 Non-Functional Requirements
- Rendimiento: respuestas en <= 500 ms bajo carga normal.
- Escalabilidad: eliminaciones deben mantener consistencia con crecimiento de la tabla.
- Seguridad: solo usuarios autorizados pueden eliminar.
- Integridad de datos: commit o rollback completo; no borrar fisicamente, solo marcar.
- Observabilidad: logs por eliminacion indicando marca de soft-delete y resultado.
- Mantenibilidad: reglas de soft-delete documentadas por el arquitecto.

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)
- Dado que el usuario existe, cuando llamo DELETE /v1/usuarios/{id}, entonces recibo 204 No Content y la fila se marca como soft-delete sin eliminarse fisicamente, y no aparece en listados activos.

#### Negative Scenarios (Non-Acceptance)
- Usuario inexistente o ya marcado: responder 404 y PostgreSQL permanece sin cambios.
- Acceso no autorizado: 401/403 sin impacto.
- Timeout: si PostgreSQL no responde, 503 sin aplicar cambios.
- Formato invalido: 400 sin alterar datos.

---

## 3️⃣ NOTES
- Base de datos: PostgreSQL.
- Endpoint: DELETE /v1/usuarios/{id}.
- Tabla: usuarios (marca de soft-delete; listados activos excluyen marcados).
- Flujo base: validar identificador, verificar existencia, marcar soft-delete transaccionalmente, auditar.
- Dependencias: Spring Boot/Spring Data, autenticacion/autorizacion, reglas del arquitecto.
- Supuestos: PostgreSQL como fuente de verdad; soft-delete reversible/auditable.
- Open questions: necesidad de endpoint administrativo para consultar registros soft-deleted.
