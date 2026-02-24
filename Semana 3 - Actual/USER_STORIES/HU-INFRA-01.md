# 1️⃣ EPIC

## Epic Title  
Docker Configuration Standardization

## Epic Purpose  
Fix environment variable inconsistencies and standardize Dockerfile configurations across services.

## Business Objective  
Ensure reliable container deployments with consistent, documented configuration patterns.

## Success Metrics  
- All services connect successfully to dependencies via Docker Compose
- All Dockerfiles follow same optimization patterns (JRE vs JDK)
- Zero configuration-related deployment failures

---

# 2️⃣ USER STORY

## Story ID: HU-INFRA-01  
## Story Title  
RabbitMQ Environment Variable and Dockerfile Consistency

### Role  
DevOps Engineer

### Objective  
Have all services correctly resolve environment variables and use optimized base images.

### Benefit  
Prevent runtime connection failures and reduce container image sizes for faster deployments.

### Detailed Description  
Current infrastructure issues identified:

1. **RabbitMQ Variable Mismatch:**
   - `usuario-service` application.properties expects `RABBITMQ_HOST`
   - `docker-compose.yml` provides `SPRING_RABBITMQ_HOST`
   - This may cause connection failures or fallback to localhost

2. **Dockerfile Base Image Inconsistency:**
   - `pedido-service` uses `eclipse-temurin:21-alpine` (full JDK, ~400MB)
   - `usuario-service` uses `eclipse-temurin:21-jre-alpine` (JRE only, ~200MB)
   - Production images should use minimal JRE for security and size

---

### 🔹 Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-INFRA-01-01 | Align `usuario-service` application.properties to use `SPRING_RABBITMQ_HOST` variable name (matching docker-compose.yml) |
| FR-INFRA-01-02 | Standardize `pedido-service` Dockerfile to use `eclipse-temurin:21-jre-alpine` base image |
| FR-INFRA-01-03 | Document environment variable naming convention in project README or copilot-instructions.md |
| FR-INFRA-01-04 | Verify both services successfully connect to RabbitMQ after changes |

---

### 🔹 Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-INFRA-01-01 | Image Size | Production images should use JRE, not full JDK (target: <250MB per service) |
| NFR-INFRA-01-02 | Convention | All services must use same environment variable naming pattern for shared dependencies |
| NFR-INFRA-01-03 | Security | Minimal base images reduce attack surface |

---

### 🔹 Acceptance Criteria

#### Positive Scenarios (GIVEN / WHEN / THEN)

**CA-01: usuario-service connects to RabbitMQ in Docker**
- **Given** docker-compose.yml provides `SPRING_RABBITMQ_HOST=rabbitmq`
- **When** I run `docker-compose up`
- **Then** usuario-service logs show successful RabbitMQ connection

**CA-02: pedido-service connects to RabbitMQ in Docker**
- **Given** docker-compose.yml provides `SPRING_RABBITMQ_HOST=rabbitmq`
- **When** I run `docker-compose up`
- **Then** pedido-service logs show successful RabbitMQ connection

**CA-03: pedido-service image uses JRE**
- **Given** Dockerfile is updated to use `eclipse-temurin:21-jre-alpine`
- **When** I build pedido-service image with `docker build`
- **Then** `docker images` shows image size under 300MB

**CA-04: Full stack starts successfully**
- **Given** all configuration changes are applied
- **When** I run `docker-compose up --build`
- **Then** all services start without errors and health checks pass

#### Negative Scenarios

**CA-05: Service fails gracefully if RabbitMQ unavailable**
- **Given** RabbitMQ container is not running
- **When** usuario-service or pedido-service starts
- **Then** Service logs connection retry attempts (does not crash immediately)

**CA-06: Missing environment variable has default fallback**
- **Given** `SPRING_RABBITMQ_HOST` is not set
- **When** service starts
- **Then** Service attempts connection to `localhost` (default fallback)

---

# 3️⃣ INVEST VALIDATION

| Criterion | Status | Justification |
|-----------|--------|---------------|
| Independent | ✔ | Infrastructure fix independent of application business logic |
| Negotiable | ✔ | Which file to change (compose vs properties) is negotiable |
| Valuable | ✔ | Fixes actual deployment configuration bug and reduces image size |
| Estimable | ✔ | Clear scope: 2-3 file changes |
| Small | ✔ | Can be completed in under 1 hour |
| Testable | ✔ | Verifiable via docker-compose up and image size inspection |

---

# 4️⃣ ASSUMPTIONS

- Spring Boot's `SPRING_RABBITMQ_HOST` convention is preferred over custom names
- JRE-alpine images are sufficient for production (no need for JDK debugging tools)
- No custom native libraries require full JDK

---

# 5️⃣ CONSTRAINTS

- Must not change RabbitMQ credentials or port configurations
- Must maintain existing volume mounts and network configurations
- Changes must be backwards compatible with CI pipeline

---

# 6️⃣ DEPENDENCIES

| Type | Description |
|------|-------------|
| Internal | None - standalone infrastructure story |

---

# 7️⃣ TECHNICAL NOTES

**File Changes Required:**

### 1. usuario-service application.properties
```properties
# Current (line 38)
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}

# Should be
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
```

### 2. pedido-service Dockerfile
```dockerfile
# Current (line 10)
FROM eclipse-temurin:21-alpine

# Should be
FROM eclipse-temurin:21-jre-alpine
```

**Verification Commands:**
```bash
# Test full stack
docker-compose up --build

# Check image sizes
docker images | grep -E "(usuario|pedido)-service"

# Verify RabbitMQ connection in logs
docker-compose logs usuario-service | grep -i rabbit
docker-compose logs pedido-service | grep -i rabbit
```

**Current Configuration Reference:**

| Service | Variable in Properties | Variable in Compose |
|---------|----------------------|---------------------|
| usuario-service | `RABBITMQ_HOST` | `SPRING_RABBITMQ_HOST` |
| pedido-service | `SPRING_RABBITMQ_HOST` | `SPRING_RABBITMQ_HOST` |

After fix, both should use `SPRING_RABBITMQ_HOST`.
