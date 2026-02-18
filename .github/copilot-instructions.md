<!-- Copilot / AI agent helper for this repository -->
# Copilot instructions — quick onboarding

Purpose: short, actionable guidance to make AI coding agents productive in this repo.

- **Big picture**: two Spring Boot services under `Backend/` and a Vite React frontend.
  - `Backend/usuario-service` — user management (REST, CORS configured in `application.properties`). See `UsuarioController` ([Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java](Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java)).
  - `Backend/pedido-service` — orders, uses RabbitMQ to enrich orders with user info. See `OrderController` ([Backend/pedido-service/src/main/java/com/example/pedidoservice/controller/OrderController.java](Backend/pedido-service/src/main/java/com/example/pedidoservice/controller/OrderController.java)).
  - `Frontend/` — Vite + React (TypeScript). Services clients live under `Frontend/src/services/`.

- **Inter-service integration**: asynchronous messaging via RabbitMQ.
  - Producer/consumer classes live in `**/messaging/` in both backend services (e.g. `UserServiceProducer`, `UserServiceConsumer`).
  - Exchange/queue names and flow are documented in `RABBITMQ_INTEGRATION.md` at repository root. Keep `RabbitMQConfig.java` in both services in sync.

- **Local run & build (quick commands)**:
 - **Local run & build (quick commands)**:
  - Full stack (recommended for first run or after code changes):
    ```bash
    docker-compose up --build
    ```
  - Start RabbitMQ only (fast):
    ```bash
    docker-compose up -d rabbitmq
    # Note: if you haven't built images yet, use `docker-compose up --build` first
    ```
  - Run user service (dev mode, module-local):
    ```bash
    mvn -f Backend/usuario-service/pom.xml spring-boot:run
    # When using docker-compose the host->container port mapping is 8083:8081 (host:container)
    ```
  - Run order service (dev mode, module-local):
    ```bash
    mvn -f Backend/pedido-service/pom.xml spring-boot:run
    # docker-compose maps host:container 8082:8080 for pedido-service
    ```
  - Frontend dev:
    ```bash
    cd Frontend && npm install && npm run dev
    ```
  - Tests:
    - Frontend unit: `cd Frontend && npm run test`
    - Frontend integration: `cd Frontend && npm run test:integration`
    - E2E: `cd tests/e2e && npm install && npm test`
    - Backend: use Maven test: `mvn -f Backend/<service>/pom.xml test`

- **Important patterns & conventions**:
  - JSON-backed repositories: data files live under `Backend/*/data` and `src/main/resources` (e.g. `users.json`, `orders.json`) — no RDBMS by default.
  - Lombok / MapStruct: backend uses Lombok to reduce boilerplate and may include MapStruct bindings for mappers; preserve generated/annotated patterns when refactoring.
  - DTO + mapper pattern (e.g. `OrderMapper`) is used to convert between entity and API models.
  - Controllers are small and delegate to `Service` classes (see controller → service separation in both backends).
  - Messaging DTOs must be mirrored across services (`UserRequest`, `UserResponse`) — update both sides when changing shape.

- **Critical integration points to check before changes**:
  - `Backend/pedido-service/src/main/java/.../messaging/*` and `Backend/usuario-service/src/main/java/.../messaging/*` (RabbitMQ producers/consumers).
  - `RABBITMQ_INTEGRATION.md` for expected exchange/queue names and routing keys.
  - `docker-compose.yml` at repo root for how RabbitMQ is started in CI/local.
  - CI workflows under `.github/workflows/` (e.g. `ci-full-pipeline.yml`) — editing build steps may require updating these.

- **Debugging & operational notes**:
  - RabbitMQ UI: http://localhost:15672 (default guest/guest when using provided compose)
  - Order service waits up to 3000 ms for user responses (`OrderService.USER_REQUEST_TIMEOUT`) — changes to timeout affect API behavior.
  - Logs are printed to stdout; prefer `mvn spring-boot:run` or container logs to inspect.
  - API context-path: `usuario-service` sets `server.servlet.context-path=/api` in `application.properties`, so external user endpoints are served under `/api/v1/usuarios` (host port mapping still applies).

- **When making edits (checklist for PRs)**:
  - If you change message payloads, update both producer and consumer DTOs and `RabbitMQConfig` bindings.
  - Update `Backend/*/data/*.json` only when intentional — these files are the system of record for the demo implementation.
  - Frontend API clients live in `Frontend/src/services/` — update there when backend routes or response shapes change.
  - Run unit and integration tests in `Frontend/` and run backend tests via Maven locally before opening PR.

  Quick verify (example):

  ```bash
  # When running via docker-compose (host ports):
  curl http://localhost:8083/api/v1/usuarios    # list users (usuario-service)
  curl http://localhost:8082/order/all          # list orders (pedido-service)
  ```

  Notes:
  - Canonical user data file: `Backend/usuario-service/src/main/resources/users.json`. When running with docker-compose edit `Backend/usuario-service/data/users.json` (mounted into the container).
  - Timeout: cross-service user lookup uses a ~3000 ms timeout in `OrderService` (see the constant `USER_REQUEST_TIMEOUT` in code).

  If anything in these notes is unclear or you'd like more examples (e.g. a local dev script, sample curl sequences, or DTO field mappings), tell me which section to expand.