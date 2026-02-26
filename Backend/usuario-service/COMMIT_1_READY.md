# 🚀 Commit 1: Configuración Inicial y Refactoring Controller (Mapeo)

## Archivos a Incluir en el Commit

### Documentación (FASE 1)
```bash
git add Backend/usuario-service/REFACTORING_PLAN_COVERAGE.md
git add Backend/usuario-service/REFACTORING_EXAMPLES.md
git add Backend/usuario-service/REFACTORING_CHECKLIST.md
git add Backend/usuario-service/COVERAGE_BASELINE.md
git add Backend/usuario-service/REFACTORING_PROGRESS.md
```

### Configuración (FASE 1)
```bash
git add Backend/usuario-service/pom.xml
```

### Código Refactorizado (FASE 2 - Paso 2.1)
```bash
git add Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java
```

## Mensaje de Commit Sugerido

### Opción 1: Commit Detallado
```bash
git commit -m "feat(test): Configure JaCoCo and refactor controller DTO mapping

FASE 1: Preparación y Setup
- Add JaCoCo plugin 0.8.11 to pom.xml with 40% minimum coverage target
- Create COVERAGE_BASELINE.md documenting initial state (7 test files, ~40% coverage)
- Add comprehensive refactoring plan (REFACTORING_PLAN_COVERAGE.md)
- Add code examples documentation (REFACTORING_EXAMPLES.md)
- Add step-by-step checklist (REFACTORING_CHECKLIST.md)
- Establish goal: 85% coverage in 4 weeks (8 phases)

FASE 2: Controller Refactoring (Step 2.1)
- Extract DTO mapping to private methods in UsuarioController
  * Add mapToResponses() for collection mapping
  * Add mapToResponse() for single entity mapping
- Refactor all 5 endpoints to use centralized mapping
- Improve code maintainability (DRY principle)
- Add comprehensive javadoc to private methods

Benefits:
- Centralized mapping logic (easier to maintain)
- Improved testability (isolated concerns)
- Prepared for comprehensive unit testing
- Better code clarity and readability

Next steps: Create unit tests for all controller endpoints"
```

### Opción 2: Commit Conciso
```bash
git commit -m "feat(test): Setup JaCoCo and refactor controller mapping

- Configure JaCoCo plugin (baseline: 40%, target: 85%)
- Create refactoring documentation and baseline report
- Extract DTO mapping to private methods in UsuarioController
- Improve code maintainability and testability

Phase 1 (Setup) and Phase 2 Step 2.1 (Controller refactoring) complete."
```

## Comando Completo

```bash
# Añadir todos los archivos
git add Backend/usuario-service/REFACTORING_PLAN_COVERAGE.md \
        Backend/usuario-service/REFACTORING_EXAMPLES.md \
        Backend/usuario-service/REFACTORING_CHECKLIST.md \
        Backend/usuario-service/COVERAGE_BASELINE.md \
        Backend/usuario-service/REFACTORING_PROGRESS.md \
        Backend/usuario-service/pom.xml \
        Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java

# Commit
git commit -m "feat(test): Setup JaCoCo and refactor controller mapping

- Configure JaCoCo plugin (baseline: 40%, target: 85%)
- Create refactoring documentation and baseline report
- Extract DTO mapping to private methods in UsuarioController
- Improve code maintainability and testability

Phase 1 (Setup) and Phase 2 Step 2.1 (Controller refactoring) complete."
```

## Verificación Antes del Commit

✅ **Checklist:**
- [ ] Todos los archivos documentación están creados
- [ ] pom.xml tiene JaCoCo configurado
- [ ] UsuarioController tiene métodos privados de mapeo
- [ ] No hay errores de compilación
- [ ] Tests existentes deben seguir pasando (verificar después del commit)

## Después del Commit

```bash
# Ver el commit
git log -1 --stat

# Ver cambios del commit
git show HEAD

# Verificar que tests siguen pasando (requiere Maven instalado)
# mvn test
```

## Próximo Commit

**FASE 2 - Paso 2.2:** Crear tests para POST endpoint
- Archivo nuevo: `UsuarioControllerCrearTest.java`
- Tests: 201 Created, 400 Bad Request, 409 Conflict

---

**Fecha:** 25 de febrero de 2026  
**Estado:** ✅ Listo para commit

