# 1️⃣ EPIC

## Epic Title  
REST API Compliance and Error Handling Standardization for pedido-service

## Epic Purpose  
Bring pedido-service API to industry-standard REST compliance by implementing centralized error handling, proper HTTP status codes, and idiomatic path conventions.

## Business Objective  
Ensure consistent, predictable API behavior for consumers, reducing integration friction and improving fault diagnosis through standardized error responses.

## Success Metrics  
- All creation endpoints return `201 Created` with `Location` header
- All deletion endpoints return `204 No Content`
- 100% of error responses follow consistent JSON format
- Zero `System.err` calls in controller layer

---

# 2️⃣ USER STORY

## Story ID: HU-ORD-08  
## Story Title  
Centralized Exception Handling with Standard HTTP Response Codes

### Role  
API Consumer / Integrator

### Objective  
Receive predictable, structured error responses and correct HTTP status codes from pedido-service endpoints.

### Benefit  
Simplify client-side error handling logic and correctly interpret API outcomes (creation vs retrieval vs deletion).

### Detailed Description  
As an API consumer, I need pedido-service to return industry-standard HTTP status codes:
- `201 Created` with `Location` header for resource creation (`POST /order/add`)
- `204 No Content` for successful deletions (`DELETE /order/{id}`)
- Structured error responses via `@ControllerAdvice` instead of raw exceptions

Currently, the service returns `200 OK` for all successful operations, uses `System.err` for logging, and lacks a global exception handler.

---

### 🔹 Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-ORD-08-01 | Create `GlobalExceptionHandler` class annotated with `@RestControllerAdvice` in pedido-service |
| FR-ORD-08-02 | Map `OrderNotFoundException` (or equivalent) to HTTP `404 Not Found` with structured JSON body |
| FR-ORD-08-03 | Map `IllegalArgumentException` to HTTP `400 Bad Request` with structured JSON body |
| FR-ORD-08-04 | Map generic `Exception` to HTTP `500 Internal Server Error` with generic message (no stack traces exposed) |
| FR-ORD-08-05 | `POST /order/add` must return `201 Created` with `Location: /order/{id}` header |
| FR-ORD-08-06 | `DELETE /order/{id}` must return `204 No Content` on success |
| FR-ORD-08-07 | Remove all `System.err` and `System.out` calls from `OrderController`; use SLF4J logger |
| FR-ORD-08-08 | Error response body must include at minimum: `timestamp`, `status`, `error`, `message`, `path` |

---

### 🔹 Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-ORD-08-01 | Consistency | All error responses across all endpoints must follow identical JSON structure |
| NFR-ORD-08-02 | Security | Stack traces and internal details must never be exposed to clients |
| NFR-ORD-08-03 | Observability | All handled exceptions must be logged at appropriate level (WARN for 4xx, ERROR for 5xx) |
| NFR-ORD-08-04 | Performance | Exception handling overhead must not exceed 5ms per request |

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (GIVEN / WHEN / THEN)

**CA-01: Order creation returns 201 Created**
- **Given** a valid order payload
- **When** I send `POST /order/add`
- **Then** I receive HTTP `201 Created` with `Location` header pointing to `/order/{id}`

**CA-02: Order deletion returns 204 No Content**
- **Given** an existing order with ID 1
- **When** I send `DELETE /order/1`
- **Then** I receive HTTP `204 No Content` with empty body

**CA-03: Structured error response for not found**
- **Given** no order exists with ID 999
- **When** I send `GET /order/999`
- **Then** I receive HTTP `404 Not Found` with JSON body containing `timestamp`, `status`, `error`, `message`, `path`

#### Negative Scenarios

**CA-04: Invalid request body returns 400**
- **Given** an order payload missing required `name` field
- **When** I send `POST /order/add`
- **Then** I receive HTTP `400 Bad Request` with validation details in response body

**CA-05: Database connection failure returns 500**
- **Given** PostgreSQL is unavailable
- **When** I send any order endpoint request
- **Then** I receive HTTP `500 Internal Server Error` with generic message (no stack trace)

**CA-06: Deleting non-existent order returns 404**
- **Given** no order exists with ID 999
- **When** I send `DELETE /order/999`
- **Then** I receive HTTP `404 Not Found` with structured error response

---

# 3️⃣ INVEST VALIDATION

| Criterion | Status | Justification |
|-----------|--------|---------------|
| Independent | ✔ | Can be implemented without other story dependencies; only touches pedido-service internal structure |
| Negotiable | ✔ | Error format details and specific mappings can be adjusted during implementation |
| Valuable | ✔ | Directly improves API consumer experience and debugging capability |
| Estimable | ✔ | Scope is clear: 1 handler class, update 2 controller methods, standardize responses |
| Small | ✔ | Focused on exception handling and status codes only; no path renaming |
| Testable | ✔ | Each acceptance criterion is verifiable via HTTP response assertions |

---

# 4️⃣ ASSUMPTIONS

- `OrderNotFoundException` custom exception will be created if not existing
- Error response format will mirror `usuario-service`'s `ErrorResponse` structure for consistency
- SLF4J is already available as dependency via Spring Boot starter

---

# 5️⃣ CONSTRAINTS

- Must not break existing RabbitMQ messaging contracts
- Must preserve existing entity/DTO structures
- Must pass all existing unit and integration tests

---

# 6️⃣ DEPENDENCIES

| Type | Description |
|------|-------------|
| Internal | None - standalone story |
| Recommended Order | Implement before HU-ORD-09 (path standardization) |

---

# 7️⃣ TECHNICAL NOTES

**Reference Implementation:** See `usuario-service/src/main/java/com/example/usuarioservice/exception/GlobalExceptionHandler.java`

**Error Response Structure:**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order with id 999 not found",
  "path": "/order/999"
}
```
