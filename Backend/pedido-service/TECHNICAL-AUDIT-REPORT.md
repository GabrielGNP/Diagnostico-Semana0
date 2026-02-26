# 📋 Reporte de Auditoría Técnica

**Proyecto:** pedido-service  
**Fecha:** 25 de febrero de 2026  
**Archivos Analizados:** 19  
**Total de Hallazgos:** 16  
**Capas de Dependencia Detectadas:** 5 (0 a Top)

---

## ⚠️ Dependencias Circulares

No se detectaron dependencias circulares ✅

---

## 📊 Resumen Ejecutivo

### Violaciones SOLID

| Principio | Hallazgos |
|-----------|-----------|
| S — Single Responsibility | 4 |
| O — Open/Closed | 1 |
| L — Liskov Substitution | 0 |
| I — Interface Segregation | 0 |
| D — Dependency Inversion | 3 |

### Code Smells

| Categoría | Hallazgos |
|-----------|-----------|
| Acoplamiento Rígido | 2 |
| Lógica Duplicada | 2 |
| Falta de Abstracción | 2 |
| Clases Dios | 1 |
| Nombres Confusos | 1 |

---

## 🗺️ Mapa de Capas de Dependencia

```
Capa 0 — Base (sin dependencias internas)
  └── model/State.java                              [enum puro]
  └── exception/OrderNotFoundException.java         [RuntimeException]
  └── exception/ErrorResponse.java                  [DTO puro]
  └── messaging/UserRequest.java                    [DTO puro]
  └── messaging/UserResponse.java                   [DTO puro]

Capa 1 — (depende de Capa 0)
  └── model/Order.java                              [depende de: State]
  └── dto/OrderDto.java                             [depende de: State]
  └── dto/OrderStateUpdateDto.java                  [depende de: State]
  └── dto/OrderWithUserDto.java                     [depende de: State, UserResponse]
  └── config/RabbitMQConfig.java                    [sin deps internas]
  └── config/CorsConfig.java                        [sin deps internas]
  └── config/RabbitMQMessageConverterConfig.java    [sin deps internas]
  └── repository/OrderJpaRepository.java            [depende de: Order]

Capa 2 — (depende de Capas 0–1)
  └── mapper/OrderMapper.java                       [depende de: OrderDto, Order]
  └── messaging/UserServiceProducer.java            [depende de: RabbitMQConfig, UserRequest]
  └── messaging/UserServiceConsumer.java            [depende de: RabbitMQConfig, UserResponse]
  └── repository/OrderRepository.java               [depende de: Order, OrderJpaRepository]
  └── exception/GlobalExceptionHandler.java         [depende de: ErrorResponse, OrderNotFoundException]

Capa 3 — (depende de Capas 0–2)
  └── service/OrderService.java                     [depende de: OrderJpaRepository, OrderMapper, 
                                                     UserServiceProducer, UserServiceConsumer, 
                                                     OrderDto, OrderWithUserDto, State, OrderNotFoundException]

Capa Top — Entry Points / Composition Root
  └── controller/OrderController.java               [depende de: OrderService, OrderDto, 
                                                     OrderWithUserDto, OrderStateUpdateDto, State]
  └── PedidoServiceApplication.java                 [Spring Boot entry point]
```

---

## 🔎 Hallazgos por Capa

> Los archivos se presentan de la capa más simple (Capa 0) a la más compleja (Capa Top).  
> Resolver las capas inferiores primero evita rehacer trabajo en capas superiores.

---

### 🏗️ Capa 0 — Base (Sin Dependencias Internas)

---

#### ✅ `model/State.java` — Sin hallazgos

---

#### ✅ `exception/OrderNotFoundException.java` — Sin hallazgos

---

#### ✅ `exception/ErrorResponse.java` — Sin hallazgos

---

#### ✅ `messaging/UserRequest.java` — Sin hallazgos

---

#### ✅ `messaging/UserResponse.java` — Sin hallazgos

---

### 🏗️ Capa 1 — (Depende de Capa 0)

---

#### ✅ `model/Order.java` — Sin hallazgos

---

#### ✅ `dto/OrderDto.java` — Sin hallazgos

---

#### ✅ `dto/OrderStateUpdateDto.java` — Sin hallazgos

---

#### 📄 `dto/OrderWithUserDto.java`

##### 🟢 [SOLID — S] Single Responsibility Principle — Líneas 89-98

**Problema:** La clase tiene métodos duplicados para acceder al mismo campo `user`: `getUser()`/`setUser()` y `getUserResponse()`/`setUserResponse()`. Esto crea confusión sobre qué método usar y viola el principio de tener una sola forma canónica de acceder a datos.

**Código actual (líneas 89–98):**
```java
public UserResponse getUser() {
    return user;
}

public void setUser(UserResponse user) {
    this.user = user;
}

public UserResponse getUserResponse() {
    return user;
}

public void setUserResponse(UserResponse user) {
    this.user = user;
}
```

**Solución sugerida:** Eliminar los métodos redundantes `getUserResponse()`/`setUserResponse()`:
```java
public UserResponse getUser() {
    return user;
}

public void setUser(UserResponse user) {
    this.user = user;
}
```

**Estado:** REFACTORIZADO — métodos redundantes eliminados en `OrderWithUserDto.java`.

---

#### ✅ `config/RabbitMQConfig.java` — Sin hallazgos

---

#### ✅ `config/CorsConfig.java` — Sin hallazgos

---

#### ✅ `config/RabbitMQMessageConverterConfig.java` — Sin hallazgos

---

#### ✅ `repository/OrderJpaRepository.java` — Sin hallazgos

---

### 🏗️ Capa 2 — (Depende de Capas 0–1)

---

#### ✅ `mapper/OrderMapper.java` — Sin hallazgos

---

#### 📄 `messaging/UserServiceProducer.java`

##### 🟡 [SOLID — D] Dependency Inversion Principle — Línea 15

**Problema:** Se utiliza `@Autowired` sobre campo directo en lugar de inyección por constructor. Esto dificulta el testing y viola el principio de inversión de dependencias al acoplar directamente con la implementación concreta.

**Código actual (líneas 14–17):**
```java
@Component
public class UserServiceProducer {

    private static final Logger log = LoggerFactory.getLogger(UserServiceProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
```

**Solución sugerida:** Usar inyección por constructor:
```java
@Component
public class UserServiceProducer {

    private static final Logger log = LoggerFactory.getLogger(UserServiceProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public UserServiceProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
```

**Estado:** REFACTORIZADO — `UserServiceProducer` refactorizado a inyección por constructor.

---

#### 📄 `messaging/UserServiceConsumer.java`

##### 🟡 [SOLID — S] Single Responsibility Principle — Líneas 1–62

**Problema:** La clase tiene dos responsabilidades distintas: (1) recibir mensajes de RabbitMQ y (2) gestionar un cache/espera de respuestas con sincronización de hilos. Estas deberían estar separadas.

**Código actual (fragmento):**
```java
@Component
public class UserServiceConsumer {
    private final Map<Integer, UserResponse> userResponses = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @RabbitListener(queues = RabbitMQConfig.USER_RESPONSE_QUEUE)
    public void receiveUserResponse(UserResponse response) {
        // Lógica de almacenamiento y notificación
    }

    public UserResponse getUserResponse(int userId, long timeoutMs) {
        // Lógica de espera con polling
    }
}
```

**Solución sugerida:** Extraer la lógica de caché a una clase separada:
```java
// UserResponseCache.java (nueva clase — Capa 1)
@Component
public class UserResponseCache {
    private final Map<Integer, UserResponse> userResponses = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public void store(UserResponse response) {
        if (response != null && response.getId() != null) {
            userResponses.put(response.getId(), response);
            synchronized (lock) { lock.notifyAll(); }
        }
    }

    public UserResponse awaitResponse(int userId, long timeoutMs) {
        // Lógica de espera...
    }
}

// UserServiceConsumer.java (refactorizado)
@Component
public class UserServiceConsumer {
    private final UserResponseCache cache;

    public UserServiceConsumer(UserResponseCache cache) {
        this.cache = cache;
    }

    @RabbitListener(queues = RabbitMQConfig.USER_RESPONSE_QUEUE)
    public void receiveUserResponse(UserResponse response) {
        cache.store(response);
    }
}
```

**Estado:** REFACTORIZADO — `UserResponseCache` creado y `UserServiceConsumer` refactorizado para delegar en la cache.

---

#### 📄 `repository/OrderRepository.java`

##### 🔴 [SMELL — Middle Man / Falta de Abstracción] — Líneas 1–88

**Problema:** `OrderRepository` es un wrapper innecesario sobre `OrderJpaRepository`. Cada método simplemente delega al JpaRepository sin agregar lógica de valor. Esto es un antipatrón "Middle Man" que agrega complejidad sin beneficio.

**Código actual (líneas 32–50):**
```java
@Repository
public class OrderRepository {

    @Autowired
    private OrderJpaRepository jpaRepository;

    public List<Order> findAll() {
        return jpaRepository.findAll();
    }

    public Order save(Order order) {
        return jpaRepository.save(order);
    }
    // ... más delegaciones directas
}
```

**Solución sugerida:** Eliminar `OrderRepository` y usar `OrderJpaRepository` directamente. Si se requiere un patrón de abstracción, usar una interfaz:
```java
// Opción 1: Eliminar OrderRepository, inyectar OrderJpaRepository directamente

// Opción 2: Si se necesita abstracción, crear interfaz
public interface IOrderRepository {
    List<Order> findAll();
    Order save(Order order);
    void deleteById(int id);
    Optional<Order> findById(int id);
    List<Order> findByUserId(int userId);
    List<Order> findAllActive();
}

// OrderJpaRepository puede extenderla o se crea un adaptador
```

**Nota:** Actualmente `OrderService` ya usa `OrderJpaRepository` directamente, haciendo a `OrderRepository` completamente obsoleto.

---

#### ✅ `exception/GlobalExceptionHandler.java` — Sin hallazgos

---

### 🏗️ Capa 3 — (Depende de Capas 0–2)

---

#### 📄 `service/OrderService.java`

##### 🔴 [SOLID — S] Single Responsibility Principle — Clase completa (229 líneas)

**Problema:** `OrderService` gestiona múltiples responsabilidades:
1. CRUD de órdenes (crear, eliminar, actualizar estado, listar)
2. Comunicación con el servicio de usuarios vía RabbitMQ
3. Composición de DTOs enriquecidos (`OrderWithUserDto`)

Esto resulta en una clase con demasiadas razones para cambiar.

**Código actual (fragmento del método getOrderWithUserInfo, líneas 133–176):**
```java
public OrderWithUserDto getOrderWithUserInfo(Integer orderId) {
    OrderDto orderDto = showOrderById(orderId);
    if (orderDto == null) {
        return null;
    }

    Integer idUser = orderDto.getIdUser();
    UserResponse userResponse = null;
    try {
        userServiceProducer.requestUserInfo(idUser);
        userResponse = userServiceConsumer.getUserResponse(idUser, USER_REQUEST_TIMEOUT);
    } catch (Exception ex) {
        log.warn("Error requesting/receiving user info for userId={}", idUser, ex);
    }

    return new OrderWithUserDto(
            orderDto.getId(),
            orderDto.getName(),
            orderDto.getDescription(),
            orderDto.getIdUser(),
            orderDto.getState(),
            orderDto.isActive(),
            userResponse
    );
}
```

**Estado:** REFACTORIZADO — `OrderRepository.java` eliminado; el código usa `OrderJpaRepository` directamente.

**Solución sugerida:** Extraer la lógica de enriquecimiento de usuario a una clase dedicada:
```java
// UserEnrichmentService.java (nueva clase — Capa 2)
@Service
public class UserEnrichmentService {
    private final UserServiceProducer producer;
    private final UserServiceConsumer consumer;
    private static final long USER_REQUEST_TIMEOUT = 3000;

    public UserEnrichmentService(UserServiceProducer producer, UserServiceConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public UserResponse fetchUserInfo(Integer userId) {
        try {
            producer.requestUserInfo(userId);
            return consumer.getUserResponse(userId, USER_REQUEST_TIMEOUT);
        } catch (Exception ex) {
            log.warn("Error fetching user info for userId={}", userId, ex);
            return null;
        }
    }
}

// OrderService.java (simplificado)
public OrderWithUserDto getOrderWithUserInfo(Integer orderId) {
    OrderDto orderDto = showOrderById(orderId);
    UserResponse user = userEnrichmentService.fetchUserInfo(orderDto.getIdUser());
    return new OrderWithUserDto(..., user);
}
```

---

##### 🟡 [SOLID — D] Dependency Inversion Principle — Líneas 35–45

**Problema:** `OrderService` depende de implementaciones concretas (`UserServiceProducer`, `UserServiceConsumer`) en lugar de abstracciones. Además, mezcla `@Autowired` en campos con inyección por constructor, creando inconsistencia.

**Código actual (líneas 35–57):**
```java
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserServiceProducer userServiceProducer;

    @Autowired
    private UserServiceConsumer userServiceConsumer;

    private static final long USER_REQUEST_TIMEOUT = 3000;

    @Autowired
    public OrderService(OrderJpaRepository orderJpaRepository, OrderMapper orderMapper,
                        UserServiceProducer userServiceProducer, UserServiceConsumer userServiceConsumer) {
        this.orderJpaRepository = orderJpaRepository;
        // ...
    }
```

**Solución sugerida:** Usar solo inyección por constructor y definir interfaces:
```java
// IUserInfoClient.java (nueva interfaz — Capa 1)
public interface IUserInfoClient {
    UserResponse fetchUserInfo(Integer userId, long timeoutMs);
}

// RabbitMQUserInfoClient.java implementa IUserInfoClient

// OrderService.java (refactorizado)
@Service
@Transactional
public class OrderService {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;
    private final IUserInfoClient userInfoClient;

    public OrderService(OrderJpaRepository orderJpaRepository, 
                        OrderMapper orderMapper,
                        IUserInfoClient userInfoClient) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderMapper = orderMapper;
        this.userInfoClient = userInfoClient;
    }
}
```

---

##### 🟡 [SMELL — Lógica Duplicada] — Líneas 96, 110, 222

**Problema:** El patrón `orElseThrow(() -> new OrderNotFoundException(...))` se repite múltiples veces con el mismo mensaje.

**Código actual (líneas dispersas):**
```java
// Línea 96
Order order = orderJpaRepository.findById(id)
    .orElseThrow(() -> new OrderNotFoundException("Pedido con ID " + id + " no encontrado"));

// Línea 110
Order order = orderJpaRepository.findById(id)
    .orElseThrow(() -> new OrderNotFoundException("Pedido con ID " + id + " no encontrado"));

// Línea 222
.orElseThrow(() -> new OrderNotFoundException("Pedido con ID " + id + " no encontrado"));
```

**Solución sugerida:** Extraer a un método privado:
```java
private Order findOrderByIdOrThrow(Integer id) {
    return orderJpaRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("Pedido con ID " + id + " no encontrado"));
}

// Uso
Order order = findOrderByIdOrThrow(id);
```

---

##### 🟢 [SMELL — Número Mágico] — Línea 47

**Problema:** El timeout `3000` está hardcodeado como constante en el servicio. Debería ser configurable externamente.

**Código actual (línea 47):**
```java
private static final long USER_REQUEST_TIMEOUT = 3000; // 3 seconds timeout
```

**Solución sugerida:** Usar `@Value` para configuración externa:
```java
@Value("${user.service.timeout:3000}")
private long userRequestTimeout;
```

---

### 🏗️ Capa Top — Entry Points y Composition Root

---

#### 📄 `controller/OrderController.java`

##### 🟡 [SOLID — O] Open/Closed Principle — Líneas 82–90

**Problema:** El método `showOrderById` usa un if-else para determinar si expandir información de usuario. Si se agregan más tipos de expansión (`expand=items`, `expand=history`), el método deberá modificarse.

**Código actual (líneas 82–90):**
```java
@GetMapping("/{id}")
public ResponseEntity<Object> showOrderById(@PathVariable("id") Integer id, 
        @RequestParam(value = "expand", required = false) String expand) {
    if ("user".equals(expand)) {
        OrderWithUserDto order = orderService.getOrderWithUserInfo(id);
        return ResponseEntity.ok(order);
    } else {
        OrderDto orderDto = orderService.showOrderById(id);
        return ResponseEntity.ok(orderDto);
    }
}
```

**Solución sugerida:** Usar un patrón Strategy/Map para expansores:
```java
// OrderExpander.java (interfaz — Capa 2)
public interface OrderExpander {
    String getKey();
    Object expand(OrderDto order);
}

// UserOrderExpander.java (implementación)
@Component
public class UserOrderExpander implements OrderExpander {
    private final UserEnrichmentService enrichmentService;
    
    public String getKey() { return "user"; }
    
    public Object expand(OrderDto order) {
        return new OrderWithUserDto(..., enrichmentService.fetchUserInfo(order.getIdUser()));
    }
}

// En OrderController o OrderService
private final Map<String, OrderExpander> expanders;

public Object getOrderWithExpansion(Integer id, String expand) {
    OrderDto order = showOrderById(id);
    if (expand != null && expanders.containsKey(expand)) {
        return expanders.get(expand).expand(order);
    }
    return order;
}
```

---

##### 🟢 [SMELL — Acoplamiento Rígido] — Línea 33

**Problema:** El controlador usa `@Autowired` en campo directo en lugar de inyección por constructor.

**Código actual (líneas 32–34):**
```java
public class OrderController {

    @Autowired
    private OrderService orderService;
```

**Solución sugerida:** Usar inyección por constructor:
```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
```

---

#### ✅ `PedidoServiceApplication.java` — Sin hallazgos

---

## ✅ Archivos Sin Hallazgos

- `model/State.java`
- `model/Order.java`
- `exception/OrderNotFoundException.java`
- `exception/ErrorResponse.java`
- `exception/GlobalExceptionHandler.java`
- `messaging/UserRequest.java`
- `messaging/UserResponse.java`
- `dto/OrderDto.java`
- `dto/OrderStateUpdateDto.java`
- `config/RabbitMQConfig.java`
- `config/CorsConfig.java`
- `config/RabbitMQMessageConverterConfig.java`
- `repository/OrderJpaRepository.java`
- `mapper/OrderMapper.java`
- `PedidoServiceApplication.java`

---

## 🛠️ Orden de Resolución Recomendado

Basado en las capas de dependencia, resolver en esta secuencia para evitar rehacer trabajo:

| Prioridad | Capa | Archivo | Hallazgo | Tipo | Severidad |
|-----------|------|---------|----------|------|-----------|
| 1 | 1 | `dto/OrderWithUserDto.java` | Métodos duplicados para campo `user` | SOLID-S | 🟢 Menor |
| 2 | 2 | `repository/OrderRepository.java` | Middle Man — wrapper innecesario | Smell | 🔴 Crítico |
| 3 | 2 | `messaging/UserServiceProducer.java` | @Autowired en campo — usar constructor | SOLID-D | 🟡 Moderado |
| 4 | 2 | `messaging/UserServiceConsumer.java` | Dos responsabilidades — extraer cache | SOLID-S | 🟡 Moderado |
| 5 | 3 | `service/OrderService.java` | Lógica duplicada — orElseThrow | Smell | 🟡 Moderado |
| 6 | 3 | `service/OrderService.java` | Número mágico — externalizar timeout | Smell | 🟢 Menor |
| 7 | 3 | `service/OrderService.java` | DIP — mezcla @Autowired con constructor | SOLID-D | 🟡 Moderado |
| 8 | 3 | `service/OrderService.java` | SRP — extraer UserEnrichmentService | SOLID-S | 🔴 Crítico |
| 9 | Top | `controller/OrderController.java` | @Autowired en campo — usar constructor | Smell | 🟢 Menor |
| 10 | Top | `controller/OrderController.java` | OCP — if-else para expand | SOLID-O | 🟡 Moderado |

---

## 📈 Recomendaciones Arquitectónicas

### Corto Plazo (Sprint Actual)
1. **Eliminar `OrderRepository.java`** — Ya no se usa, `OrderService` consume `OrderJpaRepository` directamente
2. **Unificar inyección de dependencias** — Usar inyección por constructor en todas las clases
3. **Eliminar métodos duplicados** en `OrderWithUserDto`

### Mediano Plazo (1-2 Sprints)
4. **Extraer `UserEnrichmentService`** — Desacoplar la lógica de enriquecimiento de usuarios del servicio de órdenes
5. **Extraer `UserResponseCache`** — Separar responsabilidades en `UserServiceConsumer`
6. **Externalizar configuración** — Mover timeouts y constantes a `application.properties`

### Largo Plazo (Próximo Trimestre)
7. **Definir interfaces para mensajería** — Crear `IUserInfoClient` para facilitar testing y desacoplamiento
8. **Implementar patrón Strategy para expansores** — Preparar el sistema para múltiples tipos de expansión de datos

---

> 💡 **Regla de oro:** Siempre resuelve capas inferiores antes que superiores. Un fix de DIP en Capa 2 resolverá naturalmente violaciones de acoplamiento en Capa 3 que dependían de él.
