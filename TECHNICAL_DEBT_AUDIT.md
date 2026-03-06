# Auditoría de Deuda Técnica — Resumen Ejecutivo

**Fecha actualización:** 14 febrero 2026  
**Alcance:** Backend (Spring Boot + RabbitMQ) + Frontend (React + Vite + TypeScript) + Infraestructura (Docker)

---

## 📊 Estado General

| Componente | Estado | Test Coverage | Severidad | Esfuerzo Restante |
|------------|--------|---------------|-----------|----------|
| **Frontend** | ✅ Refactorizado | 80%+ (15 tests) | 🟢 Baja | 2-4h |
| **Backend - pedido-service** | ✅ Testeado | 70%+ (34 tests) | 🟡 Media | 8-12h |
| **Backend - usuario-service** | ⚠️ Parcial | 50%+ (20 tests) | 🔴 Alta | 12-16h |
| **Infraestructura** | ⚠️ Básica | N/A | 🟡 Media | 6-8h |

**Total deuda técnica restante:** ~60-80 horas (reducido de 120-150h)

---

## 🔴 Problemas Críticos (Acción inmediata requerida)

### 1. Passwords en texto plano (usuario-service)
- **Riesgo:** Violación OWASP, GDPR — exposición total de credenciales
- **Archivo:** `users.json` — passwords como "alice123", "bob123", "12345678"
- **Solución:** BCrypt + migración de datos existentes
- **Esfuerzo:** 6h | **Prioridad:** 🔴 CRÍTICA

### 2. ~~Enums incompatibles Frontend/Backend~~ ✅ RESUELTO
- ~~**Problema:** `TRAVELINGTOWAREHOUSE` (Backend) vs `TRAVELING_TO_WAREHOUSE` (Frontend)~~
- **Estado:** ✅ Corregido en `orders.json` — todos los enums usan snake_case
- **Completado:** 14 feb 2026

### 3. ~~Backend sin tests~~ ✅ PARCIALMENTE RESUELTO
- ~~**Problema:** 0 tests unitarios/integración en ambos servicios~~
- **Estado actual:**
  - ✅ **pedido-service:** 34 tests (8 integration + 26 unit) — 70%+ coverage
  - ✅ **usuario-service:** 20 tests (16 integration + 4 unit) — 50%+ coverage
- **Pendiente:** Aumentar coverage a 70%+ en usuario-service
- **Esfuerzo restante:** 6h | **Prioridad:** 🟡 Media

### 4. Logging primitivo (7 ocurrencias)
- **Problema:** `System.out.println` (5x) + `printStackTrace()` (2x) en producción
- **Ubicaciones:**
  - usuario-service: UserServiceProducer, UserServiceConsumer (3x)
  - pedido-service: OrderRepository (2x printStackTrace), UserServiceProducer, UserServiceConsumer (2x)
- **Impacto:** Sin niveles, sin contexto, debugging imposible
- **Solución:** Migrar a SLF4J + Logback
- **Esfuerzo:** 6h | **Prioridad:** 🔴 Alta

### 5. ~~Validaciones ausentes~~ ✅ PARCIALMENTE RESUELTO
- ~~**Problema:** Sin `@Valid`, permite emails duplicados, idUser negativos~~
- **Estado actual:**
  - ✅ `@Valid` implementado en CreateUsuarioRequest con Jakarta Bean Validation
  - ✅ Validaciones: email format, password strength (8+ chars, mayús/minús/números)
  - ✅ Strategy pattern implementado (Lenient + Strict validation)
  - ❌ Pendiente: Unicidad de email, estado transitions en pedidos
- **Esfuerzo restante:** 4h | **Prioridad:** 🟡 Media

---

## 🟡 Problemas Importantes (Media prioridad)

### 6. ~~Sin service layer en usuario-service~~ ✅ RESUELTO
- ~~**Problema:** Lógica de negocio en `@RestController` + `@SpringBootApplication` (6 endpoints)~~
- **Estado:** ✅ `UsuarioService` (@Service) implementado con SRP, interface IUsuarioService
- **Completado:** Fecha anterior (ya existía en el código base)

### 7. Field injection → Constructor injection
- **Problema:** 19 usos de `@Autowired` en fields (anti-pattern)
- **Ubicaciones:** Controllers, Services, Messaging components
- **Solución:** Migrar a constructor injection + final fields
- **Esfuerzo:** 3h | **Prioridad:** 🟡 Media

### 8. RabbitMQ sin resiliencia
- **Problema:** Sin DLQ, retry, circuit breaker — memory leaks en consumer
- **Solución:** Dead Letter Queue + exponential backoff + monitoring
- **Esfuerzo:** 8h | **Prioridad:** 🟡 Media

### 9. State machine ausente
- **Problema:** Permite transiciones inválidas (DELIVERED → PROCESSING)
- **Solución:** Map de transiciones permitidas
- **Esfuerzo:** 4h | **Prioridad:** 🟡 Media

### 10. Docker healthchecks ausentes
- **Problema:** Sin health endpoints, race conditions en startup
- **Solución:** `depends_on: service_healthy` + Spring Actuator /health
- **Esfuerzo:** 2h | **Prioridad:** 🟡 Media

---

## 🟢 Mejoras recomendadas (Baja prioridad)

### Frontend
- ~~Deprecated shims (IUser.ts, Order.ts)~~ ✅ Mantenidos para compatibilidad con @JsonProperty
- ~~Enums frontend/backend~~ ✅ Alineados
- ~~Endpoints actualizados~~ ✅ Migrados a /api/v1/usuarios
- Console.error en tests — mockear (2h)
- Env vars sin validación — validar en boot (1h)
- React Query para caching (6h)

### Backend
- ~~Field injection → Constructor injection~~ ⚠️ Pendiente (3h)
- Timeout hardcoded → configurable (1h)
- Duplicados de usuarios → validar unicidad email (1h)
- JSON → DB migration (16h cuando sea necesario)

### Infra
- ~~Docker ENV vars~~ ✅ Implementado para frontend (VITE_APIUSER/ORDER)
- ~~Workflows actualizados~~ ✅ Endpoints corregidos en ci-docker-smoke.yml, ci-integration-services.yml
- ~~Vitest CI/CD~~ ✅ vitest.integration.config.ts creado
- Secrets management (.env + Vault) (4h)
- Logs centralizados (ELK / Loki) (6h)
- Prometheus + Grafana (8h)

### Docs
- README por servicio (2h)
- Swagger/OpenAPI (2h)
- Diagramas Mermaid (3h)

---

## 🎯 Plan de acción
### Sprint 1 — Críti actualizado (2 sprints)
cos restantes (1 semana, ~22h)
1. Hashear passwords BCrypt (6h)
2. ~~Alinear enums Frontend/Backend~~ ✅ COMPLETADO
3. Migrar logging a SLF4J (6h)
4. ~~Añadir validaciones @Valid~~ ✅ COMPLETADO
5. Completar validaciones (unicidad email, state transitions) (4h)
6. Field injection → Constructor (3h)
7. Docker healthchecks (2h)
8. Tests adicionales usuario-service (1h)

### Sprint 2 — Arquitectura y resiliencia (1 semana, ~18h)
9. ~~Extraer UserService~~ ✅ YA EXISTE
10. State machine (4h)
11. DLQ + retry RabbitMQ (8h)
12. Swagger docs (4h)
13. ~~CI/CD actualización~~ ✅ Workflows corregidos
14. Secrets management básico (2h)

---

## 📊 Métricas actuales

| Métrica | Frontend | usuario-service | pedido-service |
|---------|----------|-----------------|----------------|
| Tests | ✅ 15 (80%+) | ✅ 20 (50%+) | ✅ 34 (70%+) |
| Linting | ✅ ESLint | ❌ Ninguno | ❌ Ninguno |
| Seguridad | ✅ OK | 🔴 Passwords plaintext | 🟡 OK |
| Service Layer | ✅ OK | ✅ Implementado | ✅ Implementado |
| Validaciones | ✅ OK | ✅ @Valid + Strategy | ⚠️ Parcial |
| Logging | ✅ OK | 🔴 System.out (3x) | 🔴 System.out (4x) |
| Deuda técnica | 🟢 3% | 🟡 20% | 🟢 15% |

**Progreso desde última auditoría:**
- ✅ Tests backend: 0% → 60% average
- ✅ Enums alineados: 100%
- ✅ Service layer: 100%
- ✅ Validaciones básicas: 100%
- ✅ Frontend endpoints: 100%
- ❌ Passwords plaintext: 0%
- ❌ Logging profesional: 0%

**Esfuerzo total restante:** ~60-80 horas (reducción de 40-70h desde última auditoría)

---

## ✅ Criterios de éxito

### Completados ✅
- [x] ~~Enums alineados Frontend/Backend~~ (14 feb 2026)
- [x] ~~Tests backend implementados~~ (usuario: 20 tests, pedido: 34 tests)
- [x] ~~Service layer implementado~~ (UsuarioService, OrderService)
- [x] ~~Validaciones @Valid en endpoints~~ (CreateUsuarioRequest + Strategy)
- [x] ~~Endpoints frontend actualizados~~ (/api/v1/usuarios)
- [x] ~~Context-path duplicación corregida~~ (controller /v1, context /api)
- [x] ~~Workflows GitHub Actions actualizados~~ (ci-docker-smoke, ci-integration-services)
- [x] ~~Docker ENV vars frontend~~ (VITE_APIUSER, VITE_APIORDER)
- [x] ~~Vitest integración CI/CD~~ (vitest.integration.config.ts)

### Pendientes ❌
- [ ] Passwords hasheadas (BCrypt)
- [ ] ≥70% test coverage usuario-service (actualmente 50%+)
- [ ] 0 System.out/printStackTrace (SLF4J) — actualmente 7 ocurrencias
- [ ] Field injection → Constructor injection (19 @Autowired)
- [ ] Healthchecks configurados
- [ ] State machine implementada
- [ ] DLQ + retry RabbitMQ
- [ ] Swagger/OpenAPI docsigurados
- [ ] CI/CD funcionando
