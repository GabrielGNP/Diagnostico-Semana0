# Auditoría de Deuda Técnica — Resumen Ejecutivo

**Fecha:** 13 febrero 2026  
**Alcance:** Backend (Spring Boot + RabbitMQ) + Frontend (React + Vite + TypeScript) + Infraestructura (Docker)

---

## 📊 Estado General

| Componente | Estado | Test Coverage | Severidad | Esfuerzo |
|------------|--------|---------------|-----------|----------|
| **Frontend** | ✅ Refactorizado (5 PRs) | 80%+ (15 tests) | 🟢 Baja | 2-4h |
| **Backend - pedido-service** | ⚠️ Funcional | 0% ❌ | 🔴 Alta | 16-24h |
| **Backend - usuario-service** | ⚠️ Funcional | 0% ❌ | 🔴 Alta | 20-30h |
| **Infraestructura** | ⚠️ Básica | N/A | 🟡 Media | 8-12h |

**Total deuda técnica:** ~120-150 horas

---

## 🔴 Problemas Críticos (Acción inmediata requerida)

### 1. Passwords en texto plano (usuario-service)
- **Riesgo:** Violación OWASP, GDPR — exposición total de credenciales
- **Archivo:** `users.json` — passwords como "alice123", "bob123"
- **Solución:** BCrypt + migración de datos existentes
- **Esfuerzo:** 6h | **Prioridad:** 🔴 CRÍTICA

### 2. Enums incompatibles Frontend/Backend
- **Problema:** `TRAVELINGTOWAREHOUSE` (Backend) vs `TRAVELING_TO_WAREHOUSE` (Frontend)
- **Impacto:** Rompe integración — requests/responses fallan
- **Solución:** Alinear naming (usar snake_case)
- **Esfuerzo:** 3h | **Prioridad:** 🔴 CRÍTICA

### 3. Backend sin tests
- **Problema:** 0 tests unitarios/integración en ambos servicios
- **Impacto:** Imposible refactorizar con confianza, regresiones no detectadas
- **Solución:** JUnit + Mockito (OrderService, UserRepository, Controllers, RabbitMQ)
- **Esfuerzo:** 26h (pedido 16h + usuario 10h) | **Prioridad:** 🔴 CRÍTICA

### 4. Logging primitivo (11 ocurrencias)
- **Problema:** `System.out.println` + `printStackTrace()` en producción
- **Impacto:** Sin niveles, sin contexto, debugging imposible
- **Solución:** Migrar a SLF4J + Logback
- **Esfuerzo:** 8h | **Prioridad:** 🔴 Alta

### 5. Validaciones ausentes
- **Problema:** Sin `@Valid`, permite emails duplicados, idUser negativos, transiciones de estado inválidas
- **Impacto:** Data corrupta, bugs silenciosos
- **Solución:** Jakarta Bean Validation + DTOs
- **Esfuerzo:** 10h | **Prioridad:** 🔴 Alta

---

## 🟡 Problemas Importantes (Media prioridad)

### 6. Sin service layer en usuario-service
- **Problema:** Lógica de negocio en `@RestController` + `@SpringBootApplication` (6 endpoints)
- **Solución:** Extraer `UserService` (@Service) con SRP
- **Esfuerzo:** 6h | **Prioridad:** 🟡 Media‑Alta

### 7. RabbitMQ sin resiliencia
- **Problema:** Sin DLQ, retry, circuit breaker — memory leaks en consumer
- **Solución:** Dead Letter Queue + exponential backoff + monitoring
- **Esfuerzo:** 8h | **Prioridad:** 🟡 Media

### 8. State machine ausente
- **Problema:** Permite transiciones inválidas (DELIVERED → PROCESSING)
- **Solución:** Map de transiciones permitidas
- **Esfuerzo:** 4h | **Prioridad:** 🟡 Media

### 9. Docker healthchecks ausentes
- **Problema:** Race conditions en startup (services arrancan antes que RabbitMQ)
- **Solución:** `depends_on: service_healthy` + actuator/health
- **Esfuerzo:** 2h | **Prioridad:** 🟡 Media

---

## 🟢 Mejoras recomendadas (Baja prioridad)

### Frontend
- Deprecated shims (IUser.ts, Order.ts) — eliminar (1h)
- Console.error en tests — mockear (2h)
- Env vars sin validación — validar en boot (1h)
- React Query para caching (6h)

### Backend
- Field injection → Constructor injection (3h)
- Timeout hardcoded → configurable (1h)
- Duplicados de usuarios → validar unicidad email (1h)
- JSON → DB migration (16h cuando sea necesario)

### Infra
- Secrets management (.env + Vault) (4h)
- CI/CD pipeline (GitHub Actions) (6h)
- Logs centralizados (ELK / Loki) (6h)
- Prometheus + Grafana (8h)

### Docs
- README por servicio (2h)
- Swagger/OpenAPI (2h)
- Diagramas Mermaid (3h)

---

## 🎯 Plan de acción (3 sprints)

### Sprint 1 — Críticos (1 semana, ~25h)
1. Hashear passwords BCrypt (6h)
2. Alinear enums Frontend/Backend (3h)
3. Migrar logging a SLF4J (8h)
4. Añadir validaciones @Valid (6h)
5. Docker healthchecks (2h)

### Sprint 2 — Testing (1.5 semanas, ~44h)
6. Tests unitarios pedido-service (16h)
7. Tests unitarios usuario-service (14h)
8. Integration tests RabbitMQ (8h)
9. CI/CD básico (6h)

### Sprint 3 — Arquitectura (1 semana, ~22h)
10. Extraer UserService (6h)
11. State machine (4h)
12. DLQ + retry RabbitMQ (8h)
13. Swagger docs (4h)

---

## 📊 Métricas actuales

| Métrica | Frontend | Backend |
|---------|----------|---------|
| Tests | ✅ 15 (80%+) | ❌ 0 (0%) |
| Linting | ✅ ESLint | ❌ Ninguno |
| Seguridad | ✅ OK | 🔴 Passwords plaintext |
| Deuda técnica | 🟢 5% | 🔴 35-40% |

**Esfuerzo total:** ~120-150 horas

---

## ✅ Criterios de éxito

- [ ] Passwords hasheadas (BCrypt)
- [ ] ≥70% test coverage Backend
- [ ] 0 System.out/printStackTrace (SLF4J)
- [ ] Enums alineados Frontend/Backend
- [ ] Validaciones @Valid en endpoints
- [ ] Service layer implementado
- [ ] Healthchecks configurados
- [ ] CI/CD funcionando
