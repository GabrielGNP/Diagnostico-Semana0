# Guía Docker (solo terminal, sin Docker Desktop)

Esta guía permite construir y ejecutar la aplicación **Diagnostico-Semana0** usando únicamente la terminal.

---

## Requisitos previos

- Docker Engine instalado y funcionando
- Terminal (PowerShell, CMD, bash, etc.)

Verificar que Docker funciona:

```powershell
docker --version
docker info
```

---

## Ubicarse en el proyecto

```powershell
cd C:\Users\jfran\Documents\projects\Diagnostico-Semana0
```

---

## Levantar Backend completo (recomendado)

El backend usa Docker Compose con los siguientes servicios:
- **postgres** - Base de datos PostgreSQL
- **pgadmin** - Administrador de BD (opcional)
- **rabbitmq** - Mensajería
- **usuario-service** - API de usuarios (puerto 8083)
- **pedido-service** - API de pedidos (puerto 8082)

### Construir y ejecutar en segundo plano

```powershell
docker compose -f Backend/docker-compose.yml up -d --build
```

### Verificar estado de contenedores

```powershell
docker compose -f Backend/docker-compose.yml ps
```

### Ver logs de todos los servicios

```powershell
docker compose -f Backend/docker-compose.yml logs -f
```

### Ver logs de un servicio específico

```powershell
docker compose -f Backend/docker-compose.yml logs -f usuario-service
docker compose -f Backend/docker-compose.yml logs -f pedido-service
```

---

## Levantar Frontend

### Construir imagen

```powershell
docker build -t diagnostico-semana0-frontend Frontend
```

### Ejecutar contenedor

```powershell
docker run -d --name ds0-frontend -p 3000:80 diagnostico-semana0-frontend
```

### Verificar que está corriendo

```powershell
docker ps --filter name=ds0-frontend
```

---

## Accesos una vez levantado

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| Usuario API | http://localhost:8083 |
| Pedido API | http://localhost:8082 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| pgAdmin | http://localhost:5050 (admin@admin.com / admin123) |
| PostgreSQL | localhost:5432 (postgres/postgres) |

---

## Detener servicios

### Detener backend (mantiene datos)

```powershell
docker compose -f Backend/docker-compose.yml down
```

### Detener backend y eliminar volúmenes (borra datos)

```powershell
docker compose -f Backend/docker-compose.yml down -v
```

### Detener frontend

```powershell
docker rm -f ds0-frontend
```

---

## Script rápido: levantar todo

Copia y pega en PowerShell para levantar backend + frontend de una vez:

```powershell
# Levantar backend
docker compose -f Backend/docker-compose.yml up -d --build

# Esperar a que Postgres esté healthy
Write-Output "Esperando a que los servicios estén listos..."
Start-Sleep -Seconds 10

# Construir y levantar frontend
docker build -t diagnostico-semana0-frontend Frontend
docker rm -f ds0-frontend 2>$null
docker run -d --name ds0-frontend -p 3000:80 diagnostico-semana0-frontend

# Mostrar estado
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

## Script rápido: detener todo

```powershell
docker compose -f Backend/docker-compose.yml down
docker rm -f ds0-frontend
```

---

## Solución de problemas

### Puerto ocupado

```
Error: bind: address already in use
```

Solución: identificar y detener el proceso que usa el puerto:

```powershell
netstat -ano | findstr :8082
taskkill /PID <PID> /F
```

O cambiar el puerto en el comando/compose.

### Contenedor con nombre duplicado

```
Error: container name already in use
```

Solución: eliminar el contenedor existente:

```powershell
docker rm -f <nombre_contenedor>
```

### Docker Engine no responde

```
Error: Cannot connect to the Docker daemon
```

Solución: iniciar Docker Engine manualmente:

```powershell
# En Windows sin Docker Desktop, inicia el servicio:
net start docker

# O si usas WSL2 con Docker CE:
wsl -d docker-desktop
```

### Reiniciar desde cero

```powershell
docker compose -f Backend/docker-compose.yml down -v
docker system prune -f
docker compose -f Backend/docker-compose.yml up -d --build
```

---

## Comandos útiles de diagnóstico

```powershell
# Listar todos los contenedores (activos e inactivos)
docker ps -a

# Inspeccionar un contenedor
docker inspect <nombre_contenedor>

# Entrar a un contenedor
docker exec -it <nombre_contenedor> sh

# Ver uso de recursos
docker stats

# Ver redes
docker network ls

# Ver volúmenes
docker volume ls
```
