# Diagnostico-Semana0

## Descripción

Proyecto de microservicios para gestión de usuarios y pedidos, con frontend SPA en React (Vite), backend en Spring Boot y comunicación asíncrona por RabbitMQ.

## Arquitectura

### Backend (orquestado con Docker Compose)

- `postgres` (persistencia)
- `pgadmin` (administración de BD)
- `rabbitmq` (mensajería)
- `usuario-service` (API usuarios)
- `pedido-service` (API pedidos)

Archivo de orquestación backend: [backend/docker-compose.yml](backend/docker-compose.yml)

### Frontend (ejecución independiente)

- `Frontend/` se ejecuta de forma separada del compose backend.
- En local puede correr con `npm run dev` o con su propio `Dockerfile`.
- Consume APIs vía variables Vite:
  - `VITE_APIUSER=http://localhost:8083`
  - `VITE_APIORDER=http://localhost:8082`

Plantilla: [Frontend/.env.template](Frontend/.env.template)

## Requisitos previos

- Docker + Docker Compose v2
- Node.js 20+ (para desarrollo frontend)

## Ejecución local

### 1) Levantar backend

Desde la raíz del repo:

```bash
docker compose -f backend/docker-compose.yml up --build
```

En segundo plano:

```bash
docker compose -f backend/docker-compose.yml up -d --build
```

### 2) Levantar frontend (separado)

```bash
cd Frontend
cp .env.template .env
npm install
npm run dev
```

Frontend disponible en `http://localhost:5173` (o el puerto que indique Vite).

## Endpoints y accesos

- Frontend: `http://localhost:5173` (dev) o `http://localhost:3000` si lo publicas con contenedor propio
- Usuario Service API: `http://localhost:8083`
- Pedido Service API: `http://localhost:8082`
- RabbitMQ Management: `http://localhost:15672` (`guest` / `guest`)
- pgAdmin: `http://localhost:5050` (`admin@admin.com` / `admin123`)

## Base de datos e inicialización

Los scripts SQL de inicialización se mantienen intactos y se montan desde el compose backend:

- `../Backend/usuario-service/init-db/01-init-users.sql`
- `../Backend/pedido-service/init-db/01-init-orders.sql`

Esto preserva la creación de `users_db` y `orders_db` al inicializar Postgres.

## Detener backend

```bash
docker compose -f backend/docker-compose.yml down
```

Con limpieza de volúmenes:

```bash
docker compose -f backend/docker-compose.yml down -v
```

## Estructura (resumen)

```text
Diagnostico-Semana0/
├── backend/
│   └── docker-compose.yml
├── Backend/
│   ├── usuario-service/
│   └── pedido-service/
└── Frontend/
    ├── Dockerfile
    ├── Dockerfile.dev
    └── .env.template
```

## Estrategia de despliegue (producción)

- Backend: mantener compose o migrar a orquestador según madurez, pero con imágenes versionadas por servicio.
- Frontend: servir como estático (Nginx/CDN) usando el `Frontend/Dockerfile` multi-stage.
- Configuración de URLs del frontend en build/release (`VITE_APIUSER`, `VITE_APIORDER`) apuntando a dominios de API reales (no `localhost`).
- No es necesario meter frontend en el mismo compose backend para producción: separarlo mejora independencia de despliegue, caching/CDN y tiempos de release.

## Tests y CI

- Frontend unit: `cd Frontend && npm test`
- Frontend integration: `cd Frontend && npm run test:integration`
- Backend unit: `mvn -f Backend/<service>/pom.xml test`
- E2E: `cd tests/e2e && npm install && npm test`
