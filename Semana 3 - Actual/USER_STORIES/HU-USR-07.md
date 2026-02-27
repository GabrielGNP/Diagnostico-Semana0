# 1️⃣ EPIC

## Epic Title  
API Path Consistency and Response Headers for usuario-service

## Epic Purpose  
Resolve path constant discrepancies and complete REST compliance by adding Location headers to creation responses.

## Business Objective  
Ensure logging, documentation, and actual API paths are synchronized to eliminate integrator confusion.

## Success Metrics  
- Zero discrepancy between `API_PATH` constant and actual `@RequestMapping`
- All `POST` endpoints return `Location` header with created resource URI
- Logs display accurate request paths

---

# ✅ STATUS: PARTIALLY COMPLETED (2026-02-27)

## Path Consistency: ✅ RESOLVED
- **Migration completed**: Ruta simplificada de `/api/v1/usuarios` a `/users`
- `server.servlet.context-path=/api` fue **eliminado**
- `@RequestMapping("/users")` y `API_PATH = "/users"` ahora son consistentes
- Frontend actualizado en `usuarioService.ts`

## Location Header: ⏳ PENDING
- `POST /users` aún no retorna header `Location`

---

# 2️⃣ USER STORY

## Story ID: HU-USR-07  
## Story Title  
Path Constant Unification and Location Header Implementation

### Role  
API Consumer / DevOps Engineer

### Objective  
Have consistent paths across logs, documentation, and actual endpoints; receive proper `Location` headers on resource creation.

### Benefit  
Eliminate debugging confusion caused by path mismatches in logs vs actual requests; follow REST best practices for created resources.

### Detailed Description  

**BEFORE (resolved):**
- `@RequestMapping` was `/v1/usuarios`
- `API_PATH` constant was `/api/v1/usuarios`
- Context path in `application.properties` was `/api`
- Full path was `/api/v1/usuarios`

**AFTER (current state):**
- `@RequestMapping("/users")` - simplified path
- `API_PATH = "/users"` - consistent with mapping
- No context-path - direct `/users` endpoint
- Full path is now: `/users`

**Remaining work:**
`POST /users` returns `201 Created` but still lacks the `Location` header pointing to the created resource.

---

### 🔹 Functional Requirements

| ID | Requirement | Status |
|----|-------------|--------|
| FR-USR-07-01 | Unify `API_PATH` constant to match `@RequestMapping` value | ✅ DONE - Unified at `/users` |
| FR-USR-07-02 | Update logs to use consistent path | ✅ DONE - All logs use `/users` |
| FR-USR-07-03 | `POST /users` must return `Location` header with value `/users/{id}` | ⏳ PENDING |
| FR-USR-07-04 | Audit all log statements using `API_PATH` to ensure correct path | ✅ DONE |

---

### 🔹 Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-USR-07-01 | Consistency | All logs must show actual request paths matching what clients use |
| NFR-USR-07-02 | Maintainability | Consider removing hardcoded path constants in favor of dynamic path resolution |

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (GIVEN / WHEN / THEN)

**CA-01: User creation returns Location header**
- **Given** a valid user payload with name, email, password
- **When** I send `POST /users`
- **Then** I receive HTTP `201 Created` with `Location: /users/{id}` header

**CA-02: Location header contains correct ID**
- **Given** I create a new user
- **When** I follow the `Location` header URL with `GET`
- **Then** I receive HTTP `200 OK` with the created user data

**CA-03: Logs show correct path** ✅ VERIFIED
- **Given** I send `GET /users`
- **When** request is logged by the controller
- **Then** log entry shows `/users` (consistent with mapping)

#### Negative Scenarios

**CA-04: Duplicate email returns 409 without Location**
- **Given** user with email "test@test.com" already exists
- **When** I send `POST /users` with same email
- **Then** I receive HTTP `409 Conflict` without `Location` header

**CA-05: Invalid payload returns 400 without Location**
- **Given** a user payload missing required `name` field
- **When** I send `POST /users`
- **Then** I receive HTTP `400 Bad Request` without `Location` header

---

# 3️⃣ INVEST VALIDATION

| Criterion | Status | Justification |
|-----------|--------|---------------|
| Independent | ✔ | Internal change to usuario-service only; no external dependencies |
| Negotiable | ✔ | Implementation approach (constant vs dynamic resolution) is flexible |
| Valuable | ✔ | Eliminates debugging confusion and completes REST compliance |
| Estimable | ✔ | Small scope: update 1 constant or log calls, add 1 response header |
| Small | ✔ | Single-focused fix; completable in under one sprint day |
| Testable | ✔ | Verifiable via log inspection and HTTP header assertion |

---

# 4️⃣ ASSUMPTIONS

- ~~Context path `/api` will remain unchanged~~ **REMOVED** - No context-path needed
- `ServletUriComponentsBuilder` is available for dynamic URI construction
- Existing `GlobalExceptionHandler` will not interfere with `Location` header

---

# 5️⃣ CONSTRAINTS

- Must not change existing response body contracts
- Must pass all existing unit tests
- Must maintain compatibility with frontend `usuarioService.ts`

---

# 6️⃣ DEPENDENCIES

| Type | Description |
|------|-------------|
| Internal | None - standalone story |

---

# 7️⃣ TECHNICAL NOTES

**Location Header Implementation Example:**

```java
@PostMapping
public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO dto) {
    UserDTO created = userService.createUser(dto);
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.getId())
        .toUri();
    return ResponseEntity.created(location).body(created);
}
```

**Files to Review:**
- `Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java`

**Current Path Configuration (UPDATED 2026-02-27):**
- `@RequestMapping("/users")` in controller
- No context-path (removed `server.servlet.context-path`)
- Full path: `/users`
