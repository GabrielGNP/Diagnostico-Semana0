# EPIC: Migración de Persistencia a PostgreSQL

## Epic Title
Implementación de Persistencia Relacional con PostgreSQL y JPA para Microservicios

## Epic Purpose
Reemplazar la persistencia basada en archivos JSON por bases de datos PostgreSQL independientes para cada microservicio, utilizando Spring Data JPA como capa de abstracción ORM.

## Business Objective
- Garantizar integridad transaccional y consistencia de datos.
- Habilitar escalabilidad horizontal de los servicios.
- Facilitar consultas complejas y reportes.
- Cumplir con estándares de persistencia empresarial.

## Success Metrics
- 100% de operaciones CRUD migradas a JPA.
- Tiempo de respuesta de queries ≤ 100ms para operaciones simples.
- Tests de integración pasando con PostgreSQL.
- Soft-delete implementado correctamente (registros nunca eliminados físicamente).

---

# USER STORIES

---

## HU-DB-01: Configuración de Infraestructura PostgreSQL en Docker

### Role
Como **DevOps/Arquitecto**

### Objective
Quiero configurar contenedores Docker independientes con PostgreSQL para cada microservicio

### Benefit
Para que cada servicio tenga su propia base de datos aislada, facilitando el despliegue y la escalabilidad independiente.

### Detailed Description
Se requiere actualizar `docker-compose.yml` para incluir dos servicios PostgreSQL:
- `postgres-usuarios`: Base de datos para `usuario-service` (puerto 5432).
- `postgres-pedidos`: Base de datos para `pedido-service` (puerto 5433).

Cada contenedor debe tener:
- Volumen persistente para datos.
- Variables de entorno referenciadas desde archivo `.env`.
- Health checks configurados.
- Red compartida con los servicios Spring Boot.

**Archivo `.env` requerido en la raíz del proyecto** con variables:
```
POSTGRES_USER=admin
POSTGRES_PASSWORD=<secure_password>
POSTGRES_USUARIOS_DB=usuarios_db
POSTGRES_PEDIDOS_DB=pedidos_db
```

---

## HU-DB-02: Entidad JPA Usuario con UUID

### Role
Como **Desarrollador Backend**

### Objective
Quiero definir la entidad JPA `Usuario` con UUID como clave primaria

### Benefit
Para persistir y recuperar usuarios desde PostgreSQL con identificadores únicos universales.

### Detailed Description
Transformar la clase `User` actual en una entidad JPA con:
- `@Entity`, `@Table(name = "usuarios")`
- `@Id` de tipo `UUID` con `@GeneratedValue(strategy = GenerationType.UUID)`
- Columnas mapeadas: `id`, `name`, `password`, `mail`, `active`
- Restricciones: `mail` único, `name` y `password` no nulos.
- Campo `active` para soft-delete (DEFAULT true).

**Esquema de campos:**
| Campo    | Tipo    | Restricción              |
|----------|---------|--------------------------|
| id       | UUID    | PK, auto-generado        |
| name     | String  | NOT NULL, 2-50 chars     |
| password | String  | NOT NULL, min 8          |
| mail     | String  | NOT NULL, UNIQUE         |
| active   | boolean | NOT NULL, DEFAULT true   |

---

## HU-DB-03: Entidad JPA Order con UUID

### Role
Como **Desarrollador Backend**

### Objective
Quiero definir la entidad JPA `Order` con UUID como clave primaria y soporte soft-delete

### Benefit
Para persistir pedidos con identificadores únicos y eliminación lógica.

### Detailed Description
Transformar la clase `Order` actual en una entidad JPA con:
- `@Entity`, `@Table(name = "orders")`
- `@Id` de tipo `UUID` con `@GeneratedValue(strategy = GenerationType.UUID)`
- `@Enumerated(EnumType.STRING)` para el campo `state`
- Columnas: `id`, `name`, `description`, `id_user` (UUID), `state`, `active`
- Campo `active` para soft-delete.

**Esquema de campos:**
| Campo       | Tipo   | Restricción              |
|-------------|--------|--------------------------|
| id          | UUID   | PK, auto-generado        |
| name        | String | NOT NULL                 |
| description | String | nullable                 |
| idUser      | UUID   | NOT NULL                 |
| state       | State  | NOT NULL, ENUM (STRING)  |
| active      | boolean| NOT NULL, DEFAULT true   |

**Enum State:**
`PROCESSING`, `TRAVELING_TO_WAREHOUSE`, `IN_WAREHOUSE`, `TRAVELING_TO_YOUR_HOUSE`, `ON_THE_STREET`, `DELIVERED`, `CANCELED`

---

## HU-DB-04: Repositorio JPA UsuarioRepository con Soft-Delete

### Role
Como **Desarrollador Backend**

### Objective
Quiero crear una interfaz `UsuarioRepository` que soporte operaciones CRUD con soft-delete

### Benefit
Para disponer de métodos que filtren automáticamente registros inactivos.

### Detailed Description
Crear interfaz en `usuario-service`:
```java
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>
```

**Métodos requeridos:**
- CRUD heredados: `save`, `findById`, `findAll`, `existsById`
- **Soft-delete queries:**
  - `List<Usuario> findByActiveTrue()` — usuarios activos
  - `Optional<Usuario> findByIdAndActiveTrue(UUID id)` — usuario activo por ID
  - `Optional<Usuario> findByMailAndActiveTrue(String mail)` — usuario activo por email
  - `boolean existsByMailAndActiveTrue(String mail)` — verificar email único entre activos

**Nota:** NO exponer `deleteById` directamente. El borrado se realiza mediante update de `active=false`.

---

## HU-DB-05: Repositorio JPA OrderRepository con Soft-Delete

### Role
Como **Desarrollador Backend**

### Objective
Quiero crear una interfaz `OrderRepository` que soporte consultas con soft-delete

### Benefit
Para disponer de métodos CRUD que respeten la eliminación lógica.

### Detailed Description
Crear interfaz en `pedido-service`:
```java
public interface OrderRepository extends JpaRepository<Order, UUID>
```

**Métodos requeridos:**
- CRUD heredados: `save`, `findById`, `findAll`
- **Soft-delete queries:**
  - `List<Order> findByActiveTrue()` — órdenes activas
  - `Optional<Order> findByIdAndActiveTrue(UUID id)` — orden activa por ID
  - `List<Order> findByIdUserAndActiveTrue(UUID idUser)` — órdenes activas por usuario
  - `List<Order> findByStateAndActiveTrue(State state)` — órdenes activas por estado

---

## HU-DB-06: Configuración de Conexión JPA para usuario-service

### Role
Como **Desarrollador Backend**

### Objective
Quiero configurar `application.properties` con parámetros de conexión externalizados

### Benefit
Para que las credenciales no estén hardcodeadas y se lean desde variables de entorno.

### Detailed Description
Configurar en `usuario-service/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:usuarios_db}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Las variables `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT`, `DB_NAME` se inyectan desde `docker-compose.yml` o `.env`.

---

## HU-DB-07: Configuración de Conexión JPA para pedido-service

### Role
Como **Desarrollador Backend**

### Objective
Quiero configurar `application.properties` con parámetros de conexión externalizados

### Benefit
Para que las credenciales no estén hardcodeadas y se lean desde variables de entorno.

### Detailed Description
Configurar en `pedido-service/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:pedidos_db}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## HU-DB-08: Dependencias Maven para JPA y PostgreSQL

### Role
Como **Desarrollador Backend**

### Objective
Quiero agregar las dependencias necesarias en los `pom.xml` de ambos servicios

### Benefit
Para habilitar Spring Data JPA y el driver PostgreSQL en el classpath.

### Detailed Description
Agregar en ambos `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## HU-DB-09: Implementación de Soft-Delete en Servicios

### Role
Como **Desarrollador Backend**

### Objective
Quiero que los métodos de eliminación marquen registros como inactivos en lugar de borrarlos

### Benefit
Para mantener historial de datos y permitir recuperación si es necesario.

### Detailed Description
En las clases `Service` de ambos microservicios:

**Operación DELETE → Soft-Delete:**
```java
public void delete(UUID id) {
    Entity entity = repository.findByIdAndActiveTrue(id)
        .orElseThrow(() -> new EntityNotFoundException("Not found"));
    entity.setActive(false);
    repository.save(entity);
}
```

**Queries de lectura:**
- Todos los métodos `find*` deben usar variantes `*AndActiveTrue` para excluir registros eliminados.

**Opcional (avanzado):** Considerar `@Where(clause = "active = true")` a nivel de entidad para filtrado automático.

---

# PROCESS FLOW

1. **Configuración `.env`**: Crear archivo `.env` con credenciales PostgreSQL.
2. **Infraestructura**: Actualizar `docker-compose.yml` con contenedores PostgreSQL.
3. **Dependencias**: Agregar dependencias JPA/PostgreSQL en `pom.xml`.
4. **Configuración**: Configurar `application.properties` con variables externalizadas.
5. **Entidades**: Crear entidades JPA con UUID y campo `active` para soft-delete.
6. **Repositorios**: Crear interfaces `JpaRepository` con queries soft-delete.
7. **Refactor Services**: Actualizar servicios para usar repositorios JPA y soft-delete.
8. **Validación**: Ejecutar tests de integración contra PostgreSQL.

---

# FUNCTIONAL REQUIREMENTS

- **FR-01**: El sistema debe crear automáticamente las tablas en PostgreSQL al iniciar (DDL auto).
- **FR-02**: El sistema debe usar UUID como clave primaria para todas las entidades.
- **FR-03**: El sistema debe persistir usuarios con campos: id (UUID), name, password, mail, active.
- **FR-04**: El sistema debe persistir órdenes con campos: id (UUID), name, description, idUser (UUID), state, active.
- **FR-05**: El campo `mail` debe ser único a nivel de base de datos (para registros activos).
- **FR-06**: El campo `state` debe almacenarse como STRING en la columna.
- **FR-07**: Los repositorios deben implementar soft-delete (nunca DELETE físico).
- **FR-08**: Las queries de lectura deben filtrar registros con `active=true` por defecto.
- **FR-09**: Las credenciales de BD deben leerse desde variables de entorno (no hardcodeadas).
- **FR-10**: Cada microservicio debe conectar a su propia base de datos PostgreSQL.

---

# NON-FUNCTIONAL REQUIREMENTS

- **NFR-01 (Performance)**: Queries simples deben responder en ≤ 100ms.
- **NFR-02 (Availability)**: PostgreSQL debe tener health checks configurados en Docker.
- **NFR-03 (Security)**: Credenciales externalizadas en `.env` o secrets manager, nunca en código.
- **NFR-04 (Scalability)**: Cada servicio puede escalar horizontalmente sin conflicto de BD.
- **NFR-05 (Data Integrity)**: Constraints de unicidad y NOT NULL deben aplicarse a nivel de BD.
- **NFR-06 (Observability)**: `spring.jpa.show-sql=true` habilitado en desarrollo.
- **NFR-07 (Auditability)**: Soft-delete preserva historial de registros eliminados.
- **NFR-08 (Maintainability)**: DDL-auto en modo `update` para desarrollo.

---

# ACCEPTANCE CRITERIA

## Positive Scenarios (Acceptance)

**AC-01: Creación de usuario con UUID**
```
GIVEN el servicio usuario-service está corriendo con PostgreSQL
WHEN se envía POST /api/v1/usuarios con datos válidos
THEN el usuario se persiste con un UUID generado automáticamente
AND se retorna HTTP 201 con el usuario incluyendo el UUID
```

**AC-02: Consulta de usuarios activos**
```
GIVEN existen usuarios con active=true y active=false en la tabla
WHEN se envía GET /api/v1/usuarios
THEN se retornan SOLO los usuarios con active=true
```

**AC-03: Soft-delete de usuario**
```
GIVEN existe un usuario activo con UUID "abc-123"
WHEN se envía DELETE /api/v1/usuarios/abc-123
THEN el campo active se actualiza a false
AND el registro NO se elimina físicamente de la tabla
AND posteriores GET no retornan este usuario
```

**AC-04: Creación de orden con UUID**
```
GIVEN el servicio pedido-service está corriendo con PostgreSQL
WHEN se crea una orden
THEN la orden se persiste con UUID generado
AND idUser se almacena como UUID
```

**AC-05: Credenciales externalizadas**
```
GIVEN el archivo .env contiene DB_USER y DB_PASSWORD
WHEN los servicios inician via docker-compose
THEN se conectan usando las credenciales del .env
AND no hay credenciales hardcodeadas en application.properties
```

**AC-06: Persistencia sobrevive restart**
```
GIVEN se han creado usuarios y órdenes
WHEN se reinician los contenedores Docker
THEN los datos persisten gracias a los volúmenes
```

## Negative Scenarios (Non-Acceptance)

**AC-07: Email duplicado rechazado**
```
GIVEN existe un usuario activo con mail "test@mail.com"
WHEN se intenta crear otro usuario con el mismo mail
THEN la operación falla con constraint violation
AND se retorna HTTP 409 Conflict
```

**AC-08: Consulta de usuario eliminado (soft-deleted)**
```
GIVEN existe un usuario con active=false (soft-deleted)
WHEN se envía GET /api/v1/usuarios/{id}
THEN se retorna HTTP 404 Not Found
AND el registro existe en BD pero no es visible
```

**AC-09: Conexión sin credenciales falla**
```
GIVEN las variables DB_USER/DB_PASSWORD no están definidas
WHEN el servicio intenta iniciar
THEN falla con error claro de configuración
```

---

# ASSUMPTIONS

1. Se usará `spring.jpa.hibernate.ddl-auto=update` para crear/actualizar esquemas.
2. No se requiere migración de datos existentes en JSON (inicio con BD vacía).
3. Las credenciales se manejan via archivo `.env` referenciado en `docker-compose.yml`.
4. El tipo UUID nativo de PostgreSQL será usado para las claves primarias.
5. El enum `State` se mantiene sin cambios.
6. No hay FK física entre servicios (idUser es referencia lógica, no FK de BD).
7. El soft-delete se implementa via campo `active` booleano.

---

# CONSTRAINTS

- **C-01**: Cada microservicio DEBE tener su propia base de datos (no BD compartida).
- **C-02**: PostgreSQL DEBE correr en Docker.
- **C-03**: Se DEBE usar Spring Data JPA.
- **C-04**: Se DEBE usar UUID como tipo de clave primaria.
- **C-05**: Se DEBE implementar soft-delete (nunca hard-delete).
- **C-06**: Las credenciales NO deben estar hardcodeadas en el código fuente.
- **C-07**: Los archivos JSON existentes NO deben usarse como fallback.

---

# DEPENDENCIES

| Dependencia | Propósito |
|-------------|-----------|
| `spring-boot-starter-data-jpa` | ORM y repositorios JPA |
| `postgresql` (driver) | Conexión a PostgreSQL |
| Docker / Docker Compose | Orquestación de contenedores |
| PostgreSQL 15+ | Motor de base de datos (soporte UUID nativo) |
| Hibernate 6+ | Implementación JPA con soporte UUID |
| Archivo `.env` | Externalización de credenciales |
