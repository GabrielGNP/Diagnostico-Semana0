# HANDOVER REPORT — Diagnostico-Semana0

Short handover produced from a workspace scan (controllers, services, messaging, run commands).

## Big picture
- Frontend: React + Vite serving UI on port 3000 (see `Frontend/`).
- Two Spring Boot services in `Backend/`:
  - `usuario-service` (user CRUD) — container port `8081` mapped to host `8083` via `docker-compose.yml`. Controller: `Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java`.
  - `pedido-service` (orders) — container port `8080` mapped to host `8082` via `docker-compose.yml`. Controller: `Backend/pedido-service/src/main/java/com/example/pedidoservice/controller/OrderController.java`.
- Services communicate asynchronously via RabbitMQ (exchange `user-exchange`, queues `user-request-queue` / `user-response-queue`). See `RABBITMQ_INTEGRATION.md`.

## Data & persistence
- Demo persistence is file-backed JSON in the repo:
 - Demo persistence is file-backed JSON in the repo. Two locations are commonly used depending on how you run the service:
  - Users canonical resource: `Backend/usuario-service/src/main/resources/users.json`. When running with Docker Compose the service mounts `Backend/usuario-service/data` into the container (`/data`) so edit `Backend/usuario-service/data/users.json` to change running state.
  - Orders: `Backend/data/orders.json` (mounted into the `pedido-service` container as `/data` when using docker-compose).
- No RDBMS configured by default — tests and local runs rely on these JSON files.

## Messaging flow (essential)
1. `pedido-service` sends `UserRequest` to `user-request-queue` via `UserServiceProducer`.
2. `usuario-service` listens on `USER_REQUEST_QUEUE` (`UserServiceConsumer`), looks up user and sends `UserResponse` to `user-response-queue`.
3. `pedido-service` `UserServiceConsumer` collects responses and `OrderService` waits up to 3000 ms for the answer (see the timeout constant in `OrderService`, e.g. `USER_REQUEST_TIMEOUT`).

Key files:
- `Backend/pedido-service/src/main/java/.../messaging/UserServiceProducer.java`
- `Backend/pedido-service/src/main/java/.../messaging/UserServiceConsumer.java`
- `Backend/usuario-service/src/main/java/.../messaging/UserServiceConsumer.java`
- `RABBITMQ_INTEGRATION.md` (detailed doc of queues/exchanges)

## Important endpoints (quick list)
- `GET /order/all`, `GET /order/{id}`, `GET /order/user/{idUser}`, `GET /order/{id}/with-user-info` — see `OrderController`. Example external URL when using docker-compose: `http://localhost:8082/order/all`.
- `GET /v1/usuarios`, `GET /v1/usuarios/{identificador}`, `POST /v1/usuarios`, `PUT/PATCH /v1/usuarios/{id}` — see `UsuarioController`. Note: `usuario-service` defines a context path `/api` in `application.properties`, so the external routes are served at `http://localhost:8083/api/v1/usuarios` when using docker-compose.

## How to run locally
- Recommended (docker-compose): from repo root:

```bash
docker-compose up --build
```

This starts `rabbitmq`, `usuario-service`, `pedido-service`, and `frontend` with ports mapped (see `docker-compose.yml`).

Direct (dev) commands:
```bash
# Start RabbitMQ only
docker-compose up -d rabbitmq

# Backend services (each in separate shells)
mvn -f Backend/usuario-service/pom.xml spring-boot:run
mvn -f Backend/pedido-service/pom.xml spring-boot:run

# Frontend
cd Frontend && npm install && npm run dev
```

## Tests & CI
- Frontend unit/integration: `cd Frontend && npm run test` / `npm run test:integration` (Vitest).
- Backend: `mvn -f Backend/<service>/pom.xml test`.
- CI: See `.github/workflows/ci-full-pipeline.yml` — pipeline runs unit, component integration, docker smoke and e2e stages.

## Code patterns & conventions (repo-specific)
- JSON-backed repos: update `Backend/*/data/*.json` intentionally (these are the system-of-record in demos).
- Keep API client calls inside `Frontend/src/services/*` (see `api.ts`, `usuarioService.ts`, `pedidoService.ts`).
- Messaging DTOs must be identical across services — update both sides when changing structure.

## Next steps I performed for you
- Generated this `HANDOVER_REPORT.md` to capture the workspace explain snapshot.

## Recommended automation (optional)
- Scaffold a minimal `context-agent` script to run two actions:
  - `/explain`: produce a short architectural summary (this file) and save snapshots under `docs/context-snapshots/`.
  - `/doc`: locate complex or under-documented classes (`service`, `messaging`, `mapper`) and auto-generate Javadoc/docstrings into source or a `docs/` folder.

---
If quieres que genere Javadoc/docstrings automáticos ahora (Actividad 1.2), dime y los creo como parches. También puedo scaffoldar el `context-agent` si prefieres automatizar `@workspace/explain` en un script local.