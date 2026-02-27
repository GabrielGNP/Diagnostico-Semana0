# Contexto — Semana 3

Fecha: 2026-02-23

Este archivo resume el contexto y las instrucciones para el trabajo de la **Semana 3**.

Usaremos `@workspace/explain` para generar explicaciones y mantener este documento actualizado.

**Objetivo:**
- Alinear al equipo sobre el estado actual del proyecto y las tareas prioritarias para la Semana 3.

**Alcance de la semana:**
- Desarrollo y/o ajustes en `pedido-service` y `usuario-service`.
- Integración y pruebas rápidas con RabbitMQ y Postgres (entorno local con `docker compose`).

**Servicios principales (puertos locales):**
- usuario-service: http://localhost:8083  (endpoints bajo `/users`)
- pedido-service: http://localhost:8082  (endpoints bajo `/order/*`)
- RabbitMQ management: http://localhost:15672
- Frontend (opcional): http://localhost:3000

**Endpoints útiles:**
- `GET /users` — listar usuarios
- `GET /order/all` — listar pedidos

**Comandos rápidos (desde la raíz del repo):**
```bash
docker compose up -d
docker compose logs -f
docker compose down -v
```

**Archivos relevantes:**
- Backend/pedido-service/src — lógica de pedidos
- Backend/usuario-service/src — lógica de usuarios
- .github/workflows/ci-docker-smoke.yml — smoke tests de infraestructura

**Notas de integración:**
- Asegurar que los DTOs de RabbitMQ estén sincronizados entre servicios si se modifica el payload.
- Revisar `MIGRATION_ENABLED` antes de ejecutar en entornos con Postgres persistente.

**Contacto / Responsables:**
- Equipo: Equipo 2 — usar canal de Slack/Teams del proyecto para coordinación.

**Tareas sugeridas (inicio de semana):**
- Reproducir el entorno local con `docker compose up -d`.
- Ejecutar `ci-docker-smoke.yml` localmente o revisar su contenido para entender checks.
- Validar endpoints principales y reportar fallos en issue si aparecen.

---

Generado para uso con `@workspace/explain`. Actualiza este archivo según se avance en la semana.

---

## Resumen visual y rápido (para todo el equipo)

- Objetivo: entender la arquitectura y cómo probar/integrar rápido.

Arquitectura (vista simple):

```
Frontend (Vite)  <--HTTP-->  pedido-service  <--RabbitMQ-->  usuario-service
	|                                 |                             |
	|--API clients (TS)               |--Order API (/order/*)       |--User API (/users)
	|                                 |                             |
	`-> interactúa con backend        `-> produce/consume mensajes   `-> responde a requests
Postgres (5432) <-persist-> pedido-service, usuario-service
RabbitMQ (15672/5672) -> user-exchange -> user-request/response-queues
```

Componentes clave:
- `usuario-service` (Spring Boot): puerto `8083`, endpoints bajo `/users`, DTOs RabbitMQ.
- `pedido-service` (Spring Boot): puerto `8082`, endpoints bajo `/order/*`, envía `UserRequest` y espera `UserResponse`.
- `Frontend` (Vite + TS): puerto `3000`, clientes en `Frontend/src/services`.

Mensajería (esencial):
- Exchange: `user-exchange` (direct).
- Queues: `user-request-queue`, `user-response-queue`.
- Flujo: `pedido-service` -> `UserRequest` -> `usuario-service` -> `UserResponse` -> `pedido-service`.
- Si cambias payloads: actualizar DTOs en ambos servicios y `RabbitMQConfig`.

Checks y comandos útiles:
```bash
# Levantar todo
docker compose up -d

# Verificar endpoints
curl -fsS http://localhost:8083/users
curl -fsS http://localhost:8082/order/all

# Logs y debug
docker compose logs -f
docker compose down -v
```

Puntos importantes / gotchas:
- JSON en `Backend/*/data/*.json` son fixtures legacy — no confiar para persistencia real.
- Revisar `MIGRATION_ENABLED` antes de usar Postgres persistente (evita sorpresas).
- Timeout inter-servicio: `USER_REQUEST_TIMEOUT` ≈ 3000ms — puede fallar si RabbitMQ o usuario-service están lentos.

Acciones recomendadas para empezar la semana:
- Levantar stack local y validar endpoints (`docker compose up -d`).
- Ejecutar los smoke tests descritos en `.github/workflows/ci-docker-smoke.yml` para entender checks.
- Si se modifica mensajería: cambiar DTOs en ambos servicios + ejecutar tests.

---

Actualiza o pide añadir diagramas/ejemplos (secuencia RabbitMQ, payloads) si quieres que lo incluya aquí.
