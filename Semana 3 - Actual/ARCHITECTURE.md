# ANÁLISIS DE ARQUITECTURA Y DOCUMENTACIÓN DE PUNTOS DE DOLOR

**Fecha del Documento:** 24 de Febrero, 2026  
**Alcance:** Análisis completo del sistema basado en HANDOVER_REPORT.md, API_AUDIT_REPORT.md y auditoría del workspace  
**Estado:** Reporte de Diagnóstico

---

## 1. Resumen Ejecutivo

Este documento proporciona un análisis arquitectónico detallado del proyecto **Diagnostico-Semana0**, identificando puntos de dolor críticos ("dolores") que impactan la mantenibilidad, escalabilidad, seguridad y confiabilidad operacional.

El sistema consiste en dos microservicios Spring Boot (`usuario-service`, `pedido-service`), un frontend React + Vite, mensajería asíncrona vía RabbitMQ, y PostgreSQL para persistencia.

### Calificación General de Salud: ⚠️ RIESGO MEDIO

| Área | Calificación | Severidad |
|------|--------------|-----------|
| **Seguridad** | 🔴 Crítico | Contraseñas en texto plano |
| **Persistencia** | 🟡 Medio | Confusión modo dual (JSON/PostgreSQL) |
| **Observabilidad** | 🔴 Alto | Sin logging estructurado |
| **Diseño de API** | 🟡 Medio | Inconsistencias REST (adherencia parcial) |
| **Resiliencia** | 🔴 Alto | Sin DLQ, sin circuit breakers |
| **Testing** | 🟢 Aceptable | 50-70% cobertura |

### Nivel de Adherencia REST

- **`usuario-service`**: ✅ Buenas prácticas (DTOs, controlador delgado, `GlobalExceptionHandler`)
- **`pedido-service`**: ⚠️ Funcional pero con inconsistencias en códigos HTTP, nomenclatura y manejo de errores

---

## 2. Visión General de la Arquitectura Actual

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CAPA FRONTEND                               │
│                         React 18.3 + Vite + TypeScript                  │
│                              Puerto: 3000                                │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │ HTTP/REST
              ┌───────────────────┴───────────────────┐
              ▼                                       ▼
┌─────────────────────────┐              ┌─────────────────────────┐
│    usuario-service      │◄────────────►│    pedido-service       │
│    Puerto: 8083 (ext)   │   RabbitMQ   │    Puerto: 8082 (ext)   │
│    Puerto: 8081 (int)   │              │    Puerto: 8080 (int)   │
│    /api/v1/usuarios     │              │    /order/*             │
└───────────┬─────────────┘              └───────────┬─────────────┘
            │                                        │
            └──────────────┬─────────────────────────┘
                           ▼
              ┌─────────────────────────┐
              │       PostgreSQL        │
              │       Puerto: 5432      │
              │                         │
              │  users_db | orders_db   │
              └─────────────────────────┘
                           │
              ┌─────────────────────────┐
              │       RabbitMQ          │
              │    AMQP: 5672           │
              │    Management: 15672    │
              │                         │
              │  user-exchange (direct) │
              │  user-request-queue     │
              │  user-response-queue    │
              └─────────────────────────┘
```

---

## 3. Puntos de Dolor Identificados ("Dolores")

### 🔴 CRÍTICO — Acción Inmediata Requerida

---

#### DOLOR-001: Contraseñas Almacenadas en Texto Plano

**Severidad:** 🔴 CRÍTICO  
**Impacto:** Brecha de seguridad, violación OWASP, potencial incumplimiento GDPR  
**Ubicación:** `Backend/usuario-service/data/users.json`, `users.json`

**Estado Actual:**
```json
{
  "password": "alice123",
  "password": "bob123"
}
```

**Evaluación de Riesgo:**
- Exposición completa de credenciales si los archivos de datos son comprometidos
- Viola OWASP Top 10 (A02:2021 – Fallas Criptográficas)
- Sin algoritmo de hash de contraseñas implementado

**Solución Recomendada:**
- Implementar hash BCrypt con factor de fuerza ≥12
- Migrar contraseñas existentes a través de flujo de reset forzado
- Añadir validación de política de contraseñas (mín 8 chars, mayúsculas, minúsculas, números)

**Esfuerzo Estimado:** 6-8 horas

---

#### DOLOR-002: Logging Primitivo con System.out/printStackTrace

**Severidad:** 🔴 ALTO  
**Impacto:** Cero observabilidad, debugging imposible en producción, riesgo de seguridad (stack traces expuestos)  
**Ocurrencias:** 7 instancias identificadas

**Componentes Afectados:**
| Servicio | Clase | Cantidad |
|----------|-------|----------|
| usuario-service | UserServiceProducer | 1 |
| usuario-service | UserServiceConsumer | 2 |
| pedido-service | OrderRepository | 2 (`printStackTrace`) |
| pedido-service | UserServiceProducer | 1 |
| pedido-service | UserServiceConsumer | 1 |

**Anti-patrón Actual:**
```java
System.out.println("Procesando solicitud...");
e.printStackTrace();
```

**Consecuencias:**
- Sin niveles de log (DEBUG, INFO, WARN, ERROR)
- Sin timestamps ni IDs de correlación
- Sin formato estructurado para agregación de logs
- Riesgo de seguridad: stack traces pueden exponer rutas internas

**Solución Recomendada:**
- Migrar a SLF4J + Logback
- Implementar logging estructurado en JSON
- Añadir propagación de ID de correlación entre servicios
- Configurar niveles de log por ambiente

**Esfuerzo Estimado:** 6 horas

---

#### DOLOR-003: RabbitMQ Sin Patrones de Resiliencia

**Severidad:** 🔴 ALTO  
**Impacto:** Pérdida de mensajes, fugas de memoria, fallas en cascada  
**Ubicación:** `RabbitMQConfig.java` en ambos servicios

**Características de Resiliencia Faltantes:**
| Característica | Estado | Riesgo |
|----------------|--------|--------|
| Dead Letter Queue (DLQ) | ❌ Faltante | Mensajes perdidos en falla |
| Retry con backoff | ❌ Faltante | Reintentos rápidos infinitos en errores transitorios |
| Circuit breaker | ❌ Faltante | Fallas en cascada |
| TTL de mensajes | ❌ Faltante | Agotamiento de memoria de cola |
| Monitoreo de salud | ❌ Faltante | Fallas silenciosas |

**Comportamiento Actual de Timeout:**
```java
// OrderService.java
USER_REQUEST_TIMEOUT = 3000ms // Hardcodeado, sin retry
```

**Consecuencias:**
- Mensajes fallidos se descartan silenciosamente
- Sin visibilidad de fallas de procesamiento
- La memoria puede crecer sin límites si el consumidor es lento
- Timeout de 3 segundos muy corto para arranques en frío

**Solución Recomendada:**
1. Configurar DLQ para ambas colas
2. Implementar retry con backoff exponencial (3 intentos)
3. Añadir patrón circuit breaker con Resilience4j
4. Establecer TTL de mensajes y límites de cola
5. Hacer timeout configurable vía `application.properties`

**Esfuerzo Estimado:** 8-12 horas

---

### 🟡 MEDIO — Debe Abordarse Pronto

---

#### DOLOR-004: Confusión de Modo Dual de Persistencia (JSON vs PostgreSQL)

**Severidad:** 🟡 MEDIO  
**Impacto:** Confusión de desarrolladores, inconsistencia de datos, errores de despliegue  
**Ubicación:** `docker-compose.yml`, `Backend/*/data/*.json`, copilot-instructions.md

**Documentación Contradictoria Actual:**
- `docker-compose.yml` configura PostgreSQL con scripts de inicialización
- `HANDOVER_REPORT.md` indica: "La persistencia demo está respaldada por archivos JSON"
- `copilot-instructions.md` indica: "PostgreSQL es la única fuente de verdad"
- `MIGRATION_ENABLED=false` deshabilita migraciones de base de datos por defecto

**Matriz de Confusión:**
| Modo | Fuente de Datos | MIGRATION_ENABLED | Estado |
|------|-----------------|-------------------|--------|
| Desarrollo | Archivos JSON | false | Por defecto |
| Docker Compose | PostgreSQL | configurable | Disponible |
| Producción | PostgreSQL | true | Previsto |

**Consecuencias:**
- Desarrolladores editan JSON pensando que afecta al sistema en ejecución
- Desajustes de ambiente entre dev/staging/prod
- Sin ruta clara para migración de datos

**Solución Recomendada:**
1. Remover archivos JSON de la ruta de runtime (mantener solo para fixtures de test)
2. Por defecto `MIGRATION_ENABLED=true` en docker-compose
3. Añadir documentación clara sobre modos de persistencia
4. Crear scripts de migración para datos semilla

**Esfuerzo Estimado:** 4-6 horas

---

#### DOLOR-005: Inconsistencias de API REST

**Severidad:** 🟡 MEDIO  
**Impacto:** Pobre ergonomía de API, confusión de clientes, comportamiento no estándar  
**Ubicación:** `OrderController.java`, `UsuarioController.java`

##### 📂 Análisis Detallado por Servicio

---

###### **pedido-service** (controller base: `/order`)

| Endpoint | Problema Detectado | Impacto | Severidad |
|----------|-------------------|---------|-----------|
| `POST /order/add` | Retorna `200 OK` en creación en vez de `201 Created`. No incluye `Location` header. | Consumidores no pueden diferenciar entre creación y respuesta normal; rompe expectativas REST. | 🟠 Medio |
| `POST /order/add` | Verbo en URL (`add`) | No es REST idiomático. Debería ser `POST /orders`. | 🟠 Medio |
| `DELETE /order/{id}` | Retorna `200 OK` vacío; usa `IllegalArgumentException` para not-found. | `204 No Content` es más apropiado. Excepciones deberían mapearse a `404`. | 🟢 Mejora |
| `GET /order/{id}` | Correcto retorno `404` cuando no existe. | Sin problemas graves. Mantener DTOs. | 🟢 OK |
| `GET /order/{id}/with-user-info` | Captura `Exception`, escribe en `System.err` y retorna `500` sin body estructurado. | Logging inconsistente, falta formato uniforme para errores, dificulta trazabilidad y parsing por clientes. | 🟠 Medio |
| `GET /order/user/{idUser}` | Convención `/user/{idUser}` funcional pero no REST idiomática. | Usar `/orders?userId={id}` o `/users/{id}/orders` para coherencia. | 🟢 Mejora |
| `GET /order/all` | Sufijo `/all` redundante. | Debería ser simplemente `GET /orders`. | 🟢 Mejora |
| `PATCH /order/{id}` | Uso apropiado de `PATCH` para cambio de estado; valida `state` y retorna `400` si falta. | Documentar contrato del body. Considerar `PUT` vs `PATCH`. | 🟢 Mejora |

**Problema Estructural Crítico:** `pedido-service` carece de `@ControllerAdvice` para manejo global de excepciones.

---

###### **usuario-service** (controller base: `/v1/usuarios`)

| Endpoint | Problema Detectado | Impacto | Severidad |
|----------|-------------------|---------|-----------|
| **Observación Global** | Capa de Controller delgada, usa DTOs, validación JSR-380, existe `GlobalExceptionHandler` que mapea correctamente `404`, `409`, `400`, `500` con cuerpos `ErrorResponse`. | Buen cumplimiento de responsabilidades. | 🟢 OK |
| **Inconsistencia de ruta** | `@RequestMapping` en `/v1/usuarios` pero constante `API_PATH` es `/api/v1/usuarios` (incluye `/api`). | Logs y documentación muestran rutas diferentes; aumenta fricción para integradores. | 🟠 Medio |
| `POST /v1/usuarios` | Retorna `201 Created` correctamente, pero NO establece `Location` header. | No cumple completamente buenas prácticas REST. | 🟢 Mejora |
| `PUT /v1/usuarios/{id}` | Buen uso de verbo `PUT` para reemplazo completo. | OK | 🟢 OK |
| `PATCH /v1/usuarios/{id}` | Buen uso de verbo `PATCH` para actualización parcial. | OK | 🟢 OK |
| `DELETE /v1/usuarios/{id}` | Retorna `204 No Content` correctamente. Errores mapeados a `404` por `GlobalExceptionHandler`. | OK | 🟢 OK |

---

##### 🔎 Hallazgos Transversales

| Hallazgo | Descripción | Recomendación |
|----------|-------------|---------------|
| **Manejo global de errores** | `usuario-service` tiene `GlobalExceptionHandler` con formatos consistentes. `pedido-service` carece de equivalente. | Añadir `@ControllerAdvice` a `pedido-service`. |
| **Códigos de estado en creación** | `pedido-service` retorna `200` en `POST`. | Normalizar a `201 Created` + `Location` header. |
| **Nomenclatura REST** | Rutas con verbos (`/add`) y sufijos redundantes (`/all`). | Preferir rutas plurales (`/orders`, `/users`) sin verbos. |
| **Logging y trazabilidad** | Uso de `System.err` y `println`. | Usar logger y correlación de request IDs. |
| **Consistencia en constantes** | `API_PATH` vs `@RequestMapping` discordantes. | Unificar constantes de ruta. |
| **Errores y exposición** | Posible retorno de stacktraces al cliente. | Retornar mensajes genéricos; registrar detalles en servidor. |

---

##### Solución Recomendada

1. **`pedido-service`:** Implementar `GlobalExceptionHandler` (`@ControllerAdvice`) con manejo de excepciones específicas (`OrderNotFoundException` → `404`)
2. **`pedido-service`:** Cambiar `POST /order/add` para retornar `201 Created` + header `Location: /order/{id}`
3. **Ambos servicios:** Normalizar endpoints a sustantivos plurales (`/orders`, `/usuarios`)
4. **`usuario-service`:** Unificar constante `API_PATH` con `@RequestMapping`
5. **Ambos servicios:** Añadir header `Location` en creación de recursos

**Esfuerzo Estimado:** 4-6 horas

---

#### DOLOR-006: Anti-patrón de Inyección de Campo

**Severidad:** 🟡 MEDIO  
**Impacto:** Código no testeable, dependencias ocultas, riesgos de null pointer  
**Ocurrencias:** 19 inyecciones de campo `@Autowired`

**Anti-patrón Actual:**
```java
@Autowired
private UserRepository userRepository;

@Autowired
private RabbitTemplate rabbitTemplate;
```

**Problemas:**
- No se pueden usar campos final (inmutabilidad)
- Más difícil escribir tests unitarios con mocks
- Oculta dependencias de la clase
- Requiere contenedor Spring para instanciación

**Patrón Recomendado:**
```java
private final UserRepository userRepository;
private final RabbitTemplate rabbitTemplate;

public UserService(UserRepository userRepository, RabbitTemplate rabbitTemplate) {
    this.userRepository = userRepository;
    this.rabbitTemplate = rabbitTemplate;
}
```

**Esfuerzo Estimado:** 3-4 horas

---

#### DOLOR-007: Máquina de Estados Faltante para Transiciones de Pedidos

**Severidad:** 🟡 MEDIO  
**Impacto:** Estados de negocio inválidos, corrupción de datos  
**Ubicación:** `OrderService.java`, `State.java`

**Enum de Estado Actual:**
```
PROCESSING → TRAVELING_TO_WAREHOUSE → DELIVERED
```

**Problema:** Sin validación de reglas de transición. El sistema permite:
- `DELIVERED → PROCESSING` (inválido)
- `TRAVELING_TO_WAREHOUSE → PROCESSING` (inválido)

**Solución Recomendada:**
```java
Map<State, Set<State>> TRANSICIONES_VALIDAS = Map.of(
    State.PROCESSING, Set.of(State.TRAVELING_TO_WAREHOUSE),
    State.TRAVELING_TO_WAREHOUSE, Set.of(State.DELIVERED),
    State.DELIVERED, Collections.emptySet()
);
```

**Esfuerzo Estimado:** 4 horas

---

### 🟢 BAJO — Mejoras de Calidad

---

#### DOLOR-008: Health Checks de Docker Faltantes

**Severidad:** 🟢 BAJO  
**Impacto:** Condiciones de carrera en arranque, falsos positivos en orquestación  
**Ubicación:** `docker-compose.yml`

**Estado Actual:**
- PostgreSQL tiene health check ✅
- RabbitMQ NO tiene health check ❌
- Servicios arrancan antes de que RabbitMQ esté listo

**Solución Recomendada:**
```yaml
rabbitmq:
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
    interval: 5s
    timeout: 10s
    retries: 5
```

**Esfuerzo Estimado:** 2 horas

---

#### DOLOR-009: Documentación de API Faltante (OpenAPI/Swagger)

**Severidad:** 🟢 BAJO  
**Impacto:** Fricción de integración, contratos no documentados  
**Estado:** Sin Swagger/OpenAPI configurado

**Solución Recomendada:**
- Añadir dependencia `springdoc-openapi-starter-webmvc-ui`
- Anotar endpoints con anotaciones OpenAPI
- Generar especificación para equipo frontend

**Esfuerzo Estimado:** 4 horas

---

#### DOLOR-010: Cobertura de Tests Por Debajo del Objetivo

**Severidad:** 🟢 BAJO  
**Impacto:** Riesgo de regresión, miedo a refactorizar  

**Cobertura Actual:**
| Servicio | Cobertura | Objetivo |
|----------|-----------|----------|
| usuario-service | 50%+ | 70% |
| pedido-service | 70%+ | 80% |
| Frontend | 80%+ | ✅ Cumplido |

**Solución Recomendada:**
- Añadir tests de casos límite para lógica de validación
- Añadir tests de integración para flujos de RabbitMQ
- Añadir tests de escenarios negativos

**Esfuerzo Estimado:** 6-8 horas

---

## 4. Mapa de Dependencias (Análisis de Acoplamiento)

```
┌─────────────────────────────────────────────────────────────────┐
│                      RIESGOS DE ACOPLAMIENTO                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  pedido-service ──────┬─────── usuario-service                  │
│         │             │              │                           │
│         │    DTOs Compartidos        │                           │
│         │    UserRequest             │                           │
│         │    UserResponse            │                           │
│         │             │              │                           │
│         └─────────────┴──────────────┘                           │
│                       │                                          │
│            ⚠️ Riesgo de cambios disruptivos                      │
│            Cambios en estructura de DTO requieren                │
│            despliegue sincronizado de ambos servicios            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Puntos de Acoplamiento Fuerte:**
1. DTOs `UserRequest` / `UserResponse` deben ser idénticos entre servicios
2. Nombres de colas RabbitMQ hardcodeados en ambos servicios
3. Clientes de servicio del frontend dependen de rutas de endpoint exactas

---

## 5. Resumen de Deuda Técnica

| ID | Punto de Dolor | Severidad | Esfuerzo | Prioridad |
|----|----------------|-----------|----------|-----------|
| DOLOR-001 | Contraseñas en texto plano | 🔴 Crítico | 6-8h | P0 |
| DOLOR-002 | Logging con System.out | 🔴 Alto | 6h | P0 |
| DOLOR-003 | RabbitMQ sin resiliencia | 🔴 Alto | 8-12h | P0 |
| DOLOR-004 | Confusión de persistencia | 🟡 Medio | 4-6h | P1 |
| DOLOR-005 | Inconsistencias REST | 🟡 Medio | 4-6h | P1 |
| DOLOR-006 | Inyección de campo | 🟡 Medio | 3-4h | P1 |
| DOLOR-007 | Sin máquina de estados | 🟡 Medio | 4h | P1 |
| DOLOR-008 | Healthchecks faltantes | 🟢 Bajo | 2h | P2 |
| DOLOR-009 | Sin docs de API | 🟢 Bajo | 4h | P2 |
| DOLOR-010 | Cobertura de tests | 🟢 Bajo | 6-8h | P2 |

**Remediación Total Estimada:** 47-62 horas

---

## 6. Matriz de Evaluación de Riesgos

```
              IMPACTO
         Bajo   Medio   Alto
        ┌──────┬──────┬──────┐
  Alto  │      │      │ D001 │
        │      │      │ D002 │
PROB.   │      │      │ D003 │
        ├──────┼──────┼──────┤
  Medio │      │ D004 │      │
        │      │ D005 │      │
        │      │ D006 │      │
        │      │ D007 │      │
        ├──────┼──────┼──────┤
  Bajo  │ D008 │ D009 │      │
        │ D010 │      │      │
        └──────┴──────┴──────┘
```

---

## 7. Prioridad de Corrección (Sugerida)

### 🔴 Alta (Arreglos recomendados inmediatamente)

| Acción | Servicio | Severidad | Esfuerzo |
|--------|----------|-----------|----------|
| Implementar hash BCrypt para passwords | usuario-service | 🔴 Crítico | 6-8h |
| Migrar a SLF4J + Logback | Ambos | 🔴 Alto | 6h |
| Implementar `@ControllerAdvice` y remover `System.err` | pedido-service | 🟠 Medio | 3h |
| Cambiar `POST /order/add` → `201 Created` + `Location` | pedido-service | 🟠 Medio | 2h |
| Configurar DLQ y retry en RabbitMQ | Ambos | 🔴 Alto | 8-12h |

### 🟡 Media

| Acción | Servicio | Severidad | Esfuerzo |
|--------|----------|-----------|----------|
| Unificar rutas y constantes (`API_PATH` vs `@RequestMapping`) | usuario-service | 🟠 Medio | 1h |
| Normalizar paths a plural (`/orders`, `/usuarios`) | pedido-service | 🟢 Mejora | 2h |
| Eliminar sufijos `/all` y `/add` | pedido-service | 🟢 Mejora | 1h |
| Implementar máquina de estados | pedido-service | 🟡 Medio | 4h |

### 🟢 Baja

| Acción | Servicio | Severidad | Esfuerzo |
|--------|----------|-----------|----------|
| Añadir healthcheck de RabbitMQ | Infra | 🟢 Bajo | 2h |
| Generar documentación OpenAPI | Ambos | 🟢 Bajo | 4h |
| Aumentar cobertura de tests | Ambos | 🟢 Bajo | 6-8h |

---

## 8. Plan de Acción Recomendado

### Fase 1: Seguridad Crítica y Observabilidad (Semana 1)
1. ✅ Implementar hash de contraseñas BCrypt (DOLOR-001)
2. ✅ Migrar a logging estructurado SLF4J (DOLOR-002)
3. ✅ Añadir `GlobalExceptionHandler` a pedido-service (DOLOR-005)
4. ✅ Corregir códigos HTTP en pedido-service (DOLOR-005)

### Fase 2: Resiliencia y Consistencia (Semana 2)
5. ✅ Configurar DLQ y retry de RabbitMQ (DOLOR-003)
6. ✅ Clarificar estrategia de persistencia (DOLOR-004)
7. ✅ Implementar máquina de estados (DOLOR-007)
8. ✅ Refactorizar a inyección por constructor (DOLOR-006)

### Fase 3: Calidad de Código y Documentación (Semana 3)
9. ✅ Normalizar nomenclatura REST (DOLOR-005)
10. ✅ Añadir healthchecks de Docker (DOLOR-008)
11. ✅ Generar documentación OpenAPI (DOLOR-009)
12. ✅ Aumentar cobertura de tests (DOLOR-010)

---

## 9. Conclusión

El proyecto demuestra una base sólida de microservicios con separación apropiada de responsabilidades. Sin embargo, **vulnerabilidades de seguridad críticas** (contraseñas en texto plano) y **brechas de observabilidad** (logging primitivo) deben abordarse inmediatamente antes de cualquier despliegue a producción.

**Riesgos técnicos principales:**
- Consumidores pueden interpretar incorrectamente resultados (`200` vs `201`/`404`/`204`)
- Errores sin formato consistente dificultan el manejo de fallos en clientes

Los problemas de prioridad media alrededor de consistencia de API y patrones de resiliencia deben seguir para asegurar un sistema mantenible y escalable.

**Bloqueadores inmediatos para producción:**
- 🔴 DOLOR-001: Seguridad de contraseñas
- 🔴 DOLOR-002: Infraestructura de logging
- 🔴 DOLOR-003: Resiliencia de mensajería
- 🟠 DOLOR-005: Inconsistencias REST (particularmente en `pedido-service`)

---

## 10. Análisis Arquitectónico: Estado Actual vs Clean Architecture

Esta sección analiza la estructura interna de cada microservicio y contrasta teóricamente los beneficios de migrar hacia una **Clean Architecture** (también conocida como Arquitectura Hexagonal o Ports & Adapters).

---

### 10.1 Estructura Actual de `usuario-service`

```
usuario-service/
├── src/main/java/com/example/usuarioservice/
│   ├── config/                    # Configuración Spring
│   ├── controller/                # Capa de presentación REST
│   │   └── UsuarioController.java
│   ├── dto/                       # Data Transfer Objects
│   │   ├── CreateUsuarioRequest.java
│   │   ├── UpdateUsuarioRequest.java
│   │   └── UsuarioResponse.java
│   ├── entity/                    # Entidades JPA
│   │   └── UserEntity.java
│   ├── exception/                 # Excepciones custom + GlobalExceptionHandler
│   ├── mapper/                    # Mappers (entidad ↔ modelo ↔ DTO)
│   ├── messaging/                 # RabbitMQ consumers/producers
│   ├── model/                     # Modelo de dominio
│   │   └── User.java
│   ├── persistence/               # Abstracción de persistencia
│   │   ├── IUserPersistence.java              # ✅ Interfaz (Port)
│   │   ├── UserJpaPersistence.java            # Adapter JPA
│   │   └── CachedUserPersistenceDecorator.java # Decorator pattern
│   ├── repository/                # Spring Data JPA
│   │   └── UserJpaRepository.java
│   ├── service/                   # Lógica de negocio
│   │   ├── IUsuarioService.java   # ✅ Interfaz de casos de uso
│   │   ├── UsuarioService.java    # Implementación
│   │   └── UserRepository.java    # (legacy)
│   └── validation/                # Strategy pattern para validación
│       ├── IValidationStrategy.java
│       ├── LenientValidationStrategy.java
│       ├── StrictValidationStrategy.java
│       └── ValidationContext.java
```

#### Evaluación de `usuario-service`

| Aspecto | Estado | Observaciones |
|---------|--------|---------------|
| **Separación de capas** | 🟢 Buena | Controller → Service → Persistence bien diferenciados |
| **Inversión de dependencias** | 🟢 Implementado | `IUserPersistence`, `IUsuarioService`, `IValidationStrategy` |
| **Patrones de diseño** | 🟢 Múltiples | Strategy (validación), Decorator (cache), Factory implícito |
| **Modelo de dominio** | 🟡 Parcial | `User.java` existe pero mezcla concerns con DTOs |
| **Independencia del framework** | 🟡 Parcial | Service depende de DTOs de Spring (`@Valid`) |
| **Testabilidad** | 🟢 Buena | Interfaces permiten mocking fácil |

**Fortalezas detectadas:**
- Uso de interfaces (`IUserPersistence`, `IUsuarioService`) que actúan como "puertos"
- Pattern Strategy para validación configurable
- Pattern Decorator para caching de persistencia
- GlobalExceptionHandler centralizado
- Constructor injection con `@RequiredArgsConstructor`

**Debilidades detectadas:**
- El modelo `User` no es un Rich Domain Model (anémico)
- No hay capa de Use Cases explícita separada del Service
- Los DTOs de entrada (`CreateUsuarioRequest`) llegan hasta el Service
- Falta separación clara entre dominio e infraestructura

---

### 10.2 Estructura Actual de `pedido-service`

```
pedido-service/
├── src/main/java/com/example/pedidoservice/
│   ├── config/                    # Configuración Spring/RabbitMQ
│   ├── controller/                # Capa REST
│   │   └── OrderController.java
│   ├── dto/                       # Data Transfer Objects
│   │   ├── OrderDto.java
│   │   └── OrderWithUserDto.java
│   ├── mapper/                    # MapStruct mappers
│   │   └── OrderMapper.java
│   ├── messaging/                 # RabbitMQ integración
│   │   ├── RabbitMQConfig.java
│   │   ├── UserRequest.java
│   │   ├── UserResponse.java
│   │   ├── UserServiceConsumer.java
│   │   └── UserServiceProducer.java
│   ├── model/                     # Entidades de dominio/JPA mezcladas
│   │   ├── Order.java             # ⚠️ Entidad JPA = Modelo dominio
│   │   └── State.java
│   ├── repository/                # Acceso a datos
│   │   ├── OrderJpaRepository.java  # Spring Data JPA
│   │   └── OrderRepository.java     # (legacy JSON-based)
│   └── service/                   # Lógica de negocio
│       └── OrderService.java      # ⚠️ Sin interfaz
```

#### Evaluación de `pedido-service`

| Aspecto | Estado | Observaciones |
|---------|--------|---------------|
| **Separación de capas** | 🟡 Básica | Controller → Service → Repository, pero acopladas |
| **Inversión de dependencias** | 🔴 Ausente | `OrderService` no implementa interfaz |
| **Patrones de diseño** | 🔴 Mínimos | Sin Strategy, sin Decorator, sin Factory |
| **Modelo de dominio** | 🔴 Anémico | `Order.java` es entidad JPA pura, sin comportamiento |
| **Independencia del framework** | 🔴 Alta dependencia | Service usa `@Autowired`, `@Transactional` directamente |
| **Testabilidad** | 🟡 Limitada | Sin interfaces → requiere contexto Spring para tests |

**Debilidades críticas detectadas:**
- `Order.java` es simultáneamente entidad JPA y modelo de dominio (violación SRP)
- `OrderService` usa field injection (`@Autowired`) en 4 dependencias
- No existe `IOrderService` — acoplamiento directo Controller→ServiceImpl
- Validación hardcodeada dentro del Service (no reutilizable)
- Mezcla de responsabilidades: Service hace validación + negocio + orquestación de mensajería
- Sin GlobalExceptionHandler — manejo de errores inconsistente

---

### 10.3 Comparación Teórica: Arquitectura Actual vs Clean Architecture

#### Diagrama de Clean Architecture (Robert C. Martin)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FRAMEWORKS & DRIVERS                            │
│   (Web, UI, DB, External Interfaces, Devices, etc.)                     │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │                    INTERFACE ADAPTERS                            │   │
│   │   (Controllers, Gateways, Presenters, Repositories impl)        │   │
│   │   ┌─────────────────────────────────────────────────────────┐   │   │
│   │   │               APPLICATION BUSINESS RULES                │   │   │
│   │   │               (Use Cases / Interactors)                 │   │   │
│   │   │   ┌─────────────────────────────────────────────────┐   │   │   │
│   │   │   │          ENTERPRISE BUSINESS RULES              │   │   │   │
│   │   │   │          (Entities / Domain Model)              │   │   │   │
│   │   │   └─────────────────────────────────────────────────┘   │   │   │
│   │   └─────────────────────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘

        La REGLA DE DEPENDENCIA: Las capas internas NO conocen
        las capas externas. Las dependencias apuntan hacia adentro.
```

#### Mapeo de Capas: Actual → Clean Architecture

| Capa Clean Architecture | usuario-service | pedido-service |
|-------------------------|-----------------|----------------|
| **Entities (Domain)** | `model/User.java` (parcial) | ❌ `model/Order.java` es entidad JPA |
| **Use Cases** | `service/IUsuarioService.java` | ❌ No existe interfaz |
| **Interface Adapters (Controllers)** | `controller/UsuarioController.java` ✅ | `controller/OrderController.java` ✅ |
| **Interface Adapters (Gateways)** | `persistence/IUserPersistence.java` ✅ | ❌ Acoplado a `OrderJpaRepository` |
| **Frameworks & Drivers** | `repository/`, `config/`, `messaging/` | `repository/`, `config/`, `messaging/` |

---

### 10.4 Beneficios Teóricos de Migrar a Clean Architecture

#### 1. **Independencia del Framework**

| Aspecto | Estado Actual | Con Clean Architecture |
|---------|---------------|------------------------|
| Cambiar de Spring Boot a Quarkus | 🔴 Requiere reescribir Services | 🟢 Solo adaptar capa externa |
| Cambiar de PostgreSQL a MongoDB | 🟡 Modificar Services + Repositories | 🟢 Solo nuevo Adapter de persistencia |
| Cambiar de RabbitMQ a Kafka | 🔴 Modificar Services directamente | 🟢 Solo nuevo Adapter de mensajería |

**Beneficio:** El núcleo de negocio (Entities + Use Cases) permanece intacto ante cambios tecnológicos.

---

#### 2. **Testabilidad Mejorada**

| Tipo de Test | Estado Actual | Con Clean Architecture |
|--------------|---------------|------------------------|
| **Unit Tests de Dominio** | 🟡 Requiere mocks de Spring | 🟢 POJOs puros, sin dependencias |
| **Unit Tests de Use Cases** | 🟡 Mocks de repositorios Spring | 🟢 Mocks de puertos (interfaces) |
| **Integration Tests** | 🟡 Contexto Spring completo | 🟢 Solo adapters involucrados |

**Beneficio:** Tests más rápidos, aislados y mantenibles. Cobertura del dominio sin necesidad de levantar Spring.

---

#### 3. **Separación Clara de Responsabilidades**

```
ESTADO ACTUAL (pedido-service):
┌─────────────────────────────────────────┐
│           OrderService.java             │
│  ┌───────────────────────────────────┐  │
│  │ • Validación de DTOs             │  │
│  │ • Lógica de negocio              │  │
│  │ • Orquestación de mensajería     │  │
│  │ • Transacciones (@Transactional) │  │
│  │ • Mapeo entidad ↔ DTO            │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
          ⚠️ VIOLACIÓN DE SRP

CON CLEAN ARCHITECTURE:
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   Use Case:      │  │   Domain:        │  │   Adapter:       │
│ CreateOrderUC    │  │ Order (Entity)   │  │ OrderJpaAdapter  │
│                  │  │  - validate()    │  │                  │
│ • Orquestar      │  │  - changeState() │  │ • Persistir      │
│ • Invocar domain │  │  - isActive()    │  │ • Traducir       │
└──────────────────┘  └──────────────────┘  └──────────────────┘
          ✅ SINGLE RESPONSIBILITY
```

---

#### 4. **Regla de Dependencia Explícita**

**Problema actual en `pedido-service`:**
```java
// OrderService.java - DEPENDENCIAS HACIA AFUERA (violación)
@Autowired
private OrderJpaRepository orderJpaRepository;  // Framework específico

@Autowired
private RabbitTemplate rabbitTemplate;          // Framework específico
```

**Con Clean Architecture:**
```java
// CreateOrderUseCase.java - DEPENDENCIAS HACIA ADENTRO (correcto)
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;     // Puerto (interfaz)
    private final UserGateway userGateway;             // Puerto (interfaz)
    private final EventPublisher eventPublisher;       // Puerto (interfaz)
    
    // Constructor injection - sin @Autowired
    public CreateOrderUseCase(OrderRepository repo, UserGateway gateway, EventPublisher pub) {
        this.orderRepository = repo;
        this.userGateway = gateway;
        this.eventPublisher = pub;
    }
}
```

---

#### 5. **Modelo de Dominio Rico vs Anémico**

**Estado actual (Modelo Anémico):**
```java
// Order.java - Solo datos, sin comportamiento
@Entity
public class Order {
    private Integer id;
    private String name;
    private State state;
    // Solo getters/setters - ANEMIC!
}

// OrderService.java - Toda la lógica aquí
public void changeState(Order order, State newState) {
    // Validación de transición aquí
    if (order.getState() == State.DELIVERED) {
        throw new IllegalStateException("Cannot change state of delivered order");
    }
    order.setState(newState);
}
```

**Con Clean Architecture (Modelo Rico):**
```java
// Order.java - Entidad de dominio con comportamiento
public class Order {
    private OrderId id;
    private OrderName name;
    private OrderState state;
    
    // COMPORTAMIENTO EN EL DOMINIO
    public void transitionTo(OrderState newState) {
        if (!this.state.canTransitionTo(newState)) {
            throw new InvalidStateTransitionException(this.state, newState);
        }
        this.state = newState;
        // Puede emitir Domain Events
    }
    
    public boolean isDelivered() {
        return this.state == OrderState.DELIVERED;
    }
}
```

---

### 10.5 Propuesta de Estructura Clean Architecture

#### Estructura Propuesta para `pedido-service`

```
pedido-service/
├── src/main/java/com/example/pedidoservice/
│   │
│   ├── domain/                           # 🔵 NÚCLEO - Sin dependencias externas
│   │   ├── model/
│   │   │   ├── Order.java                # Entidad de dominio rica
│   │   │   ├── OrderId.java              # Value Object
│   │   │   ├── OrderState.java           # Value Object con transiciones
│   │   │   └── OrderCreatedEvent.java    # Domain Event
│   │   ├── repository/
│   │   │   └── OrderRepository.java      # Puerto (interfaz)
│   │   ├── service/
│   │   │   └── OrderDomainService.java   # Lógica que no cabe en entidad
│   │   └── exception/
│   │       └── InvalidStateTransitionException.java
│   │
│   ├── application/                       # 🟢 USE CASES - Orquestación
│   │   ├── port/
│   │   │   ├── in/                        # Puertos de entrada
│   │   │   │   ├── CreateOrderUseCase.java
│   │   │   │   ├── GetOrderUseCase.java
│   │   │   │   └── ChangeOrderStateUseCase.java
│   │   │   └── out/                       # Puertos de salida
│   │   │       ├── OrderPersistencePort.java
│   │   │       ├── UserServicePort.java
│   │   │       └── EventPublisherPort.java
│   │   ├── service/
│   │   │   ├── CreateOrderService.java   # Implementa CreateOrderUseCase
│   │   │   └── GetOrderService.java
│   │   └── dto/                           # DTOs de aplicación
│   │       ├── CreateOrderCommand.java
│   │       └── OrderResult.java
│   │
│   └── infrastructure/                    # 🟠 ADAPTERS - Frameworks
│       ├── adapter/
│       │   ├── in/
│       │   │   └── web/
│       │   │       ├── OrderController.java
│       │   │       └── OrderRestMapper.java
│       │   └── out/
│       │       ├── persistence/
│       │       │   ├── OrderJpaAdapter.java      # Implementa OrderPersistencePort
│       │       │   ├── OrderJpaRepository.java   # Spring Data
│       │       │   ├── OrderJpaEntity.java       # Entidad JPA
│       │       │   └── OrderPersistenceMapper.java
│       │       ├── messaging/
│       │       │   ├── RabbitUserServiceAdapter.java  # Implementa UserServicePort
│       │       │   └── RabbitEventPublisher.java      # Implementa EventPublisherPort
│       │       └── rest/
│       │           └── UserServiceRestAdapter.java    # Alternativa HTTP
│       └── config/
│           ├── BeanConfiguration.java     # Wiring de dependencias
│           └── RabbitMQConfiguration.java
```

---

### 10.6 Tabla Comparativa de Impacto

| Métrica | Estado Actual | Con Clean Architecture | Mejora |
|---------|---------------|------------------------|--------|
| **Acoplamiento** | Alto (Service↔JPA↔RabbitMQ) | Bajo (solo interfaces) | 🟢 60% reducción |
| **Cobertura tests unitarios** | 50-70% | 85%+ potencial | 🟢 +15-35% |
| **Tiempo ejecución tests** | ~30s (requiere Spring) | ~5s (POJOs puros) | 🟢 6x más rápido |
| **Complejidad ciclomática** | Alta (Services monolíticos) | Baja (responsabilidades separadas) | 🟢 Significativa |
| **Costo de cambio de DB** | 8-16h | 2-4h | 🟢 4x menos esfuerzo |
| **Costo de cambio de mensajería** | 8-12h | 2-3h | 🟢 4x menos esfuerzo |
| **Curva de aprendizaje** | Baja | Media-Alta | 🟡 Inversión inicial |
| **Líneas de código** | ~2,500 | ~3,500 | 🟡 +40% (boilerplate) |

---

### 10.7 Recomendación de Migración

#### Prioridad de Migración por Servicio

| Servicio | Prioridad | Justificación |
|----------|-----------|---------------|
| `pedido-service` | 🔴 Alta | Más deuda técnica, sin interfaces, modelo anémico |
| `usuario-service` | 🟡 Media | Ya tiene algunas abstracciones, migración incremental posible |

#### Estrategia de Migración Incremental

**Fase 1 (2-3 días): Extraer Puertos**
1. Crear interfaz `IOrderService` en `pedido-service`
2. Crear interfaz `OrderPersistencePort`
3. Refactorizar a constructor injection

**Fase 2 (3-4 días): Separar Dominio**
1. Crear paquete `domain/` con entidades ricas
2. Mover validación de transiciones a `OrderState`
3. Separar `OrderJpaEntity` de `Order` (dominio)

**Fase 3 (2-3 días): Crear Use Cases**
1. Extraer `CreateOrderUseCase` del Service
2. Extraer `ChangeOrderStateUseCase`
3. Implementar puertos de salida

**Esfuerzo total estimado:** 40-60 horas (ambos servicios)

---

### 10.8 Conclusión del Análisis Arquitectónico

| Servicio | Madurez Actual | Deuda Arquitectónica |
|----------|----------------|---------------------|
| `usuario-service` | 🟢 70% Clean | Baja — ya tiene abstracciones clave |
| `pedido-service` | 🔴 30% Clean | Alta — requiere refactorización significativa |

**Veredicto:** La migración a Clean Architecture es **recomendable** especialmente para `pedido-service`, donde los beneficios en testabilidad, mantenibilidad y reducción de acoplamiento justifican la inversión. Para `usuario-service`, la migración puede ser incremental aprovechando las abstracciones existentes.

**Beneficio clave:** Un dominio bien encapsulado permitirá evolucionar los microservicios independientemente del framework, facilitando futuras migraciones tecnológicas y mejorando drásticamente la cobertura de tests.

---

*Documento generado por IRISH - Agente de Ingeniería de Requerimientos*  
*Fuentes: HANDOVER_REPORT.md, API_AUDIT_REPORT.md, análisis de código fuente*  
*Última actualización: 24 de Febrero, 2026*

## 11 Justificación de dejar el módelo de MVC vs Clean Architecrture

### 11.1. Postura a favor de conservar el esquema MVC

Teniendo en cuenta que el proyecto en su estado actual tiene una arquitectura de Modelo Vista Controlador, y que actualmente tiene pocas funcionalidades, se aboga por conservar el esquema actual **MVC**, esto en virtud de poder cumplir con uno de los 7 principios del Testing **No es posible realizar testing de software exhaustivo**. Bajo esa premisa, parte del equipo de desarrollo plantea la posibilidad de preservar el esquema actual y una vez hayan más requerimientos del cliente migrar hacia el Clean Architecture.

Esto también es procedente en virtud de poder asegurar entregas eficientes y oportunas

### 11.2. Postura en contra de conservar el esquema MVC