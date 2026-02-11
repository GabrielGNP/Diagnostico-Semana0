# Metodología
- Revisión inicial del contexto del proyecto y los documentos clave proporcionados.
- Definición de los casos de uso
- evaluación de propuesta diseñada por IA y reiteraciones sobre propuestas
- implementación
- pruebas
- integración

# Interacciones clave
- Creación del proyecto base
- Diseño de la interfaz de usuario
- Creación de los endpoints de servicios
- Creación de los tests unitarios
- Creación de los tests de integración

# Documentos clave y contextualización

## stack tecnológico y versiones
java: 25
maven: 3.9.12
Spring 4.0.2
React 16.13.1
Vite 2.9.0
rabitMQ 3
node 18

## Imágenes utilizadsa en docker
- node:18-alpine
- maven:3.9.12-eclipse-temurin-25-alpine
- eclipse-temurin:25-alpine
- nginx:alpine
- rabbitmq:3-management

## contexto del proyecto

El sistema es un fragmento de un sistema más complejo que busca ser una plataforma de gestión de pedidos y usuarios que permite a los usuarios crear, actualizar y eliminar sus cuentas, así como gestionar sus pedidos a través de una serie de estados predefinidos. La aplicación está diseñada con una arquitectura de microservicios para garantizar la escalabilidad y la mantenibilidad.

Arquitectura de mircroservicios
Validaciones con Spring Validator
Aplicación Dockerizada
cada servicio debe ser un contenedor independiente
Comunicación entre servicios a través de RabbitMQ


## modelo de datos
users.json
{
    int id
    String name
    String password
    String mail
    boolean active
}
orders.json
{
    int id
    String name
    String description
    int idUser
    State state (un enum [PROCESSING, TRAVELINGTOWAREHOUSE, IN WAREHOUSE, TRAVELINGTOYOURHOUSE, ONTHESTREET, DELIVERED, CANCELED])
    boolean active
}

Cada pedido debe estar asociado a un usuario mediante idUser

## procedimientos importantes
servicio Pedidos
- CreateOrder (procedimiento que permite crear nuevos pedidos)
- DeleteOrder (procedimiento que permite eliminar pedidos existentes. La eliminación es lógica, cambiando el campo active a false)
- ChangeStateOrder (procedimiento que permite cambiar el estado de un pedido. El estado se actualiza según el flujo definido en el enum State)
- ListOrderByIdUser (procedimiento que permite listar los pedidos asociados a un usuario específico, utilizando el campo idUser para filtrar los resultados)
- ShowOrderById (procedimiento que permite mostrar los detalles de un pedido específico utilizando su id)

Servicio Usuarios
- CreateUser (procedimiento que permite crear nuevos usuarios)
- DeleteUser (procedimiento que permite eliminar usuarios existentes. La eliminación es lógica, cambiando el campo active a false)
- UpdateUser (procedimiento que permite actualizar la información de un usuario existente)
- ReadUserById (procedimiento que permite leer los detalles de un usuario específico utilizando su id)

## endpoints de servicios
ENDPOINTS SERVICIO PEDIDOS
- POST /orders/add
- DELETE /orders/{id}
- GET /orders/{id}
- GET /orders/user/{idUser}
- PATCH /orders/{id}/state

ENDPOINTS SERVICIO USUARIOS
- POST /users/add
- DELETE /users/{id}
- GET /users/{id}
- PATCH /users/{id}

# Dinámicas de interacción
## Revisión inicial del contexto del proyecto y los documentos clave proporcionados.
El contexto puede ser revisado a través de los documentos proporcionados, como el modelo de datos, los procedimientos importantes y los endpoints de servicios. Esta revisión es crucial para comprender el alcance del proyecto, las funcionalidades requeridas y las interacciones entre los diferentes componentes del sistema.
El contexto debe permanecer intacto almenos que sea estrictamente necesario realizar algún cambio para la correcta implementación de las funcionalidades solicitadas.

## Definición de los casos de uso
Se define las funcionalidades principales del sistema, que incluyen la gestión de usuarios y pedidos. Cada caso de uso se detalla con sus respectivos procedimientos y endpoints, asegurando que todas las operaciones necesarias estén cubiertas.

## evaluación de propuesta diseñada por IA y reiteraciones sobre propuestas
Se elabora un prompt en función de los documentos clave, el contexto del proyecto y el caso de uso trabajado, para solicitar a la IA una propuesta de implementación. La propuesta se evalúa en función de su alineación con los requisitos del proyecto, la claridad de la solución propuesta y su viabilidad técnica. Si es necesario, se realizan reiteraciones sobre la propuesta para mejorarla y ajustarla a las necesidades específicas del proyecto.
Este proceso se repite hasta obtener una propuesta satisfactoria que cumpla con los requisitos del proyecto y el caso de uso.

## implementación
Una vez que se ha obtenido una propuesta satisfactoria, se procede a la implementación de las funcionalidades definidas en los casos de uso. Esto implica escribir el código necesario para crear los endpoints, implementar la lógica de negocio y asegurar que la comunicación entre los servicios se realice correctamente a través de RabbitMQ.
Solo si es necesario, en esta etapa el desarrollador hace los cambios necesarios en el código generado por la IA para que se ajuste a los requisitos del proyecto y el caso de uso, asegurando que la implementación sea funcional y eficiente.

## pruebas
Una vez implementadas las funcionalidades, se realizan pruebas para verificar que todo funcione correctamente. Esto incluye pruebas unitarias para cada componente, pruebas de integración para asegurar que los servicios se comuniquen correctamente y pruebas funcionales para validar que los casos de uso se ejecuten según lo esperado.
También se verificará que los servicios Dockerizados funcionen correctamente en sus contenedores independientes y que la comunicación a través de RabbitMQ sea efectiva.

## integración
Si las pruebas son exitosas, se procede a integrar las funcionalidades implementadas en el sistema completo. Esto implica asegurarse de que los nuevos componentes se integren sin problemas con los existentes y que el sistema en su conjunto funcione de manera coherente y eficiente.