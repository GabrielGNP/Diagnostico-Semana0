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
Currently in usuario-service:
- `@RequestMapping` is set to `/v1/usuarios`
- `API_PATH` constant used in logs is `/api/v1/usuarios`
- Context path in `application.properties` is `/api`
- Actual full path is `/api/v1/usuarios`

The `API_PATH` constant should match the relative path `/v1/usuarios` (without context path) or be dynamically resolved to avoid hardcoded discrepancies.

Additionally, `POST /v1/usuarios` returns `201 Created` but lacks the `Location` header pointing to the created resource.

---

### 🔹 Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-USR-07-01 | Unify `API_PATH` constant to match `@RequestMapping` value (`/v1/usuarios`) |
| FR-USR-07-02 | Alternatively, update logs to use request URI from `HttpServletRequest` instead of hardcoded constant |
| FR-USR-07-03 | `POST /v1/usuarios` must return `Location` header with value `/api/v1/usuarios/{id}` |
| FR-USR-07-04 | Audit all log statements using `API_PATH` to ensure correct path is displayed |

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
- **When** I send `POST /api/v1/usuarios`
- **Then** I receive HTTP `201 Created` with `Location: /api/v1/usuarios/{id}` header

**CA-02: Location header contains correct ID**
- **Given** I create a new user
- **When** I follow the `Location` header URL with `GET`
- **Then** I receive HTTP `200 OK` with the created user data

**CA-03: Logs show correct path**
- **Given** I send `GET /api/v1/usuarios`
- **When** request is logged by the controller
- **Then** log entry shows `/api/v1/usuarios` or `/v1/usuarios` (consistent with mapping)

#### Negative Scenarios

**CA-04: Duplicate email returns 409 without Location**
- **Given** user with email "test@test.com" already exists
- **When** I send `POST /api/v1/usuarios` with same email
- **Then** I receive HTTP `409 Conflict` without `Location` header

**CA-05: Invalid payload returns 400 without Location**
- **Given** a user payload missing required `name` field
- **When** I send `POST /api/v1/usuarios`
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

- Context path `/api` will remain unchanged
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

**Current Path Configuration:**
- `@RequestMapping("/v1/usuarios")` in controller
- `server.servlet.context-path=/api` in application.properties
- Full path: `/api/v1/usuarios`
