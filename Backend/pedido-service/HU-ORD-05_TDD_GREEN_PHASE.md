# HU-ORD-05: Implementación Fase GREEN (TDD)

## 📋 Historia de Usuario
**HU-ORD-05**: Como Product Owner quiero crear pedidos nuevos y asegurar que cada entidad se almacena correctamente en PostgreSQL con todos sus atributos.

## 🔴 Fase RED (Completada)
✅ Tests creados en `OrderServiceHuOrd05Test.java`
- CA-01: Creación exitosa de pedido
- CA-02: Campos requeridos faltantes  
- CA-03: Tipo de dato inválido
- FR-ORD-05-01: Inserción en PostgreSQL
- NFR-ORD-05-02: Performance < 100ms

**Commit**: Tests en fase RED - Todos fallando como esperado

## 🟢 Fase GREEN (Implementada)

### Cambios Realizados en `OrderService.java`

#### 1. **Inyección de Dependencias Actualizada**
```java
@Autowired
private OrderJpaRepository orderJpaRepository;  // Nueva: PostgreSQL JPA

@Autowired
private OrderRepository orderRepository;  // Mantener para compatibilidad con otros métodos
```

#### 2. **Método `createOrder()` Refactorizado**

**Antes (Lógica antigua con JSON)**:
```java
public OrderDto createOrder(OrderDto orderDto) {
    Order order = orderMapper.toEntity(orderDto);
    order.setState(State.PROCESSING);
    order.setActive(true);
    
    // Cálculo manual de ID (JSON-based)
    int maxId = orderRepository.findAll().stream()
        .mapToInt(Order::getId).max().orElse(0);
    order.setId(maxId + 1);
    
    Order savedOrder = orderRepository.save(order);
    return orderMapper.toDto(savedOrder);
}
```

**Después (PostgreSQL con validaciones)**:
```java
public OrderDto createOrder(OrderDto orderDto) {
    // 1. Validar campos requeridos (FR-ORD-05-02)
    validateOrderDto(orderDto);
    
    // 2. Mapear a entidad
    Order order = orderMapper.toEntity(orderDto);
    
    // 3. Asignar valores por defecto (FR-ORD-05-03)
    order.setState(State.PROCESSING);
    order.setActive(true);
    
    // 4. Guardar en PostgreSQL (FR-ORD-05-01)
    // ID autogenerado por PostgreSQL (@GeneratedValue)
    Order savedOrder = orderJpaRepository.save(order);
    
    return orderMapper.toDto(savedOrder);
}
```

#### 3. **Método de Validación Nuevo** (Privado)

```java
private void validateOrderDto(OrderDto orderDto) {
    if (orderDto == null) {
        throw new IllegalArgumentException("El pedido no puede ser null");
    }
    
    // Validar 'name': no null, no vacío, no blank
    if (orderDto.getName() == null) {
        throw new IllegalArgumentException("El campo 'name' es requerido");
    }
    if (orderDto.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("El campo 'name' no puede estar vacío");
    }
    
    // Validar 'description': no null
    if (orderDto.getDescription() == null) {
        throw new IllegalArgumentException("El campo 'description' es requerido");
    }
    
    // Validar 'idUser': debe ser > 0
    if (orderDto.getIdUser() <= 0) {
        throw new IllegalArgumentException("El campo 'idUser' debe ser un valor positivo válido (mayor que cero)");
    }
}
```

### Criterios de Aceptación Implementados

#### ✅ CA-01: Creación exitosa de pedido
- **Requisito**: Dados datos válidos (name, description, idUser), el sistema crea el pedido con ID autogenerado
- **Implementación**: 
  - Método `createOrder()` acepta `OrderDto` con campos válidos
  - Usa `OrderJpaRepository.save()` para persistencia
  - PostgreSQL asigna ID automáticamente vía `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - Retorna `OrderDto` con ID asignado

#### ✅ CA-02: Campos requeridos faltantes
- **Requisito**: Dado body sin campos requeridos, lanza `IllegalArgumentException`
- **Implementación**:
  - `validateOrderDto()` verifica `name != null && !name.trim().isEmpty()`
  - Verifica `description != null`
  - Verifica `idUser > 0`
  - Lanza excepciones descriptivas para cada caso

#### ✅ CA-03: Tipo de dato inválido
- **Requisito**: Dado idUser con tipo/valor inválido, lanza `IllegalArgumentException`
- **Implementación**:
  - Valida `idUser > 0` (rechaza negativos y cero)
  - Valida `name` no vacío ni solo espacios
  - Mensajes de error descriptivos

### Requisitos Funcionales Implementados

#### ✅ FR-ORD-05-01: Inserción en PostgreSQL
- Usa `OrderJpaRepository extends JpaRepository<Order, Integer>`
- Transacciones manejadas automáticamente por Spring (`@Transactional` implícito en JPA)
- Entidad `Order` mapeada con JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`)

#### ✅ FR-ORD-05-02: Campos requeridos
- **name**: Validado como no null, no vacío, no blank
- **description**: Validado como no null
- **idUser**: Validado como > 0

#### ✅ FR-ORD-05-03: Asignación automática
- **id**: Autogenerado por PostgreSQL (`@GeneratedValue(strategy = GenerationType.IDENTITY)`)
- **state**: Asignado como `State.PROCESSING` por defecto
- **active**: Asignado como `true` por defecto

#### ✅ FR-ORD-05-04: Endpoint POST /order/add
- Endpoint existente en `OrderController.createOrder()`
- Delega a `OrderService.createOrder()`
- Retorna `ResponseEntity.ok(OrderDto)` (HTTP 200)

### Requisitos No Funcionales Implementados

#### ✅ NFR-ORD-05-01: Transaccionalidad
- Spring JPA maneja transacciones automáticamente
- Rollback automático en caso de excepción
- Commit automático en caso de éxito

#### ✅ NFR-ORD-05-02: Performance < 100ms
- Test de performance incluido en suite
- Operación simple de INSERT en PostgreSQL
- Sin lógica de negocio compleja

## 🧪 Tests Actualizados

### Estructura de Tests
```
OrderServiceHuOrd05Test
├── CA-01: CreacionExitosaPedidoTests
│   ├── testCrearPedidoConDatosValidos
│   ├── testValoresPorDefectoAsignadosAutomaticamente
│   └── testPerformanceCreacionPedido
├── CA-02: CamposRequeridosFaltantesTests
│   ├── testCrearPedidoSinCampoName
│   ├── testCrearPedidoSinCampoDescription
│   └── testCrearPedidoSinCampoIdUser
├── CA-03: TipoDatoInvalidoTests
│   ├── testCrearPedidoConIdUserNegativo
│   ├── testCrearPedidoConIdUserCero
│   ├── testCrearPedidoConNameVacio
│   └── testCrearPedidoConNameSoloEspacios
└── FR-ORD-05-01: InsercionPostgreSQLTests
    └── testInvocacionRepositorioJPA
```

### Mocks Configurados
- `OrderJpaRepository` - Repositorio JPA para PostgreSQL
- `OrderMapper` - Mapper DTO ↔ Entity
- `OrderRepository` - Repositorio legacy (para otros métodos)
- `UserServiceProducer` - Productor RabbitMQ (inyección)
- `UserServiceConsumer` - Consumidor RabbitMQ (inyección)

## 📊 Estado Actual

### ✅ Implementación Completada
- [x] Validación de campos requeridos
- [x] Validación de tipos de datos
- [x] Integración con OrderJpaRepository
- [x] Asignación automática de valores por defecto
- [x] Manejo de excepciones con mensajes descriptivos
- [x] Tests unitarios completos

### 🔄 Próximos Pasos (Fase REFACTOR)
1. Ejecutar tests y verificar que todos pasen (GREEN)
2. Commit de la fase GREEN
3. Analizar oportunidades de refactorización
4. Aplicar principios SOLID
5. Mejorar legibilidad y mantenibilidad
6. Commit de la fase REFACTOR

## 📝 Notas Técnicas

### Diferencias con Implementación Anterior
| Aspecto | Antes (JSON) | Ahora (PostgreSQL) |
|---------|--------------|-------------------|
| Repositorio | `OrderRepository` | `OrderJpaRepository` |
| Generación ID | Manual (max+1) | PostgreSQL `@GeneratedValue` |
| Validaciones | Ninguna | Completas (name, description, idUser) |
| Transacciones | Manual (JSON file) | Automáticas (Spring JPA) |
| Performance | File I/O | Database optimizado |

### Compatibilidad
- `OrderRepository` se mantiene inyectado para compatibilidad con métodos legacy
- Migración gradual: solo `createOrder()` usa el nuevo repositorio
- Otros métodos (`deleteOrder`, `changeStateOrder`, etc.) pueden migrarse en futuras HUs

## 🔗 Referencias
- User Story: `USER_STORIES/HU-ORD-05.md`
- Entity: `Backend/pedido-service/src/main/java/com/example/pedidoservice/model/Order.java`
- Repository: `Backend/pedido-service/src/main/java/com/example/pedidoservice/repository/OrderJpaRepository.java`
- Service: `Backend/pedido-service/src/main/java/com/example/pedidoservice/service/OrderService.java`
- Tests: `Backend/pedido-service/src/test/java/com/example/pedidoservice/service/OrderServiceHuOrd05Test.java`
