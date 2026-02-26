# Baseline de Cobertura - usuario-service
**Fecha:** 25 de febrero de 2026  
**Objetivo:** Documentar cobertura inicial antes de refactorización

---

## 📊 Estado Inicial del Proyecto

### Archivos de Test Existentes
1. `HUUSR03_CreateUserTests.java` - Tests de creación de usuario
2. `componentIntegrationTests.java` - Tests de integración de componentes
3. `UsuarioServiceObtenerTodosTest.java` - Tests de servicio (obtenerTodos)
4. `UserJpaPersistenceIntegrationTest.java` - Tests de persistencia JPA
5. `UserRepositoryFindAllActiveTest.java` - Tests de repositorio (findAllActive)
6. `UserRepositoryTest.java` - Tests generales de repositorio
7. `UsuarioControllerObtenerTodosTest.java` - Tests de controller (obtenerTodos)

**Total:** 7 archivos de test

---

## 🎯 Cobertura Inicial (Estimada)

### Por Capa

| Capa | Archivos | Métodos | Cobertura Estimada | Notas |
|------|----------|---------|-------------------|-------|
| **Controller** | 1 | 6 endpoints | ~30% | Solo 1 endpoint con tests (GET all) |
| **Service** | 1 | 8 métodos | ~40% | Tests básicos de obtenerTodos y crear |
| **Persistence** | 1 | 7 métodos | ~50% | Solo tests de integración |
| **Repository** | 1 | Varios | ~90% | Bien cubierto con tests |
| **DTOs** | 3 | Factory methods | ~5% | Sin tests específicos |
| **Validation** | 4 | Estrategias | ~10% | Sin tests específicos |
| **Exception** | 4 | Handlers | ~0% | Sin tests |
| **Mapper** | 1 | Conversión | ~0% | Sin tests |

**Cobertura Global Estimada:** ~35-40%

---

## 📝 Métodos Sin Cobertura

### Controller (UsuarioController.java)
- ❌ `crear()` - POST /v1/usuarios
- ❌ `obtenerPorIdentificador()` - GET /v1/usuarios/{identificador}
- ❌ `actualizar()` - PUT /v1/usuarios/{id}
- ❌ `actualizarParcial()` - PATCH /v1/usuarios/{id}
- ❌ `eliminar()` - DELETE /v1/usuarios/{id}
- ✅ `obtenerTodos()` - GET /v1/usuarios (CUBIERTO)

### Service (UsuarioService.java)
- ❌ `obtenerPorIdentificador()` - Casos edge (email/ID/inválido)
- ❌ `obtenerPorId()` - Casos edge
- ❌ `obtenerPorEmail()` - Casos edge
- ❌ `actualizar()` - Cobertura completa
- ❌ `actualizarParcial()` - Cobertura completa
- ❌ `eliminar()` - Cobertura completa
- ✅ `obtenerTodos()` - Básico cubierto
- ✅ `crear()` - Básico cubierto (HUUSR03)

### Persistence (UserJpaPersistence.java)
- ❌ Tests unitarios con mocks
- ❌ `partialUpdate()` - Casos específicos
- ✅ Tests de integración existentes

### DTOs
- ❌ `CreateUsuarioRequest` - Validaciones Jakarta
- ❌ `UpdateUsuarioRequest` - Validaciones Jakarta
- ❌ `UsuarioResponse.from()` - Factory method y null-safety
- ❌ Serialización/deserialización JSON

### Validation
- ❌ `ValidationContext` - Selección de estrategias
- ❌ `LenientValidationStrategy` - Reglas específicas
- ❌ `StrictValidationStrategy` - Reglas específicas
- ❌ Integración con Service

### Exception Handling
- ❌ `GlobalExceptionHandler` - Todos los handlers
- ❌ `UsuarioNotFoundException`
- ❌ `UsuarioYaExisteException`
- ❌ `ValidationException`

---

## 🎯 Metas de Refactorización

### Cobertura Objetivo por Capa

| Capa | Actual | Objetivo | Incremento |
|------|--------|----------|------------|
| Controller | ~30% | 90% | +60% |
| Service | ~40% | 85% | +45% |
| Persistence | ~50% | 80% | +30% |
| Repository | ~90% | 90% | Mantener |
| DTOs | ~5% | 95% | +90% |
| Validation | ~10% | 85% | +75% |
| Exception | ~0% | 90% | +90% |
| **TOTAL** | **~40%** | **85%** | **+45%** |

### Tests a Crear

**Estimación:** ~25 archivos de test nuevos

- Controller: +5 archivos
- Service: +5 archivos
- Persistence: +2 archivos
- DTOs: +4 archivos
- Validation: +4 archivos
- Exception: +2 archivos
- E2E: +3 archivos

---

## 🔧 Configuración

### JaCoCo Plugin
- ✅ Agregado a pom.xml
- ✅ Versión: 0.8.11
- ✅ Meta mínima inicial: 40% (línea)
- 🎯 Meta final: 85%

### Comandos

```bash
# Ejecutar tests y generar reporte
mvn clean test jacoco:report

# Ver reporte
open target/site/jacoco/index.html

# Verificar meta mínima
mvn jacoco:check
```

---

## 📅 Timeline

- **Inicio:** 25 de febrero de 2026
- **Duración estimada:** 4 semanas
- **Fin estimado:** 25 de marzo de 2026

---

## ✅ Checklist FASE 1

- [x] Documentar archivos de test existentes
- [x] Estimar cobertura inicial por capa
- [x] Identificar métodos sin cobertura
- [x] Configurar JaCoCo en pom.xml
- [x] Establecer metas de cobertura
- [ ] Ejecutar primer reporte de cobertura (requiere mvn)
- [ ] Crear rama de refactoring

---

**Estado:** ✅ Documentación completada  
**Siguiente paso:** Generar reporte JaCoCo y crear rama de trabajo

