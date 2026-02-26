# Backend — Docker Compose

Este README contiene los comandos mínimos para levantar el stack backend en entorno local o en Codespaces.

Servicios orquestados:
- `postgres` (BD)
- `pgadmin` (UI PG)
- `rabbitmq` (broker)
- `usuario-service` (Spring Boot)
- `pedido-service` (Spring Boot)

Puerto relevantes (host):
- Postgres: 5432
- pgAdmin: 5050
- RabbitMQ UI: 15672
- usuario-service: 8083
- pedido-service: 8082

Ejecutar (desde la raíz del repo):

```bash
# Levanta el backend (build + up)
docker compose -f backend/docker-compose.yml up --build

# En segundo plano
docker compose -f backend/docker-compose.yml up -d --build
```

Parar y limpiar:

```bash
docker compose -f backend/docker-compose.yml down
# Eliminar volúmenes (limpieza completa)
docker compose -f backend/docker-compose.yml down -v
```

Validaciones rápidas:

```bash
# Ver estado de contenedores del compose
docker compose -f backend/docker-compose.yml ps

# Seguir logs de un servicio
docker compose -f backend/docker-compose.yml logs -f usuario-service

# Listar archivos de init en Postgres
docker exec -it postgres_server ls -la /docker-entrypoint-initdb.d

# Probar endpoint ejemplo
curl -sS http://localhost:8083/api/v1/usuarios | jq .
```

Nota para GitHub Codespaces
- Si tu Codespace incluye Docker daemon puedes usar los mismos comandos.
- Si no, levanta el backend en otro host con Docker y apunta las variables de los servicios (DB/Rabbit) desde el Codespace a ese host.
- Para exponer la UI del frontend (Vite) desde Codespaces recuerda añadir el puerto en el panel `Ports` y usar la URL pública que Codespaces genera.

Seguridad y entornos
- Estas configuraciones son para desarrollo local. En producción:
  - Versiona las imágenes Docker por tag.
  - Gestiona secretos con vault/secret manager (no en `application.properties`).
  - No utilices `addAllowedOrigin("*")` en CORS para producción.

Si quieres, creo también `scripts/dev-up.sh` que automatice `up --build` + comprobaciones de salud.