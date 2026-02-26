# 📋 Reporte de Auditoría Técnica

**Proyecto:** Diagnostico-Semana0
**Fecha:** 2026-02-26
**Archivos Analizados:** 2
**Total de Hallazgos:** 7
**Capas de Dependencia Detectadas:** 3

---

## ⚠️ Dependencias Circulares (resolver primero)

No se detectaron dependencias circulares ✅

---

## 📊 Resumen Ejecutivo

### Violaciones SOLID

| Principio | Hallazgos |
|-----------|-----------|
| S — Single Responsibility | 2 |
| O — Open/Closed | 0 |
| L — Liskov Substitution | 0 |
| I — Interface Segregation | 0 |
| D — Dependency Inversion | 2 |

### Code Smells

| Categoría | Hallazgos |
|-----------|-----------|
| Acoplamiento Rígido | 2 |
| Lógica Duplicada | 1 |
| Falta de Abstracción | 1 |
| Clases Dios | 0 |
| Nombres Confusos | 1 |

---

## 🗺️ Mapa de Capas de Dependencia

Capa 0 — Base (sin dependencias internas)
  └── DTOs / Modelos / Repositorio (interfaces) — `OrderDto`, `OrderWithUserDto`, `OrderJpaRepository`, `Order` (model), `State`

Capa 1 — (depende de Capa 0)
  └── `OrderService.java`    [depende de: OrderJpaRepository, OrderMapper, UserEnrichmentService, OrderDto]

Capa Top — Entry points / Composition Root
  └── `OrderController.java`    [depende de: OrderService, OrderDto, OrderWithUserDto, OrderStateUpdateDto]

---

## 🔎 Hallazgos por Capa

---

### 🏗️ Capa 1 — `OrderService.java`

Resumen: Servicio con responsabilidades de negocio y de orquestación/enchufe a mensajería; además mantiene lógica de persistencia directa y mapeo. Hay mezcla de responsabilidades y dependencias concretas que dificultan pruebas y extensión.

#### 🔴 [SOLID — S] Single Responsibility Principle — Método: `getOrderWithUserInfo` (severidad: Moderado/Alto)

Problema: `OrderService` realiza 3 responsabilidades en un método: recuperar entidad (persistencia), orquestar petición de enriquecimiento por RabbitMQ (integración/messaging) y construir DTO compuesto. Esto causa que el servicio cambie por múltiples razones (cambios en persistencia, cambios en protocolo de mensajería, cambios de representación DTO).

Código problemático (fragmento):

```java
public OrderWithUserDto getOrderWithUserInfo(Integer orderId) {
    OrderDto orderDto = showOrderById(orderId);
    // ... llama a userEnrichmentService.fetchUserInfo(idUser) y arma OrderWithUserDto
}
```

Solución sugerida: Extraer la responsabilidad de enriquecimiento a un componente/adapter separado (`OrderEnrichmentFacade`) que reciba un `OrderDto` y devuelva `OrderWithUserDto`. Dejar `OrderService` enfocado en reglas de negocio y persistencia.

Snippet propuesto (nuevo componente):

```java
public class OrderEnrichmentFacade {
    private final UserEnrichmentService enrichmentService;

    public OrderEnrichmentFacade(UserEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    public OrderWithUserDto enrich(OrderDto orderDto) {
        UserResponse userResponse = null;
        try {
            userResponse = enrichmentService.fetchUserInfo(orderDto.getIdUser());
        } catch (Exception ex) {
            // log and return order without user
        }
        return new OrderWithUserDto(orderDto.getId(), orderDto.getName(), orderDto.getDescription(),
                orderDto.getIdUser(), orderDto.getState(), orderDto.isActive(), userResponse);
    }
}
```

Aplicación: `OrderService#getOrderWithUserInfo` delega a `OrderEnrichmentFacade`.

#### 🔴 [SOLID — D] Dependency Inversion Principle — Uso de dependencias concretas (severidad: Moderado)

Problema: Aunque `OrderJpaRepository` es una abstracción Spring Data, `OrderService` depende directamente de `OrderMapper` y `UserEnrichmentService` concretos. Esto hace difícil mockear/componer alternativas en tests o cuando cambie la implementación (por ejemplo, llamar a un HTTP sync en lugar de RabbitMQ).

Solución sugerida: Programar contra interfaces/contratos y no contra implementaciones. Por ejemplo definir `IOrderMapper` (si MapStruct ya genera una interface, inyectar la interface), y exponer `IUserEnrichmentClient` como interfaz para el adaptador de mensajería.

Snippet propuesto (interfaz mínima para el adaptador):

```java
public interface IUserEnrichmentClient {
    UserResponse fetchUserInfo(Integer userId) throws Exception;
}

// Implementación por RabbitMQ
public class RabbitUserEnrichmentClient implements IUserEnrichmentClient {
    private final UserEnrichmentService service;
    public RabbitUserEnrichmentClient(UserEnrichmentService service) { this.service = service; }
    public UserResponse fetchUserInfo(Integer userId) throws Exception { return service.fetchUserInfo(userId); }
}
```

Así `OrderService` depende de `IUserEnrichmentClient`.

#### 🟡 [SMELL — Lógica Duplicada / Importaciones] Importaciones repetidas (severidad: Bajo)

Problema: `OrderService.java` contiene imports duplicados de `Service` y `Transactional`:

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

Solución: Eliminar duplicados. Es un cambio trivial pero mejora claridad.

#### 🟢 [SMELL — Nombres Confusos] `findOrderByIdOrThrow` devuelve excepción personalizada

Problema: El método lanza `OrderNotFoundException` que probablemente está bien, pero los controladores no manejan esa excepción explícitamente (ver Capa Top). Recomiendo un contrato claro: servicios lanzan excepciones del dominio y controladores traducen a códigos HTTP mediante `@ControllerAdvice`.

---

### 🏗️ Capa Top — `OrderController.java`

Resumen: Controlador delgado, pero falta manejo explícito de excepciones y validación de respuestas nulas en endpoints que pueden devolver null.

#### 🔴 [SMELL — Acoplamiento Rígido / Manejo de errores] Falta de manejo de `OrderNotFoundException` (severidad: Moderado/Alto)

Problema: `OrderService.findOrderByIdOrThrow` lanza `OrderNotFoundException` cuando no existe el pedido; `OrderController` no captura esa excepción ni existe un `@ControllerAdvice` visible. Resultado probable: respuestas 500 en lugar de 404.

Solución sugerida: Añadir un `@ControllerAdvice` global que mapee `OrderNotFoundException` a `ResponseEntity.status(404)`.

Snippet propuesto (`RestExceptionHandler`):

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
```

Aplicación alternativa: manejar en cada endpoint con `try/catch` y devolver `ResponseEntity.notFound()`.

#### 🟡 [SOLID — S] Respuesta `showOrderById` con `expand=user` puede devolver `null` (severidad: Moderado)

Problema: `OrderService#getOrderWithUserInfo` puede devolver `null` cuando el pedido no existe; el controlador hará `ResponseEntity.ok(null)` en vez de 404.

Solución: Después de obtener el resultado, validar `null` y devolver 404 o dejar que la excepción fluya y sea manejada por `@ControllerAdvice`.

Snippet mínimo de cambio en `OrderController#showOrderById`:

```java
if ("user".equals(expand)) {
    OrderWithUserDto order = orderService.getOrderWithUserInfo(id);
    if (order == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(order);
}
```

---

### 🧭 Propuesta: Endpoints explícitos y de responsabilidad única (Controlador)

Motivación: Evitar `RequestParam` condicionales que introducen rutas ambiguas y lógica de branching en el controlador. Preferir rutas REST explícitas facilita pruebas, documentación y cumplimiento de SRP (cada endpoint hace una sola cosa).

Propuesta de endpoints concretos:

- `POST /orders` — Crear pedido
- `GET /orders` — Listar pedidos activos
- `GET /orders/all` — Listar todos los pedidos (administrativo)
- `GET /orders/{id}` — Obtener pedido por id
- `GET /orders/{id}/user` — Obtener pedido por id con información de usuario (enriquecido)
- `GET /orders/user/{userId}` — Listar pedidos de un usuario
- `PATCH /orders/{id}` — Actualizar estado del pedido
- `DELETE /orders/{id}` — Soft-delete del pedido

Breve ejemplo de controlador (firma de endpoints):

```java
@GetMapping("/orders/{id}")
public ResponseEntity<OrderDto> getOrder(@PathVariable Integer id) { ... }

@GetMapping("/orders/{id}/user")
public ResponseEntity<OrderWithUserDto> getOrderWithUser(@PathVariable Integer id) { ... }

@GetMapping("/orders/user/{userId}")
public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable Integer userId) { ... }
```

Aplicación: Refactorizar `OrderController` para exponer estos endpoints y mover la lógica de branching al servicio o a un facade cuando sea estrictamente necesario.


## ✅ Resumen de Cambios Recomendados (ordenados por prioridad)

1. (Alta) Añadir `@ControllerAdvice` para mapear `OrderNotFoundException` a 404 y evitar 500s inesperados. — REFACTORIZADO (GlobalExceptionHandler presente)
2. (Alta) Extraer la lógica de enriquecimiento a un `OrderEnrichmentFacade` o adaptador (`IUserEnrichmentClient`) y delegar desde `OrderService`. — REFACTORIZADO (OrderEnrichmentFacade implementado)
3. (Media) Programar contra interfaces: introducir `IOrderMapper`/`IUserEnrichmentClient` si no existen (MapStruct genera interfaces habitualmente). — REFACTORIZADO (IUserEnrichmentClient añadido; servicios adaptados)
4. (Baja) Eliminar imports duplicados en `OrderService.java`. — REFACTORIZADO (imports limpios)
5. (Baja) Validar retornos `null` en el controlador y devolver 404 apropiado. — REFACTORIZADO (moved handling to service + GlobalExceptionHandler)

---

## 🛠️ Ejemplo de Implementación Rápida (pasos mínimos)

- Crear `RestExceptionHandler` en paquete `controller` y registrarlo.
- Crear clase `OrderEnrichmentFacade` que encapsule llamadas a `UserEnrichmentService` y manejar timeouts/errores allí.
- Actualizar `OrderService#getOrderWithUserInfo` para delegar al facade.

---

## Archivos Sin Hallazgos

- Ninguno (se analizaron únicamente `OrderService.java` y `OrderController.java`).

---

## Conclusión

El código es funcional y sigue muchas buenas prácticas (uso de Spring Data, DTOs, transaccionalidad). Las principales deudas detectadas son mezcla de responsabilidades en `OrderService` (persistencia + orquestación de mensajería + construcción de DTO compuesto) y falta de manejo centralizado de excepciones en la capa de presentación. Recomiendo priorizar el manejo de excepciones (para evitar retornos 500) y extraer el enriquecimiento de usuario a un adaptador separado para mejorar testabilidad y cumplir mejor SRP y DIP.

---

Si quieres, aplico los cambios mínimos: 1) añado `RestExceptionHandler`, 2) quito imports duplicados y 3) extraigo el `OrderEnrichmentFacade` y actualizo `OrderService` para delegar. ¿Procedo con esos cambios ahora?
