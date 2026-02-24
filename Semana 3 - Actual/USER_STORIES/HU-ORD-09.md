# 1️⃣ EPIC

## Epic Title  
REST API Compliance and Error Handling Standardization for pedido-service

## Epic Purpose  
Bring pedido-service API to industry-standard REST compliance by implementing centralized error handling, proper HTTP status codes, and idiomatic path conventions.

## Business Objective  
Ensure consistent, predictable API behavior for consumers, reducing integration friction and improving fault diagnosis through standardized error responses.

## Success Metrics  
- All endpoints use plural resource names (`/orders` instead of `/order`)
- No action verbs in URL paths (`/add`, `/all` removed)
- Frontend successfully integrated with new paths

---

# 2️⃣ USER STORY

## Story ID: HU-ORD-09  
## Story Title  
RESTful Path Naming Standardization

### Role  
API Consumer / Integrator

### Objective  
Use intuitive, resource-oriented URL paths that follow REST conventions.

### Benefit  
Reduce cognitive load for integrators by using predictable, industry-standard naming patterns.

### Detailed Description  
Current pedido-service endpoints use non-RESTful conventions:
- `POST /order/add` should be `POST /orders`
- `GET /order/all` should be `GET /orders`
- `GET /order/user/{idUser}` should be `GET /orders?userId={id}`

This story standardizes paths to plural nouns without action verbs, following REST best practices.

---

### 🔹 Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-ORD-09-01 | Change `POST /order/add` to `POST /orders` |
| FR-ORD-09-02 | Change `GET /order/all` to `GET /orders` |
| FR-ORD-09-03 | Change `GET /order/{id}` to `GET /orders/{id}` |
| FR-ORD-09-04 | Change `DELETE /order/{id}` to `DELETE /orders/{id}` |
| FR-ORD-09-05 | Change `PATCH /order/{id}` to `PATCH /orders/{id}` |
| FR-ORD-09-06 | Change `GET /order/user/{idUser}` to `GET /orders?userId={idUser}` |
| FR-ORD-09-07 | Change `GET /order/{id}/with-user-info` to `GET /orders/{id}?expand=user` |
| FR-ORD-09-08 | Update Frontend API client `pedidoService.ts` to use new paths |

---

### 🔹 Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-ORD-09-01 | Documentation | OpenAPI/Swagger documentation must reflect new paths |
| NFR-ORD-09-02 | Backwards Compatibility | (Optional) Consider maintaining old endpoints as deprecated for one release cycle |

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (GIVEN / WHEN / THEN)

**CA-01: GET /orders returns list**
- **Given** orders exist in PostgreSQL
- **When** I send `GET /orders`
- **Then** I receive HTTP `200 OK` with all orders array

**CA-02: POST /orders creates order**
- **Given** a valid order payload
- **When** I send `POST /orders`
- **Then** I receive HTTP `201 Created` with `Location: /orders/{id}` header

**CA-03: GET /orders with userId filter**
- **Given** user 1 has 2 orders
- **When** I send `GET /orders?userId=1`
- **Then** I receive HTTP `200 OK` with exactly 2 orders

**CA-04: GET /orders/{id} with user expansion**
- **Given** order 1 exists with user 1
- **When** I send `GET /orders/1?expand=user`
- **Then** I receive HTTP `200 OK` with order data including user details

**CA-05: Frontend integration works**
- **Given** frontend is built with updated `pedidoService.ts`
- **When** dashboard loads orders
- **Then** orders display correctly from new `/orders` endpoint

#### Negative Scenarios

**CA-06: Invalid query parameter ignored gracefully**
- **Given** I send `GET /orders?invalidParam=xyz`
- **When** the request reaches the server
- **Then** I receive HTTP `200 OK` with all orders (parameter ignored)

**CA-07: Old endpoints return 404 (if backwards compatibility disabled)**
- **Given** backwards compatibility is disabled
- **When** I send `GET /order/all`
- **Then** I receive HTTP `404 Not Found`

---

# 3️⃣ INVEST VALIDATION

| Criterion | Status | Justification |
|-----------|--------|---------------|
| Independent | ✔ | Can be done independently from HU-ORD-08 (though recommended after) |
| Negotiable | ✔ | Specific path formats and backwards compatibility are adjustable |
| Valuable | ✔ | Improves API discoverability and standards compliance |
| Estimable | ✔ | Clear scope: rename paths in controller + update frontend client |
| Small | ✔ | Focused only on URL paths, no business logic changes |
| Testable | ✔ | Each endpoint is verifiable via HTTP request |

---

# 4️⃣ ASSUMPTIONS

- Backwards compatibility is NOT required (negotiable)
- Frontend deployment can be synchronized with backend changes
- Query parameter `expand=user` will trigger user info enrichment

---

# 5️⃣ CONSTRAINTS

- Must maintain existing request/response body contracts
- Must update E2E tests in `tests/e2e/` folder
- Must update any API documentation

---

# 6️⃣ DEPENDENCIES

| Type | Description |
|------|-------------|
| Recommended | HU-ORD-08 (exception handling) implemented first |
| Required | Frontend `pedidoService.ts` update |
| Required | E2E test updates |

---

# 7️⃣ TECHNICAL NOTES

**Path Mapping Changes:**

| Current Path | New Path |
|--------------|----------|
| `POST /order/add` | `POST /orders` |
| `GET /order/all` | `GET /orders` |
| `GET /order/{id}` | `GET /orders/{id}` |
| `DELETE /order/{id}` | `DELETE /orders/{id}` |
| `PATCH /order/{id}` | `PATCH /orders/{id}` |
| `GET /order/user/{idUser}` | `GET /orders?userId={idUser}` |
| `GET /order/{id}/with-user-info` | `GET /orders/{id}?expand=user` |

**Frontend Files to Update:**
- `Frontend/src/services/pedidoService.ts`

**Test Files to Update:**
- `tests/e2e/services.test.js`
