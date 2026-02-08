# Diagnostico-Semana0#

## Descripción

Proyecto de microservicios para gestión de usuarios y pedidos. Incluye un frontend en React y dos servicios backend en Spring Boot, comunicados mediante RabbitMQ.

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
