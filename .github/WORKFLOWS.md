# GitHub Actions CI/CD Workflows

Este proyecto utiliza GitHub Actions para ejecutar tests automáticos en tres niveles: unitarios, integración de componentes e integración de servicios.

## 📁 Estructura de Workflows

### Tests Unitarios
- `ci-unit-pedido.yml` - Tests unitarios del servicio de pedidos
- `ci-unit-usuario.yml` - Tests unitarios del servicio de usuarios
- `ci-unit-frontend.yml` - Tests unitarios del frontend

### Tests de Integración de Componentes
- `ci-integration-component-pedido.yml` - Tests de integración del servicio de pedidos
- `ci-integration-component-usuario.yml` - Tests de integración del servicio de usuarios
- `ci-integration-component-frontend.yml` - Tests de integración del frontend

### Tests de Integración de Servicios (E2E)
- `ci-integration-services.yml` - Tests end-to-end del sistema completo

### Pipeline Completo
- `ci-full-pipeline.yml` - Orquesta todos los tests en 3 stages secuenciales

### Creación de Issues
- `create-issue-on-failure.yml` - Workflow reusable para crear issues cuando fallen tests

## 🔄 Flujo de Ejecución

### Workflows Individuales
Cada workflow individual se ejecuta cuando:
- Haces push a `main`, `master` o `develop`
- Creas un PR hacia esas ramas
- Modificas archivos del servicio específico

### Pipeline Completo
El pipeline completo (`ci-full-pipeline.yml`) ejecuta en 3 stages:

```
Stage 1: Unit Tests (paralelo)
  ├── unit-pedido
  ├── unit-usuario
  └── unit-frontend
       ↓
Stage 2: Component Integration (paralelo)
  ├── integration-pedido (necesita unit-pedido)
  ├── integration-usuario (necesita unit-usuario)
  └── integration-frontend (necesita unit-frontend)
       ↓
Stage 3: Service Integration
  └── integration-services (necesita todos los anteriores)
```

## 🐛 Sistema de Issues Automáticos

Cuando un test falla en un workflow individual, se crea automáticamente un Issue con detalles del error.

### Workflows que Crean Issues
Solo los **workflows individuales** crean Issues cuando fallan:
- Tests unitarios (ci-unit-*.yml)
- Tests de integración de componentes (ci-integration-component-*.yml)
- Tests de integración de servicios (ci-integration-services.yml)

**Nota**: El workflow padre (`ci-full-pipeline.yml`) NO crea Issues para evitar duplicados. Solo los workflows específicos que fallan generan Issues.

### Contenido de los Issues
- **Título**: `[emoji] [Tipo de Test] Failed: [servicio]`
- **Contenido**:
  - Información del servicio que falló
  - Links a los logs completos del workflow
  - Información del commit, branch y autor
  - Comandos específicos para reproducir el test localmente
  - Pasos sugeridos para resolver el problema
- **Labels**: `bug`, `ci-failure`, `[tipo-test]`, `automated`

### Tipos de Tests
Los Issues se etiquetan según el tipo de test:
- `unit-tests` - 🧪 Tests unitarios
- `integration-tests` - 🔧 Tests de integración de componentes
- `e2e-tests` - 🌐 Tests de integración de servicios

### Extracción Automática de Errores
El sistema extrae automáticamente las líneas relevantes de los logs que contienen:
- Errores de pruebas (FAILED, FAIL, ✖)
- Excepciones (Exception:, Error:)
- Aserciones fallidas (AssertionError)
- Contexto (5 líneas antes y después del error)

Esto permite identificar rápidamente el problema sin necesidad de revisar todos los logs.

## 📊 Ejemplo de Issue Creado

### Pipeline Completo
```
## 🔴 CI Pipeline Failed

**Workflow:** `Full CI Pipeline`
**Run Number:** #42
**Commit:** `abc1234`
**Branch:** `develop`
**Triggered by:** @usuario
**Event:** `push`

### 📊 Failed Test Summary

#### ❌ Unit Tests Failed
- **pedido** - [View logs](...)

#### ❌ Service Integration (E2E) Tests Failed
- **E2E Service Integration** - [View logs](...)

### 🔍 Next Steps
1. Review the workflow logs
2. Check the specific failed jobs listed above
3. Fix the failing tests
4. Push the fixes to trigger a new CI run

### 📝 Job Status Details
| Job | Status |
|-----|--------|
| unit-pedido | ❌ failure |
| unit-usuario | ✅ success |
| unit-frontend | ✅ success |
| integration-pedido | ⚠️ skipped |
| integration-usuario | ✅ success |
| integration-frontend | ✅ success |
| integration-services | ❌ failure |
```

## 🎯 Comandos para Ejecutar Tests Localmente

### Tests Unitarios

**Backend (Java/Maven):**
```bash
cd Backend/pedido-service
mvn test

cd Backend/usuario-service
mvn test
```

**Frontend (Node/Vitest):**
```bash
cd Frontend
npm test
```

### Tests de Integración de Componentes

**Backend (Java/Maven):**
```bash
cd Backend/pedido-service
mvn verify

cd Backend/usuario-service
mvn verify
```

**Frontend (Node/Vitest):**
```bash
cd Frontend
npm run test:integration
```

### Tests de Integración de Servicios (E2E)

```bash
# 1. Levantar servicios
docker compose up -d

# 2. Ejecutar tests E2E
cd tests/e2e
npm install
npm test

# 3. Apagar servicios
docker compose down -v
```

## 🛠️ Configuración de Permisos

Los workflows necesitan permisos para crear Issues. Esto está configurado en cada workflow con:

```yaml
permissions:
  issues: write
```

Si los Issues no se crean, verifica que tu repositorio tenga habilitados los permisos de escritura para workflows en:
`Settings` → `Actions` → `General` → `Workflow permissions`

## 🔧 Personalización

### Agregar Nuevo Servicio
1. Crea `ci-unit-[nuevo-servicio].yml` siguiendo el patrón de los existentes
2. Crea `ci-integration-component-[nuevo-servicio].yml`
3. Agrega los jobs al `ci-full-pipeline.yml`
4. Agrega job de creación de issue al final

### Modificar Condiciones de Ejecución
Edita el `paths:` en el `on:` de cada workflow para cambiar qué archivos trigger el workflow.

### Desactivar Creación de Issues
Comenta o elimina el job `create-issue-on-failure` de los workflows.
