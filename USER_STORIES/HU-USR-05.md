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

## Story ID: HU-USR-05
## Story Title
Actualizacion parcial de datos de usuario

### Role
Product Owner

### Objective
Poder modificar atributos especificos sin afectar el resto del registro.

### Benefit
Mantener precision en datos criticos mientras actualizo solo lo necesario, con la certeza de que PostgreSQL persiste los cambios parciales correctamente.

### Detailed Description
Como Product Owner quiero aplicar cambios parciales a un usuario para garantizar que solo los campos modificados se actualicen en PostgreSQL. Los requisitos de datos concretos los definira el arquitecto.

---

### 🔹 Functional Requirements
- FR-05: El endpoint PATCH /v1/usuarios/{id} debe aplicar unicamente los campos enviados y dejar intactos los demas, marcando error si el usuario no existe.

### 🔹 Non-Functional Requirements
- Rendimiento: respuestas en <= 500 ms bajo carga normal.
- Escalabilidad: actualizaciones parciales deben mantener consistencia con crecimiento de la tabla.
- Seguridad: solo usuarios autorizados pueden modificar.
- Integridad de datos: commit o rollback completo; sin alterar campos no enviados.
- Observabilidad: logs por actualizacion parcial confirmando cambios aplicados.
- Mantenibilidad: reglas de campos y validaciones definidas por el arquitecto.

### 🔹 Acceptance Criteria

#### Positive Scenarios (Acceptance)
- Dado que el usuario existe y los campos enviados son validos, cuando llamo PATCH /v1/usuarios/{id}, entonces recibo 200 OK y PostgreSQL refleja solo esos valores nuevos sin alterar otros.

#### Negative Scenarios (Non-Acceptance)
- Usuario inexistente: responder 404 y no cambiar PostgreSQL.
- Payload invalido o campos vacios: 4xx y la fila permanece intacta.
- Acceso no autorizado: 401/403 sin impacto.
- Timeout: si PostgreSQL no responde, 503 sin aplicar cambios.

---

## 3️⃣ NOTES
- Base de datos: PostgreSQL.
- Endpoint: PATCH /v1/usuarios/{id}.
- Tabla: usuarios (se actualizan solo campos enviados; soft-deletes excluidos de modificacion).
- Flujo base: validar campos parciales, verificar existencia, aplicar cambios transaccionales, auditar.
- Dependencias: Spring Boot/Spring Data, autenticacion/autorizacion, reglas del arquitecto.
- Supuestos: PostgreSQL como fuente de verdad; modelo soporta soft-delete.
- Open questions: listas exactas de campos permitidos y reglas de validacion parcial.
