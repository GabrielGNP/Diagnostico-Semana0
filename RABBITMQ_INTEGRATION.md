# Integración de RabbitMQ - Comunicación entre Servicios

## Descripción General

Se ha implementado un sistema de comunicación asíncrona entre el servicio de pedidos (`pedido-service`) y el servicio de usuarios (`usuario-service`) usando **RabbitMQ**. Cuando se solicita mostrar los pedidos de un usuario, el servicio de pedidos solicita automáticamente la información del usuario a través de colas de mensajes.

## Arquitectura

### Flujo de Comunicación

```
1. Cliente → pedido-service: GET /order/user/{userId}/with-info
2. pedido-service (Producer) → RabbitMQ: UserRequest
3. usuario-service (Consumer) Lee: UserRequest
4. usuario-service (Producer) → RabbitMQ: UserResponse
5. pedido-service (Consumer) Lee: UserResponse
6. pedido-service → Cliente: OrderWithUserDto (con info del usuario)
```

### Colas y Exchanges

**Exchange:** `user-exchange` (DirectExchange)

**Colas:**
- `user-request-queue`: Solicitudes de información de usuarios
- `user-response-queue`: Respuestas con información de usuarios

**Routing Keys:**
- `user.request`: Para enviar solicitudes
- `user.response`: Para recibir respuestas

## Componentes Implementados

### En pedido-service

#### 1. **RabbitMQConfig.java**
Configuración de colas, exchange y bindings para RabbitMQ.

#### 2. **UserRequest.java**
DTO que representa una solicitud de información de usuario.

```java
public class UserRequest {
    private int userId;
}
```

#### 3. **UserResponse.java**
DTO que representa la respuesta con información del usuario.

```java
public class UserResponse {
    private Integer id;
    private String name;
    private String mail;
    private boolean active;
}
```

#### 4. **UserServiceProducer.java**
Componente que envía solicitudes de usuario a través de RabbitMQ.

```java
public void requestUserInfo(int userId) {
    UserRequest request = new UserRequest(userId);
    rabbitTemplate.convertAndSend(
        USER_EXCHANGE, 
        USER_REQUEST_ROUTING_KEY, 
        request
    );
}
```

#### 5. **UserServiceConsumer.java**
Componente que escucha respuestas del usuario-service y las almacena para que el OrderService las pueda usar.

```java
public UserResponse getUserResponse(int userId, long timeoutMs) {
    // Espera hasta `timeoutMs` por la respuesta
    // Retorna la respuesta cuando llega
}
```

#### 6. **OrderWithUserDto.java**
DTO que combina información de pedido con información de usuario.

#### 7. **OrderService.java**
Actualizado con nuevo método:

```java
public List<OrderWithUserDto> listOrdersByIdUserWithUserInfo(int idUser) {
    // Solicita usuario a través de RabbitMQ
    // Espera la respuesta con timeout de 3 segundos
    // Retorna pedidos con info del usuario
}
```

#### 8. **OrderController.java**
Nuevo endpoint agregado:

- **GET** `/order/user/{idUser}/with-info` → Retorna pedidos con info del usuario

### En usuario-service

#### 1. **RabbitMQConfig.java**
Configuración idéntica a pedido-service para mantener sincronización de colas.

#### 2. **UserRequest.java** y **UserResponse.java**
DTOs para mensajería (mismos que en pedido-service).

#### 3. **UserServiceProducer.java**
Productor que envía respuestas con información del usuario.

```java
public void sendUserResponse(UserResponse response) {
    rabbitTemplate.convertAndSend(
        USER_EXCHANGE,
        USER_RESPONSE_ROUTING_KEY,
        response
    );
}
```

#### 4. **UserServiceConsumer.java**
Consumer que escucha solicitudes de usuario y las procesa.

```java
@RabbitListener(queues = USER_REQUEST_QUEUE)
public void receiveUserRequest(UserRequest request) {
    // Busca el usuario en la BD
    // Envía la respuesta a través del Producer
}
```

#### 5. **UserRepository.java**
Nuevo servicio que encapsula la lógica de acceso a usuarios (antes estaba en la clase principal).

Métodos principales:
- `findById(int id)`: Busca usuario por ID
- `findByEmail(String email)`: Busca usuario por email
- `findAll()`: Obtiene todos los usuarios
- `save(User user)`: Guarda nuevo usuario
- `update(int id, User user)`: Actualiza usuario
- `deleteById(int id)`: Elimina usuario

#### 6. **User.java**
Modelo de usuario extraído a clase separada (antes era inner class).

#### 7. **UsuarioServiceApplication.java**
Refactorizado para usar UserRepository y UserServiceConsumer.

## Cómo Funciona

### Caso de Uso: Obtener Pedidos de un Usuario con Información

**Solicitud:**
```
GET http://localhost:8082/order/user/1/with-info
```

**Proceso:**

1. El controlador recibe la solicitud
2. OrderService.listOrdersByIdUserWithUserInfo(1) es llamado
3. UserServiceProducer envía mensaje `UserRequest(userId: 1)` a la cola `user-request-queue`
4. usuario-service recibe el mensaje en UserServiceConsumer
5. Busca el usuario en la BD (User ID 1)
6. usuario-service envía UserResponse con los datos del usuario a la cola `user-response-queue`
7. pedido-service recibe la respuesta en UserServiceConsumer (con timeout de 3 segundos)
8. OrderService combina los pedidos con la información del usuario
9. Retorna una lista de `OrderWithUserDto`

**Respuesta Ejemplo:**
```json
[
  {
    "id": 101,
    "name": "Pedido Laptop",
    "description": "Compra de laptop gaming",
    "idUser": 1,
    "state": "PROCESSING",
    "active": true,
    "user": {
      "id": 1,
      "name": "Juan García",
      "mail": "juan@example.com",
      "active": true
    }
  }
]
```

## Configuración de Entorno

El archivo `docker-compose.yml` ya tiene configurado:

```yaml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"      # AMQP port
    - "15672:15672"    # Management UI
  environment:
    - SPRING_RABBITMQ_HOST=rabbitmq
```

## Endpoints Disponibles

### pedido-service

- `GET /order/user/{idUser}` → Pedidos sin info del usuario (rápido)
- `GET /order/user/{idUser}/with-info` → Pedidos CON info del usuario (requiere 3s timeout)
- `GET /order/{id}` → Pedido específico
- `GET /order/all` → Todos los pedidos
- `POST /order/add` → Crear pedido
- `DELETE /order/{id}` → Eliminar pedido
- `PATCH /order/{id}` → Cambiar estado del pedido

### usuario-service

- `GET /users` → Todos los usuarios
- `GET /user/{id}` → Usuario por ID o email
- `POST /user/add` → Crear usuario
- `PUT /user/{id}` → Actualizar usuario completo
- `PATCH /user/{id}` → Actualizar parcialmente
- `DELETE /user/{id}` → Eliminar usuario

## Timeouts y Consideraciones de Desempeño

- **Timeout de respuesta:** 3000 ms (3 segundos)
- Si el usuario-service no responde en ese tiempo, el endpoint retorna pedidos sin información del usuario
- Para obtener solo el listado rápidamente, usa: `GET /order/user/{userId}` (sin `/with-info`)

## Próximas Mejoras Sugeridas

1. **Implementar tabla de usuarios en BD**: Cambiar de JSON a base de datos relacional
2. **Caché de usuarios**: Evitar solicitudes repetidas al usuario-service
3. **Patrón Saga**: Para transacciones distribuidas más complejas
4. **Dead Letter Queue**: Para manejo de mensajes no procesados
5. **Logs distribuidos**: Implementar ELK stack para rastrear requests entre servicios
6. **Circuit Breaker**: Para manejar fallos del usuario-service gracefully

## Debugging

### Ver estado de RabbitMQ
- Panel de administración: http://localhost:15672
- Usuario por defecto: guest
- Contraseña por defecto: guest

### Logs en los servicios
Busca mensajes como:
- "User request received"
- "User response received"
- "User response sent"

### Verificar colas
```bash
docker exec -it rabbitmq rabbitmqctl list_queues
```
