# Resumen de Tests Unitarios - OrderService

**Fecha de Creación:** 13 de febrero de 2026  
**Última Actualización:** 13 de febrero de 2026  
**Total de Tests Implementados:** 35  
**Ubicación del Test:** `src/test/java/com/example/pedidoservice/unitTest.java`

---

## Overview

Se han implementado **35 tests unitarios** completos para cubrir todos los 7 métodos del servicio `OrderService`. Utilizamos la estructura de **clases anidadas con `@Nested`** para organizar los tests de forma clara y jerárquica. Todos los tests siguen el patrón **AAA (Arrange-Act-Assert)** y utilizan **Mockito** para aislar completamente el servicio bajo prueba.

---

## Estructura de Tests

La clase `OrderServiceTest` contiene 7 clases anidadas, una por cada método:

```
OrderServiceTest
├── CreateOrderTests (5 tests)
├── DeleteOrderTests (4 tests)
├── ChangeStateOrderTests (5 tests)
├── ListOrdersByIdUserTests (5 tests)
├── GetOrderWithUserInfoTests (6 tests)
├── ListAllOrdersTests (5 tests)
└── ShowOrderByIdTests (5 tests)
```

---

## 1. CreateOrderTests (5 tests)

### `testCreateOrder_Success`
- Verifica creación exitosa con estado `PROCESSING` y `active = true`
- Valida asignación correcta de ID y campos

### `testCreateOrder_IdAssignment_NoExistingOrders`
- Prueba asignación de ID cuando no hay órdenes previas
- Resultado esperado: `id = 1`

### `testCreateOrder_IdAssignment_WithExistingOrders`
- Prueba asignación de ID con órdenes existentes (IDs: 3, 5, 7)
- Resultado esperado: `id = 8` (maxId + 1)

### `testCreateOrder_CorrectMapping`
- Valida que `OrderMapper.toEntity()` y `toDto()` se invocan correctamente

### `testCreateOrder_Persistence`
- Verifica que `save()` se invoca una vez con la orden correcta
- Usa `ArgumentCaptor` para validar estado y propiedades

---

## 2. DeleteOrderTests (4 tests)

### `testDeleteOrder_Success`
- Verifica que `deleteById()` se invoca exactamente una vez

### `testDeleteOrder_CorrectParameter`
- Captura el parámetro y valida que sea el ID correcto

### `testDeleteOrder_RepositoryThrowsException`
- Simula excepción en el repositorio y verifica propagación

### `testDeleteOrder_InvalidId`
- Prueba eliminación con ID negativo (sin validación en el servicio)

---

## 3. ChangeStateOrderTests (5 tests)

### `testChangeStateOrder_Success`
- Verifica cambio exitoso de estado y retorno del DTO actualizado

### `testChangeStateOrder_OrderNotFound`
- Valida retorno de `null` cuando la orden no existe
- Verifica que `save()` nunca se invoca

### `testChangeStateOrder_PersistenceOfNewState`
- Captura la orden persistida y valida que tenga el nuevo estado

### `testChangeStateOrder_CorrectMapping`
- Verifica que el mapper convierta correctamente la orden actualizada

### `testChangeStateOrder_MultipleStateTransitions`
- Prueba transiciones en cadena: PROCESSING → TRAVELING_TO_WAREHOUSE → DELIVERED

---

## 4. ListOrdersByIdUserTests (5 tests)

### `testListOrdersByIdUser_Success`
- Verifica listado exitoso de 2 órdenes del usuario

### `testListOrdersByIdUser_NoOrders`
- Valida retorno de lista vacía cuando el usuario no tiene órdenes

### `testListOrdersByIdUser_CorrectMapping`
- Verifica que `toDto()` se invoca para cada orden

### `testListOrdersByIdUser_MultipleOrders`
- Prueba con 4 órdenes y valida que todas se retornan

### `testListOrdersByIdUser_DataConsistency`
- Valida consistencia de TODOS los campos incluyendo `active` (boolean)

---

## 5. GetOrderWithUserInfoTests (6 tests)

### `testGetOrderWithUserInfo_Success`
- Verifica obtención exitosa con orden y datos del usuario
- Valida comunicación RabbitMQ (mockeada)

### `testGetOrderWithUserInfo_OrderNotFound`
- Valida retorno de `null` cuando la orden no existe
- Verifica que productores/consumidores nunca se invocan

### `testGetOrderWithUserInfo_RabbitMQFailure`
- Simula fallo en conexión RabbitMQ
- Retorna DTO sin `userResponse` (null)

### `testGetOrderWithUserInfo_RabbitMQTimeout`
- Simula timeout en espera de respuesta del usuario
- Maneja `InterruptedException` sin propagarla

### `testGetOrderWithUserInfo_CorrectMapping`
- Verifica mapeo correcto de todos los campos
- Valida datos del usuario incluidos en `OrderWithUserDto`

### `testGetOrderWithUserInfo_ExceptionHandling`
- Valida manejo seguro de excepciones sin propagarlas
- Continúa ejecutión aunque RabbitMQ falle

---

## 6. ListAllOrdersTests (5 tests)

### `testListAllOrders_Success`
- Verifica listado exitoso de todas las órdenes

### `testListAllOrders_NoOrders`
- Valida retorno de lista vacía cuando no hay órdenes

### `testListAllOrders_CorrectMapping`
- Verifica que `toDto()` se invoca para cada orden

### `testListAllOrders_MultipleOrdersDifferentUsers`
- Prueba con 5 órdenes de 3 usuarios diferentes

### `testListAllOrders_DataConsistency`
- Valida consistencia de TODOS los campos
- Incluye estados especiales (CANCELED) y `active = false`

---

## 7. ShowOrderByIdTests (5 tests)

### `testShowOrderById_Success`
- Verifica búsqueda exitosa y retorno del OrderDto

### `testShowOrderById_OrderNotFound`
- Valida retorno de `null` cuando la orden no existe
- Verifica que mapper nunca se invoca

### `testShowOrderById_CorrectMapping`
- Verifica que el mapper se invoca con el Order correcto

### `testShowOrderById_DataConsistency`
- Valida consistencia de todos los campos incluyendo `active = false`

### `testShowOrderById_DifferentIds`
- Prueba búsquedas con múltiples IDs (exitosa, exitosa, no encontrada)

---

## Configuración de Tests

### Herramientas Utilizadas
- **Framework:** JUnit 5 (Jupiter)
- **Mocking:** Mockito 4.x
- **Inyección:** `@ExtendWith(MockitoExtension.class)`
- **Anotaciones:** `@Mock`, `@InjectMocks`, `@Nested`, `@BeforeEach`
- **Patterns:** `@ArgumentCaptor`, `doThrow()`, `doNothing()`

### Componentes Mockeados
| Componente | Razón |
|-----------|-------|
| `OrderRepository` | Aislamiento de lógica de persistencia |
| `OrderMapper` | Aislamiento de transformación de datos |
| `UserServiceProducer` | Aislamiento de comunicación RabbitMQ |
| `UserServiceConsumer` | Aislamiento de consumer RabbitMQ |

---

## Ejecución de Tests

### Ejecutar todos los tests:
```bash
mvn test
```

### Ejecutar solo los tests de OrderService:
```bash
mvn test -Dtest=OrderServiceTest
```

### Ejecutar una clase anidada específica:
```bash
mvn test -Dtest=OrderServiceTest\$CreateOrderTests
```

### Ejecutar un test específico:
```bash
mvn test -Dtest=OrderServiceTest#testCreateOrder_Success
```

### Con reporte de cobertura (JaCoCo):
```bash
mvn test jacoco:report
```

---

## Estado Actual

### Resumen por Método

| Método | Tests | Estado |
|--------|-------|--------|
| `createOrder()` | 5 | ✅ Completados |
| `deleteOrder()` | 4 | ✅ Completados |
| `changeStateOrder()` | 5 | ✅ Completados |
| `listOrdersByIdUser()` | 5 | ✅ Completados |
| `getOrderWithUserInfo()` | 6 | ✅ Completados |
| `listAllOrders()` | 5 | ✅ Completados |
| `showOrderById()` | 5 | ✅ Completados |
| **TOTAL** | **35** | **✅ COMPLETO** |

---



## Características Principales

### ✅ Aislamiento Total
- Todos los componentes externos están mockeados
- Sin dependencias reales a base de datos o RabbitMQ

### ✅ Patrón AAA
- **Arrange:** Setup de datos y mocks
- **Act:** Invocación del método bajo prueba
- **Assert:** Validación de resultados

### ✅ Validación Exhaustiva
- Tests de casos exitosos
- Tests de casos fallidos
- Tests de edge cases
- Tests de transiciones de estado
- Tests de consistencia de datos

### ✅ Verificación de Invocaciones
- Uso de `verify()` para confirmar llamadas a dependencias
- `ArgumentCaptor` para validar parámetros exactos
- `never()` para verificar que métodos NO se invocan

### ✅ Manejo de Excepciones
- Tests que simulan fallos de RabbitMQ
- Validación de timeout
- Confirmación que excepciones se capturan correctamente

---

## Notas Importantes

- La clase `OrderServiceTest` utiliza **clases anidadas `@Nested`** para mejor organización
- Los mocks son compartidos entre todas las clases anidadas
- Los tests RabbitMQ son **unitarios** (RabbitMQ está mockeado)
- Para tests de integración real con RabbitMQ, usar `componentIntegrationTests.java`
- La estructura permite fácil expansión de tests para futuros métodos

---

## Convenciones de Nombres

- **Clase de test:** `OrderServiceTest`
- **Clases anidadas:** `{Method}Tests` (ej: `CreateOrderTests`)
- **Métodos de test:** `test{Method}_{Scenario}` (ej: `testCreateOrder_Success`)
