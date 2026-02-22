# Diagnostico-Semana0#

## Descripción

Proyecto de microservicios para gestión de usuarios y pedidos. Incluye un frontend en React (Vite) y dos servicios backend en Spring Boot, comunicados mediante RabbitMQ.

## Arquitectura

```
┌─────────────────┐     ┌─────────────────┐
│    Frontend     │     │    RabbitMQ     │
│   (React/Vite)  │     │   (Mensajería)  │
│    Puerto 3000  │     │  Puerto 15672   │
└────────┬────────┘     └────────┬────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│ Usuario Service │     │  Pedido Service │
│  (Spring Boot)  │◄───►│  (Spring Boot)  │
│   Puerto 8083   │     │   Puerto 8082   │
└─────────────────┘     └─────────────────┘
```

## Servicios

| Servicio            | Tecnología   | Puerto       | Descripción                |
| ------------------- | ------------ | ------------ | -------------------------- |
| **frontend**        | React + Vite | 3000         | Interfaz de usuario        |
| **usuario-service** | Spring Boot  | 8083         | API de gestión de usuarios |
| **pedido-service**  | Spring Boot  | 8082         | API de gestión de pedidos  |
| **rabbitmq**        | RabbitMQ     | 5672 / 15672 | Broker de mensajería       |

## Requisitos Previos

- Docker
- Docker Compose

## Ejecución Local

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd Diagnostico-Semana0
```

### 2. Ejecutar con Docker Compose

```bash
docker-compose up --build
```

Para ejecutar en segundo plano:

```bash
docker-compose up -d --build
```

### 3. Acceder a los servicios

- **Frontend:** http://localhost:3000
- **Usuario Service API:** http://localhost:8083
- **Pedido Service API:** http://localhost:8082
- **RabbitMQ Management:** http://localhost:15672 (usuario: `guest`, contraseña: `guest`)
 - **pgAdmin (PGAdmin4):** http://localhost:5050 (usuario: `admin@admin.com`, contraseña: `admin123`)

### Acceso a pgAdmin y ver las bases de datos

- El `docker-compose.yml` arranca un servicio `pgadmin` mapeado en el puerto `5050`. Usa las credenciales del servicio:

    - Email: `admin@admin.com`
    - Password: `admin123`

- Desde la interfaz web de pgAdmin crea un nuevo servidor (clic derecho en Servers → Create → Server) y usa estos datos de conexión:

    - **Nombre:** postgres (o cualquier nombre descriptivo)
    - **Host name/address:** postgres
    - **Port:** 5432
    - **Maintenance database:** postgres
    - **Username:** postgres
    - **Password:** postgres

    Nota: cuando pgAdmin corre como contenedor, debe conectarse al servicio `postgres` por su nombre de servicio de la red Docker (`postgres`) — NO uses `localhost` aquí.

- Alternativamente, puedes añadir dos conexiones separadas usando las cuentas creadas por los scripts de inicialización si prefieres ver/usar cada base con su usuario específico:

    1) Conexión a `users_db` (usuario-service)

         - **Host:** postgres
         - **Port:** 5432
         - **Maintenance database:** users_db
         - **Username:** usuario_user
         - **Password:** usuario_pass

    2) Conexión a `orders_db` (pedido-service)

         - **Host:** postgres
         - **Port:** 5432
         - **Maintenance database:** orders_db
         - **Username:** pedido_user
         - **Password:** pedido_pass

    Estas cuentas son creadas por los scripts montados en el contenedor `postgres` (`Backend/usuario-service/init-db/01-init-users.sql` y `Backend/pedido-service/init-db/01-init-orders.sql`).

 - Una vez conectado, expande el servidor y verás las bases `users_db` y `orders_db` bajo la sección `Databases`.


## Detener los servicios

```bash
docker-compose down
```

Para eliminar también los volúmenes:

```bash
docker-compose down -v
```

## Estructura del Proyecto

```
Diagnostico-Semana0/
├── docker-compose.yml
├── README.md
├── Backend/
│   ├── data/
│   │   └── orders.json
│   ├── pedido-service/      # Microservicio de pedidos
│   └── usuario-service/     # Microservicio de usuarios
├── Frontend/                # Aplicación React
```


## Tests y CI/CD

- **Tests unitarios:** cada subproyecto tiene su propia suite (JUnit en backend, Vitest en frontend).
- **CI:** en cada push o PR a `main`/`master`, GitHub Actions ejecuta los tests de los tres subproyectos. Ver [docs/TESTING_AND_CI.md](docs/TESTING_AND_CI.md) para detalles y cómo exigir que los tests pasen antes de merge.
=======