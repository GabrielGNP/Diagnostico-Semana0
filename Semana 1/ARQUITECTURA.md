# Resumen Arquitectónico del Proyecto
## Sistema de Gestión de Usuarios y Pedidos

**Fecha:** 17 de Febrero, 2026  
**Repositorio:** Diagnostico-Semana0  
**Tipo de Arquitectura:** Microservicios con Mensajería Asíncrona

---

## 📋 Descripción General

Sistema distribuido para la gestión de usuarios y pedidos implementado con arquitectura de microservicios. El proyecto utiliza Spring Boot para los servicios backend, React para el frontend, y RabbitMQ para la comunicación asíncrona entre servicios.

### Características Principales
- ✅ Arquitectura de microservicios desacoplada
- ✅ Comunicación asíncrona mediante mensajería
- ✅ Frontend SPA con React + Vite
- ✅ Contenedorización completa con Docker
- ✅ Persistencia basada en archivos JSON
- ✅ Testing automatizado (70%+ coverage en backend)
- ✅ Patrones de diseño avanzados (Factory, Decorator, Strategy)

---

## 🏗️ Arquitectura de Alto Nivel

```
┌────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                │
│                  React 18.3 + Vite 5.2                         │
│                   Puerto 3000 (HTTP)                           │
│              TypeScript + TailwindCSS + Vitest                 │
└─────────────────────┬──────────────────────────────────────────┘
                      │ HTTP REST
                      ▼
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
┌──────────────────┐        ┌──────────────────┐
│ Usuario Service  │◄──────►│  Pedido Service  │
│  Spring Boot 4.0 │ RabbitMQ│  Spring Boot 4.0 │
│    Java 21       │        │    Java 21       │
│  Puerto 8083     │        │  Puerto 8082     │
└────────┬─────────┘        └────────┬─────────┘
         │                           │
         │ JSON File                 │ JSON File
         ▼                           ▼
    users.json                  orders.json
         │                           │
         └───────────┬───────────────┘
                     │
                     ▼
           ┌──────────────────┐
           │    RabbitMQ      │
           │  3-management    │
           │ Puerto 5672      │
           │ UI: 15672        │
           └──────────────────┘
```

### Flujo de Comunicación

#### 1. Comunicación Síncrona (REST)
```
Cliente → Frontend → Backend Services → Persistencia → Backend → Frontend → Cliente
```

#### 2. Comunicación Asíncrona (RabbitMQ)
```
pedido-service → RabbitMQ (user.request) → usuario-service
usuario-service → RabbitMQ (user.response) → pedido-service
```

---

## 🛠️ Stack Tecnológico

### Backend

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Framework** | Spring Boot | 4.0.2 |
| **Lenguaje** | Java | 21 |
| **Mensajería** | RabbitMQ + Spring AMQP | 3-management |
| **Validación** | Jakarta Bean Validation | 3.x |
| **Serialización** | Jackson (JSON) | 2.x |
| **Testing** | JUnit 5 + Mockito + Spring Test | 5.10.x |
| **Build Tool** | Maven | 3.x |
| **Utilities** | Lombok | Latest |

### Frontend

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Framework** | React | 18.3.1 |
| **Build Tool** | Vite | 5.2.0 |
| **Lenguaje** | TypeScript | 5.5.4 |
| **Routing** | React Router DOM | 7.13.0 |
| **Estilos** | TailwindCSS | 3.4.19 |
| **Testing** | Vitest + Testing Library | 1.6.1 |
| **Iconos** | Lucide React | 0.563.0 |

### Infraestructura

| Componente | Tecnología | Propósito |
|------------|------------|-----------|
| **Contenedorización** | Docker + Docker Compose | Orquestación de servicios |
| **Networking** | Docker Bridge Network | Comunicación inter-contenedores |
| **Message Broker** | RabbitMQ | Cola de mensajes asíncrona |
| **Web Server** | Nginx (en Frontend) | Servir aplicación React |

---

## 🧩 Componentes Principales

### 1. Usuario Service (Puerto 8083)

**Responsabilidad:** Gestión completa del ciclo de vida de usuarios

#### Estructura de Paquetes
```
com.example.usuarioservice/
├── config/              # Configuración de Spring (RabbitMQ, CORS, etc.)
├── controller/          # REST Controllers (@RestController)
│   └── UsuarioController
├── dto/                 # Data Transfer Objects
│   ├── CreateUsuarioRequest
│   └── UsuarioDto
├── exception/           # Manejo de excepciones personalizadas
├── mapper/              # Conversión Entity ↔ DTO
├── messaging/           # RabbitMQ Producers/Consumers
│   ├── UserServiceProducer
│   └── UserServiceConsumer
├── model/               # Entidades de dominio
│   └── Usuario
├── persistence/         # Capa de persistencia con patrones
│   ├── IUserPersistence (interface)
│   ├── JsonUserPersistence (implementación)
│   ├── CachedUserPersistenceDecorator
│   └── UserPersistenceFactory
├── service/             # Lógica de negocio
│   ├── IUsuarioService (interface)
│   └── UsuarioService (implementación)
└── validation/          # Estrategias de validación
    ├── IValidationStrategy
    ├── LenientValidationStrategy
    └── StrictValidationStrategy
```

#### Endpoints Principales
```
GET    /api/usuarios               # Listar todos los usuarios
GET    /api/usuarios/{id}          # Obtener usuario por ID
POST   /api/usuarios               # Crear nuevo usuario
PUT    /api/usuarios/{id}          # Actualizar usuario
DELETE /api/usuarios/{id}          # Eliminar usuario
GET    /api/usuarios/active        # Listar usuarios activos
```

#### Patrones de Diseño Implementados

**1. Factory Pattern** (`UserPersistenceFactory`)
- Crea instancias de persistencia basadas en configuración
- Soporta múltiples tipos: JSON, Database (futuro), In-Memory
- Configuración: `app.persistence.type=json`

**2. Decorator Pattern** (`CachedUserPersistenceDecorator`)
- Envuelve la persistencia con caché en memoria
- Mejora rendimiento: O(n) → O(1) en búsquedas
- Configuración: `app.persistence.cache.enabled=true`

**3. Strategy Pattern** (Validación)
- `LenientValidationStrategy`: Validaciones básicas
- `StrictValidationStrategy`: Validaciones estrictas (formato email, password fuerte)
- Intercambiables sin modificar código cliente

#### Características Técnicas
- ✅ Service Layer con SRP (Single Responsibility Principle)
- ✅ Jakarta Bean Validation (`@Valid`, `@Email`, `@NotBlank`)
- ✅ Constructor Injection (mejor práctica Spring)
- ✅ 20+ tests (16 integration + 4 unit) = 50%+ coverage
- ✅ Manejo de excepciones global
- ⚠️ Passwords en texto plano (deuda técnica crítica)

---

### 2. Pedido Service (Puerto 8082)

**Responsabilidad:** Gestión de pedidos y coordinación con usuario-service

#### Estructura de Paquetes
```
com.example.pedidoservice/
├── config/              # Configuración RabbitMQ
│   └── RabbitMQConfig
├── controller/          # REST Controllers
│   └── OrderController
├── dto/                 # Data Transfer Objects
│   ├── OrderDto
│   ├── OrderWithUserDto
│   ├── UserRequest
│   └── UserResponse
├── mapper/              # Conversión Entity ↔ DTO
├── messaging/           # RabbitMQ Messaging
│   ├── UserServiceProducer
│   └── UserServiceConsumer
├── model/               # Entidades de dominio
│   ├── Order
│   └── OrderStatus (enum)
├── repository/          # Capa de persistencia
│   └── OrderRepository
└── service/             # Lógica de negocio
    └── OrderService
```

#### Endpoints Principales
```
GET    /order                              # Listar todos los pedidos
GET    /order/{id}                         # Obtener pedido por ID
GET    /order/user/{userId}                # Pedidos por usuario
GET    /order/user/{userId}/with-info      # Pedidos con info de usuario (RabbitMQ)
POST   /order                              # Crear nuevo pedido
PUT    /order/{id}                         # Actualizar pedido
DELETE /order/{id}                         # Eliminar pedido
```

#### Estados de Pedido (OrderStatus)
```java
public enum OrderStatus {
    PROCESSING,              // Procesando
    WAITING_FOR_PAYMENT,     // Esperando pago
    TRAVELING_TO_WAREHOUSE,  // En tránsito a almacén
    DELIVERED                // Entregado
}
```

#### Integración RabbitMQ

**Caso de Uso:** Endpoint `/order/user/{userId}/with-info`

1. **Request:** Cliente solicita pedidos con información de usuario
2. **Producer:** `pedido-service` envía `UserRequest` a cola `user-request-queue`
3. **Consumer:** `usuario-service` procesa solicitud
4. **Producer:** `usuario-service` envía `UserResponse` a cola `user-response-queue`
5. **Consumer:** `pedido-service` recibe respuesta (timeout: 3s)
6. **Response:** Cliente recibe `List<OrderWithUserDto>` con datos enriquecidos

**Configuración RabbitMQ:**
```java
Exchange: "user-exchange" (DirectExchange)
Colas:
  - user-request-queue  → routing key: "user.request"
  - user-response-queue → routing key: "user.response"
```

#### Características Técnicas
- ✅ 34 tests (8 integration + 26 unit) = 70%+ coverage
- ✅ Comunicación asíncrona inter-servicios
- ✅ DTOs especializados por caso de uso
- ✅ Mappers para conversión limpia
- ⚠️ Sin state machine para transiciones de estado (deuda técnica)
- ⚠️ Sin resiliencia RabbitMQ (DLQ, retry)

---

### 3. Frontend (Puerto 3000)

**Responsabilidad:** Interfaz de usuario SPA + Consumo de APIs

#### Estructura de Código
```
src/
├── components/          # Componentes React
│   ├── AddOrder.tsx
│   ├── AddUser.tsx
│   ├── Dashboard.tsx
│   ├── Dashboard/
│   │   ├── PedidoCard.tsx
│   │   ├── PedidoList.tsx
│   │   └── UserFilter.tsx
│   └── __tests__/       # Tests de componentes
├── hooks/               # Custom Hooks
│   ├── useDashboardData.ts
│   └── __tests__/
├── interfaces/          # Tipos TypeScript
│   ├── IUser.ts
│   ├── Order.ts
│   └── index.ts
├── services/            # Capa de acceso a datos
│   ├── api.ts           # Cliente HTTP centralizado
│   ├── usuarioService.ts
│   ├── pedidoService.ts
│   └── __tests__/
└── test/                # Configuración de tests
    └── setup.js
```

#### Funcionalidades
- ✅ Dashboard con listado de pedidos + filtrado por usuario
- ✅ Formularios para crear usuarios y pedidos
- ✅ Integración con ambos servicios backend
- ✅ Manejo de estados de carga y errores
- ✅ Diseño responsive con TailwindCSS
- ✅ Routing con React Router

#### Testing
```bash
# Tests unitarios (15 tests)
npm run test
  ✓ Components: AddOrder, AddUser
  ✓ Hooks: useDashboardData
  ✓ Services: apiClient, pedidoService, usuarioService

# Tests de integración
npm run test:integration
  ✓ Integración simple de componentes
```

#### Variables de Entorno
```bash
VITE_APIUSER=http://localhost:8083  # URL usuario-service
VITE_APIORDER=http://localhost:8082 # URL pedido-service
```

---

## 💾 Persistencia de Datos

### Modelo de Datos

#### Usuario (users.json)
```json
{
  "id": 1,
  "name": "Alice Smith",
  "mail": "alice@example.com",
  "password": "alice123",  // ⚠️ Texto plano (deuda técnica)
  "active": true
}
```

#### Pedido (orders.json)
```json
{
  "id": 1,
  "idUser": 1,
  "datePurchase": "2024-03-15 10:30:00",
  "totalAmount": 150.50,
  "status": "DELIVERED"
}
```

### Estrategia de Persistencia

**Actual:** Archivos JSON en volúmenes Docker
- ✅ Pros: Simple, sin dependencias externas, fácil debug
- ❌ Contras: No escalable, sin transacciones, concurrencia limitada

**Futuro (Recomendado):** Migración a Base de Datos
- PostgreSQL para datos relacionales
- Redis para caché distribuida
- Esfuerzo estimado: 16h

---

## 🧪 Testing y Calidad

### Cobertura de Tests

| Servicio | Tests Totales | Unitarios | Integración | Coverage |
|----------|---------------|-----------|-------------|----------|
| **pedido-service** | 34 | 26 | 8 | 70%+ ✅ |
| **usuario-service** | 20 | 4 | 16 | 50%+ ⚠️ |
| **frontend** | 15 | 15 | 2 | 80%+ ✅ |

### Estrategia de Testing

**Backend (JUnit 5 + Mockito)**
```java
// Tests de Integración (@SpringBootTest)
- Contexto completo de Spring
- Testing de endpoints REST
- Validación de persistencia

// Tests Unitarios (@ExtendWith(MockitoExtension.class))
- Lógica de negocio aislada
- Mocking de dependencias
- Validación de DTOs/Mappers
```

**Frontend (Vitest + Testing Library)**
```typescript
// Tests de Componentes
- Renderizado y UI
- Interacción de usuario
- Estado y hooks

// Tests de Servicios
- Llamadas HTTP
- Manejo de errores
- Mocking de APIs
```

### CI/CD

**Archivos de Configuración:**
- `docs/TESTING_AND_CI.md`: Documentación de estrategia
- `.github/workflows/`: GitHub Actions (inferido)

**Proceso:**
1. Push/PR a `main` → Trigger automático
2. Build de servicios (Maven + npm)
3. Ejecución de tests en paralelo
4. Smoke tests con Docker Compose
5. Bloqueo de merge si fallan tests

---

## 🐳 Infraestructura y Despliegue

### Docker Compose

**Servicios Definidos:**
```yaml
services:
  - rabbitmq          # Message broker (Puerto 5672, UI: 15672)
  - usuario-service   # Backend usuarios (Puerto 8083)
  - pedido-service    # Backend pedidos (Puerto 8082)
  - frontend          # SPA React (Puerto 3000)
```

**Networking:**
- Red: `microservicios-network` (bridge)
- DNS interno: Los servicios se comunican por nombre

**Volúmenes:**
```yaml
usuario-service:
  volumes:
    - ./Backend/usuario-service/data:/data  # Persistencia users.json

pedido-service:
  volumes:
    - ./Backend/data:/data                   # Persistencia orders.json
```

### Comandos de Despliegue

```bash
# Iniciar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d --build

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Ver logs en tiempo real
docker-compose logs -f [servicio]
```

### Healthchecks

⚠️ **Deuda Técnica:** Sin healthchecks configurados
- Problema: Race conditions en startup
- Solución: Spring Boot Actuator + `depends_on: service_healthy`
- Esfuerzo: 2h

---

## 🎯 Patrones de Diseño Aplicados

### 1. Factory Pattern
**Ubicación:** `usuario-service/persistence/UserPersistenceFactory`
**Propósito:** Crear instancias de persistencia según configuración
```java
public IUserPersistence createPersistence() {
    String type = properties.getType();
    return switch(type) {
        case "json" -> new JsonUserPersistence();
        case "database" -> new DatabaseUserPersistence(); // Futuro
        default -> throw new IllegalArgumentException();
    };
}
```

### 2. Decorator Pattern
**Ubicación:** `usuario-service/persistence/CachedUserPersistenceDecorator`
**Propósito:** Añadir caché transparente a persistencia
```java
public class CachedUserPersistenceDecorator implements IUserPersistence {
    private final IUserPersistence delegate;
    private final Map<Integer, Usuario> cache = new HashMap<>();
    
    public Usuario findById(int id) {
        return cache.computeIfAbsent(id, delegate::findById);
    }
}
```

### 3. Strategy Pattern
**Ubicación:** `usuario-service/validation/`
**Propósito:** Intercambiar estrategias de validación
```java
public interface IValidationStrategy {
    void validate(CreateUsuarioRequest request);
}

// Implementaciones: LenientValidationStrategy, StrictValidationStrategy
```

### 4. Repository Pattern
**Ubicación:** Ambos servicios
**Propósito:** Abstracción de acceso a datos
```java
public interface IUserPersistence {
    List<Usuario> findAll();
    Optional<Usuario> findById(int id);
    Usuario save(Usuario usuario);
    void deleteById(int id);
}
```

### 5. DTO Pattern
**Ubicación:** Todos los servicios
**Propósito:** Desacoplar modelo de dominio de API
```java
// Evita exponer entidades directamente
CreateUsuarioRequest → Usuario (Entity) → UsuarioDto
```

### 6. Layered Architecture
**Capas:**
```
Controller → Service → Repository → Persistence
   ↓           ↓          ↓            ↓
  REST       Lógica    Abstracción   Datos
```

---

## 🔐 Seguridad y Configuración

### Seguridad

#### Problemáticas Actuales (⚠️ Crítico)
1. **Passwords en texto plano**
   - Ubicación: `users.json`
   - Riesgo: Violación OWASP A02:2021, GDPR
   - Solución: BCrypt hashing + salt
   - Esfuerzo: 6h

2. **Sin autenticación/autorización**
   - APIs públicas sin protección
   - Solución: Spring Security + JWT
   - Esfuerzo: 12h

3. **CORS básico**
   - Hardcoded origins en properties
   - Solución: Configuración por entorno

#### Mejoras Recomendadas
- ✅ HTTPS en producción
- ✅ Rate limiting
- ✅ Input sanitization (parcialmente con `@Valid`)
- ❌ API Gateway con autenticación centralizada
- ❌ Secrets management (Vault, AWS Secrets Manager)

### Configuración

**application.properties (usuario-service)**
```properties
# Server
server.port=8081
server.servlet.context-path=/api

# Persistencia
app.persistence.type=json
app.persistence.cache.enabled=true

# RabbitMQ
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=5672

# CORS
app.cors.allowed-origins=http://localhost:3000
```

---

## 📊 Deuda Técnica

### Resumen Ejecutivo

**Estado General:** ⚠️ Medio
**Deuda Total Estimada:** 60-80 horas
**Progreso:** 50% completado (reducido de 120-150h)

### Problemas Críticos (🔴)

| # | Problema | Impacto | Esfuerzo | Prioridad |
|---|----------|---------|----------|-----------|
| 1 | Passwords texto plano | Seguridad crítica | 6h | URGENTE |
| 2 | Logging primitivo (System.out) | Debugging imposible | 6h | Alta |
| 3 | Sin resiliencia RabbitMQ | Memory leaks, pérdida mensajes | 8h | Alta |

### Problemas Importantes (🟡)

| # | Problema | Impacto | Esfuerzo | Prioridad |
|---|----------|---------|----------|-----------|
| 4 | Field injection | Anti-pattern Spring | 3h | Media |
| 5 | Sin state machine pedidos | Transiciones inválidas | 4h | Media |
| 6 | Coverage usuario-service | Solo 50% | 6h | Media |
| 7 | Sin healthchecks Docker | Race conditions | 2h | Media |

### Mejoras Recomendadas (🟢)

- Frontend: React Query para caching (6h)
- Backend: Migración JSON → PostgreSQL (16h)
- Infra: Logs centralizados (ELK) (6h)
- Docs: Swagger/OpenAPI (2h)
- Monitoring: Prometheus + Grafana (8h)

---

## 🚀 Próximos Pasos

### Sprint 1 - Críticos (1 semana)
1. **Seguridad Passwords** (6h)
   - Implementar BCrypt
   - Migrar datos existentes
   - Tests de autenticación

2. **Logging Profesional** (6h)
   - Migrar a SLF4J + Logback
   - Configurar niveles por entorno
   - Añadir MDC para trazabilidad

3. **Refactoring Inyección** (3h)
   - Constructor injection en todos los componentes
   - Marcar dependencias como `final`

4. **Validaciones Completas** (4h)
   - Unicidad de email
   - State machine para pedidos
   - Custom validators

5. **Docker Healthchecks** (2h)
   - Spring Boot Actuator
   - Configurar depends_on: service_healthy

**Total Sprint 1:** 21h

### Sprint 2 - Arquitectura (1 semana)
1. **Resiliencia RabbitMQ** (8h)
   - Dead Letter Queue
   - Exponential backoff
   - Circuit breaker pattern

2. **Coverage Tests** (6h)
   - usuario-service: 50% → 70%
   - Tests adicionales de edge cases

3. **Documentación API** (4h)
   - Swagger/OpenAPI specs
   - Postman collections

4. **Secrets Management** (2h)
   - .env files
   - Docker secrets

**Total Sprint 2:** 20h

### Sprint 3 - Escalabilidad (Futuro)
- Migración a base de datos relacional
- API Gateway (Kong/Spring Cloud Gateway)
- Observabilidad (Prometheus + Grafana)
- CI/CD avanzado
- Kubernetes deployment

---

## 📚 Documentación Adicional

El proyecto incluye documentación detallada:

| Documento | Contenido |
|-----------|-----------|
| [README.md](README.md) | Guía de inicio rápido |
| [RABBITMQ_INTEGRATION.md](RABBITMQ_INTEGRATION.md) | Integración mensajería (260 líneas) |
| [TECHNICAL_DEBT_AUDIT.md](TECHNICAL_DEBT_AUDIT.md) | Auditoría técnica completa |
| [AI_WORKFLOW.md](AI_WORKFLOW.md) | Workflows de desarrollo |
| [CALIDAD.md](CALIDAD.md) | Estándares de calidad |
| [docs/TESTING_AND_CI.md](docs/TESTING_AND_CI.md) | Estrategia de testing |

---

## 🎓 Conclusiones

### Fortalezas del Proyecto
✅ **Arquitectura limpia:** Separación clara de responsabilidades  
✅ **Patrones de diseño:** Factory, Decorator, Strategy aplicados correctamente  
✅ **Testing robusto:** 70%+ coverage en pedido-service  
✅ **Mensajería asíncrona:** Integración RabbitMQ funcional  
✅ **Contenedorización:** Despliegue reproducible con Docker  
✅ **Stack moderno:** Java 21, Spring Boot 4, React 18  

### Áreas de Mejora
⚠️ **Seguridad:** Passwords sin hashear (crítico)  
⚠️ **Logging:** System.out en producción  
⚠️ **Persistencia:** JSON files no escalables  
⚠️ **Resiliencia:** Sin manejo de fallos RabbitMQ  
⚠️ **Observabilidad:** Sin métricas ni trazas  

### Recomendación Final
El proyecto tiene una **base sólida** con buenas prácticas arquitectónicas. Con 40-50 horas de trabajo enfocado en seguridad y resiliencia, puede estar **production-ready**. La migración a base de datos y observabilidad pueden ser fases posteriores.

---

**Contacto:** [Tu Información]  
**Última Actualización:** 17 de Febrero, 2026
