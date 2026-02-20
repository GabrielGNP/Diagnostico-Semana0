♻️ REFACTOR: HU-ORD-01 - Listado de pedidos activos desde PostgreSQL

## 🔄 Fase TDD: GREEN → REFACTOR

### Resumen
Refactorización completa del código de producción y tests para HU-ORD-01,
mejorando legibilidad, mantenibilidad y aplicando principios SOLID y Clean Code.

### Archivos Modificados

#### 1. OrderController.java
- ✅ Endpoint `/order/all` ahora usa `findAllActiveOrders()` 
- ✅ Javadoc completo con referencias a FR y HU
- ✅ Documentación actualizada sobre soft-delete pattern

#### 2. OrderService.java
- ✅ Método `listAllOrders()` marcado como @Deprecated
- ✅ Javadoc completo en `findAllActiveOrders()` con:
  - Detalles de implementación
  - Reglas de negocio (HU-ORD-01)
  - Requisitos de performance (NFR-ORD-01-01)
- ✅ Clarificación de usos apropiados para cada método

#### 3. OrderServiceHuOrd01Test.java
- ✅ Métodos helper agregados:
  - `createTestOrder()`: Reduce duplicación de código
  - `setupMapperMock()`: Centraliza configuración de mocks
- ✅ Tests refactorizados con mejoras:
  - Assertions más expresivas y descriptivas
  - Verificación de interacciones con mocks
  - Documentación de mejoras aplicadas
  - DisplayNames actualizados: 🔴 RED → ♻️ REFACTOR

### Principios Aplicados

#### Clean Code
- DRY: Eliminación de código duplicado
- Nombres descriptivos
- Comentarios significativos
- Funciones con responsabilidad única

#### SOLID
- Single Responsibility Principle
- Open/Closed Principle (deprecation strategy)

#### Test Best Practices
- AAA Pattern (Arrange-Act-Assert)
- Test Isolation
- Descriptive Assertions
- Mock Verification

### Cumplimiento de HU-ORD-01

✅ FR-ORD-01-01: Recuperar pedidos de PostgreSQL
✅ FR-ORD-01-02: Incluir todos los campos requeridos
✅ FR-ORD-01-03: Retornar HTTP 200 OK
✅ CA-01: Listado exitoso con pedidos activos
✅ CA-02: Listado vacío sin pedidos activos
✅ Edge Case: Base de datos vacía

### Tests Status
✅ Todos los tests siguen en GREEN
✅ Sin cambios en funcionalidad
✅ Cobertura mantenida

### Notas
- Ver HU-ORD-01_REFACTOR_REPORT.md para detalles completos
- Ready for production 🚀

---
Co-authored-by: GitHub Copilot <copilot@github.com>
