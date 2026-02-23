<!-- Copilot / AI agent helper for this repository -->
# Copilot instructions - quick onboarding

Purpose: short, actionable guidance to make AI coding agents productive in this repo.

## Architecture overview

Two Spring Boot services + Vite React frontend, communicating via RabbitMQ:

| Service | Host Port | Container Port | Context Path |
|---------|-----------|----------------|--------------|
| `usuario-service` | 8083 | 8081 | `/api` - endpoints at `/api/v1/usuarios` |
| `pedido-service` | 8082 | 8080 | (none) - endpoints at `/order/*` |
| `frontend` | 3000 | 80 | - |
| `rabbitmq` | 15672 (UI) / 5672 | - | guest/guest |
| `postgres` | 5432 | 5432 | postgres/postgres |
| `pgadmin` | 5050 | 80 | admin@admin.com / admin123 |

Key source files:
- [UsuarioController](Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java)
- [OrderController](Backend/pedido-service/src/main/java/com/example/pedidoservice/controller/OrderController.java)
- Frontend API clients: `Frontend/src/services/{usuarioService,pedidoService}.ts`

## Inter-service messaging (RabbitMQ)

Exchange: `user-exchange` (DirectExchange) | Queues: `user-request-queue`, `user-response-queue`

Flow: `pedido-service` -> `UserRequest` -> `usuario-service` -> `UserResponse` -> `pedido-service` (timeout: 3000ms in `OrderService.USER_REQUEST_TIMEOUT`)

Key files to keep in sync:
- `Backend/*/src/main/java/.../messaging/RabbitMQConfig.java`
- `Backend/*/src/main/java/.../messaging/User{Request,Response}.java` (mirrored DTOs)
- See [RABBITMQ_INTEGRATION.md](RABBITMQ_INTEGRATION.md) for full documentation.

## Local run & build

```bash
# Full stack (recommended)
docker-compose up --build

# Dev mode (requires RabbitMQ running)
docker-compose up -d rabbitmq postgres
mvn -f Backend/usuario-service/pom.xml spring-boot:run
mvn -f Backend/pedido-service/pom.xml spring-boot:run
cd Frontend && npm install && npm run dev
```

Quick verify:
```bash
curl http://localhost:8083/api/v1/usuarios    # users
curl http://localhost:8082/order/all          # orders
```

## Testing

| Scope | Command |
|-------|---------|
| Frontend unit | `cd Frontend && npm test` |
| Frontend integration | `cd Frontend && npm run test:integration` |
| Backend unit | `mvn -f Backend/<service>/pom.xml test` |
| E2E | `cd tests/e2e && npm install && npm test` |

CI pipeline (`.github/workflows/ci-full-pipeline.yml`): Unit -> Component Integration -> Docker Smoke -> E2E

## Patterns & conventions

- **Persistence (actual)**: PostgreSQL is the single source of truth for all runtime data. All application data MUST be stored in PostgreSQL in production and persisted environments.

- **Legacy fixtures (JSON)**: The JSON files under `Backend/*/data/*.json` are legacy test fixtures only. They were used for early local testing and are scheduled to be removed in upcoming versions — do not edit them and do not rely on them for runtime state.

- **Migrations & seeds (clarification)**: The `MIGRATION_ENABLED` flag controls DB schema migrations and optional seed scripts (e.g., Flyway/Liquibase or SQL scripts under `Backend/*/init-db/`) that prepare Postgres schemas and initial data. This flag does NOT trigger any automatic conversion of JSON fixtures into the database. Enabling migrations (`MIGRATION_ENABLED=true`) is for applying schema and approved seed scripts to Postgres in environments that require persistent storage. If you need to populate Postgres for tests or staging, use the provided SQL seed scripts or add explicit migration scripts; there is no plan to migrate data from the JSON fixtures automatically.
- **Enums**: Use `UPPER_CASE` values. See `State.java` for reference: `PROCESSING`, `TRAVELING_TO_WAREHOUSE`, `DELIVERED`, etc.
- **DTO + Mapper**: `OrderMapper`, entities/DTOs separated. Use MapStruct annotations.
- **Controller -> Service**: Controllers are thin, delegate to `*Service` classes.
- **Lombok**: Used throughout backend - preserve `@Data`, `@Builder`, etc.
- **Frontend**: TypeScript + TailwindCSS. API clients wrap `ApiClient` class in `api.ts`.

## PR checklist

1. If changing message payloads -> update both producer/consumer DTOs + `RabbitMQConfig`
2. If changing backend routes -> update `Frontend/src/services/` clients
3. If changing persistence or migrations -> update DB migration scripts and set `MIGRATION_ENABLED` accordingly; do not rely on editing JSON files for runtime state.
4. Run local tests before pushing

## Project documentation

- [ARQUITECTURA.md](ARQUITECTURA.md) - detailed architecture diagrams
- [RABBITMQ_INTEGRATION.md](RABBITMQ_INTEGRATION.md) - messaging flow
- [Entregables - Week 2/USER_STORIES/](Entregables%20-%20Week%202/USER_STORIES/) - user stories (HU-USR-*, HU-ORD-*)
- [TECHNICAL_DEBT_AUDIT.md](TECHNICAL_DEBT_AUDIT.md) - known issues
