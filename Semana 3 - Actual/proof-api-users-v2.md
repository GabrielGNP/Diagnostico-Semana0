# Proof API - Orders (Actividad 3.2)

Este documento genera casos de prueba en lenguaje Gherkin (Given/When/Then) y los organiza en una matriz de pruebas. La matriz incluye un campo de resultado para ejecucion manual con valores Paso/Fallo.

## Metodología de la tabla

Esta tabla contiene los siguientes valores a ser referenciados

**1. ID** Inventario de escenarios: enumera IDs únicos y agrupa por Feature.

**2. Scenario** Corresponde a la explicación de la implementación de la prueba

**3. Método** Consiste en la petición de la API REST

**4. Resumen Gherkin** Sintesis del caso redactado en lenguaje Gherkin

**5. URL** La URI necesaria para ejecutar la prueba manualmente

**6. Datos de prueba** Datos esperados de acuerdo a criterios de prueba implementados desde el taller 2

**7. Resultado esperado** Resultado deseado para aplicar la prueba

**8. Resultado (Pasó/falló)** Resultado obtenido tras ejecución manual

**9. Imagen** Link con la imagen de la ejecución manual, las imágenes tendrán su correspondiente link en el repositorio

Además de la tabla adjunta, los resultados de la prueba los podrás consultar en [el siguiente documento](https://docs.google.com/spreadsheets/d/1rmoi_-cpBNbqephP62vb6UHY72yxVD1bVuJeE1nmrpc/edit?usp=sharing)

este será un link de google sheets que puede ser consultado para ver los resultados de las pruebas manuales

## Matriz de Pruebas 

| ID | Feature | Scenario | Metodo | Gherkin (resumen) | Url | Datos de prueba | Resultado esperado | Resultado (Paso/Fallo) | Observaciones |
|----|---------|----------|--------|------------------|-----|-----------------|-------------------|------------------------|---------------|
| UM-01 | OrderMapper | Convertir Order entity a OrderDto exitosamente | N/A | Given Order valido / When toDto / Then campos iguales | http://localhost:8082 | Order con id=1, name Test, idUser=10, state PROCESSING | OrderDto con mismos valores |  |  |
| UM-02 | OrderMapper | Convertir Order null retorna null | N/A | Given Order null / When toDto / Then null | http://localhost:8082 | Order null | null |  |  |
| UM-03 | OrderMapper | Convertir OrderDto a Order entity exitosamente | N/A | Given OrderDto valido / When toEntity / Then campos iguales | http://localhost:8082 | OrderDto name New Order, idUser=5 | Order con campos iguales |  |  |
| UM-04 | OrderMapper | Convertir OrderDto null retorna null | N/A | Given OrderDto null / When toEntity / Then null | http://localhost:8082 | OrderDto null | null |  |  |
| UE-01 | OrderEnrichmentFacade | Enriquecer orden con datos de usuario exitosamente | N/A | Given OrderDto valido / When enrich / Then OrderWithUserDto con user | http://localhost:8082 | idUser=10, UserResponse valido | OrderWithUserDto con user |  |  |
| UE-02 | OrderEnrichmentFacade | Enriquecer orden null retorna null | N/A | Given OrderDto null / When enrich / Then null | http://localhost:8082 | OrderDto null | null |  |  |
| UE-03 | OrderEnrichmentFacade | Enriquecer orden cuando el servicio de usuario falla | N/A | Given OrderDto valido / When enrich / Then user null | http://localhost:8082 | idUser=999 | OrderWithUserDto con user null |  |  |
| US-01 | UserEnrichmentService | Obtener informacion de usuario exitosamente | N/A | Given IUserInfoClient mock / When fetchUserInfo / Then UserResponse | http://localhost:8082 | userId=5 | Retorna UserResponse |  |  |
| US-02 | UserEnrichmentService | Manejar error al obtener informacion de usuario | N/A | Given mock lanza excepcion / When fetchUserInfo / Then null | http://localhost:8082 | userId=5 | Retorna null |  |  |
| UC-01 | UserResponseCache | Almacenar respuesta de usuario en cache | N/A | Given cache vacio / When store / Then disponible | http://localhost:8082 | UserResponse id=10 | Respuesta en cache |  |  |
| UC-02 | UserResponseCache | Ignorar almacenamiento de respuesta null | N/A | Given cache vacio / When store null / Then cache vacio | http://localhost:8082 | null | Cache sin cambios |  |  |
| UC-03 | UserResponseCache | Esperar y obtener respuesta dentro del timeout | N/A | Given cache con respuesta / When awaitResponse / Then retorna y elimina | http://localhost:8082 | timeout=1000 | Retorna UserResponse y elimina |  |  |
| UC-04 | UserResponseCache | Timeout al esperar respuesta no disponible | N/A | Given cache vacio / When awaitResponse / Then null por timeout | http://localhost:8082 | userId=999, timeout=100 | Retorna null |  |  |
| UC-05 | UserResponseCache | Manejar interrupcion durante espera | N/A | Given cache vacio / When awaitResponse con interrupcion / Then null e interrupted | http://localhost:8082 | timeout=5000 | Retorna null y flag interrupted |  |  |
| DTO-01 | DTOs | OrderStateUpdateDto constructor y getters | N/A | Given state DELIVERED / When new dto / Then getState DELIVERED | http://localhost:8082 | state=DELIVERED | Getter retorna DELIVERED |  |  |
| DTO-02 | DTOs | OrderStateUpdateDto setter | N/A | Given state PROCESSING / When setState / Then getState actualizado | http://localhost:8082 | state=TRAVELING_TO_WAREHOUSE | Getter retorna nuevo state |  |  |
| DTO-03 | DTOs | UserRequest constructor y accessors | N/A | Given userId=25 / When new dto / Then getUserId y toString | http://localhost:8082 | userId=25 | Getter 25 y toString contiene userId=25 |  |  |
| DTO-04 | DTOs | OrderWithUserDto setters | N/A | Given dto vacio / When set all / Then getters iguales | http://localhost:8082 | valores en todos los campos | Getters con valores seteados |  |  |
| DTO-05 | DTOs | ErrorResponse builder completo | N/A | Given valores completos / When builder / Then campos correctos | http://localhost:8082 | timestamp, status=400, error, message | ErrorResponse con campos correctos |  |  |
| DTO-06 | DTOs | ErrorResponse con validationErrors | N/A | Given mapa errores / When builder / Then validationErrors igual | http://localhost:8082 | validationErrors map | Mapa devuelto |  |  |
| IC-01 | OrderController | Crear pedido con datos validos retorna 201 | POST | Given OrderDto valido / When POST /orders / Then 201 y Location | http://localhost:8082/orders | name Test Order, idUser=1 | 201 Created y body con state PROCESSING |  |  |
| IC-02 | OrderController | Crear pedido con name vacio retorna 400 | POST | Given OrderDto invalido / When POST /orders / Then 400 con errores | http://localhost:8082/orders | name="" | 400 Bad Request con errores |  |  |
| IC-03 | OrderController | Crear pedido sin idUser retorna 400 | POST | Given OrderDto invalido / When POST /orders / Then 400 | http://localhost:8082/orders | idUser=null | 400 Bad Request |  |  |
| IC-04 | OrderController | Obtener pedido por ID existente retorna 200 | GET | Given pedido id=1 existe / When GET /orders/1 / Then 200 y body | http://localhost:8082/orders/1 | orderId=1 | 200 OK con pedido |  |  |
| IC-05 | OrderController | Obtener pedido por ID inexistente retorna 404 | GET | Given pedido no existe / When GET /orders/999 / Then 404 | http://localhost:8082/orders/999 | orderId=999 | 404 Not Found con error |  |  |
| IC-06 | OrderController | Listar pedidos activos retorna lista | GET | Given 3 pedidos activos / When GET /orders / Then lista con 3 | http://localhost:8082/orders | 3 pedidos activos | 200 OK con 3 pedidos |  |  |
| IC-07 | OrderController | Listar pedidos sin datos retorna lista vacia | GET | Given sin pedidos activos / When GET /orders / Then lista vacia | http://localhost:8082/orders | sin pedidos | 200 OK con lista vacia |  |  |
| IC-08 | OrderController | Listar pedidos por usuario retorna filtrado | GET | Given pedidos userId=5 / When GET /orders/user/5 / Then todos idUser=5 | http://localhost:8082/orders/user/5 | userId=5 | 200 OK solo userId=5 |  |  |
| IC-09 | OrderController | Listar todos los pedidos incluye inactivos | GET | Given activos e inactivos / When GET /orders/all / Then incluye active=false | http://localhost:8082/orders/all | mezcla de pedidos | 200 OK con inactivos |  |  |
| IC-10 | OrderController | Eliminar pedido (soft-delete) retorna 204 | DELETE | Given pedido id=1 / When DELETE /orders/1 / Then 204 y active=false | http://localhost:8082/orders/1 | orderId=1 | 204 No Content |  |  |
| IC-11 | OrderController | Eliminar pedido inexistente retorna 404 | DELETE | Given pedido no existe / When DELETE /orders/999 / Then 404 | http://localhost:8082/orders/999 | orderId=999 | 404 Not Found |  |  |
| IC-12 | OrderController | Cambiar estado de pedido retorna 200 | PATCH | Given pedido state PROCESSING / When PATCH /orders/1 / Then 200 y state DELIVERED | http://localhost:8082/orders/1 | state=DELIVERED | 200 OK y state actualizado |  |  |
| IC-13 | OrderController | Obtener pedido con informacion de usuario | GET | Given pedido idUser=10 / When GET /orders/1/user / Then OrderWithUserDto | http://localhost:8082/orders/1/user | idUser=10 | 200 OK con OrderWithUserDto |  |  |
| EH-01 | GlobalExceptionHandler | OrderNotFoundException retorna 404 con ErrorResponse | GET | Given OrderNotFoundException / When handler / Then 404 y error Not Found | http://localhost:8082/orders/999 | id=999 | 404 con ErrorResponse |  |  |
| EH-02 | GlobalExceptionHandler | IllegalArgumentException retorna 400 con ErrorResponse | POST | Given IllegalArgumentException / When handler / Then 400 | http://localhost:8082/orders | mensaje invalido | 400 Bad Request |  |  |
| EH-03 | GlobalExceptionHandler | Validacion fallida retorna 400 con errores | POST | Given request invalida / When POST /orders / Then 400 y validationErrors | http://localhost:8082/orders | name="", idUser=null | 400 y validationErrors |  |  |
| EH-04 | GlobalExceptionHandler | JSON malformado retorna 400 | POST | Given JSON invalido / When POST /orders / Then 400 y message esperado | http://localhost:8082/orders | body invalido | 400 con message |  |  |
| EH-05 | GlobalExceptionHandler | Error de creacion retorna 500 | POST | Given OrderCreationException / When handler / Then 500 | http://localhost:8082/orders | mensaje error | 500 Internal Server Error |  |  |
| EH-06 | GlobalExceptionHandler | Excepcion generica retorna 500 | POST | Given RuntimeException / When handler / Then 500 y message generico | http://localhost:8082/orders | mensaje inesperado | 500 con message generico |  |  |
| MSG-01 | Messaging | Enviar solicitud de informacion de usuario | N/A | Given producer mock / When requestUserInfo / Then convertAndSend invocado | http://localhost:8082 | userId=10 | exchange y routing correctos |  |  |
| MSG-02 | Messaging | Recibir respuesta de usuario via listener | N/A | Given consumer mock / When receiveUserResponse / Then cache.store | http://localhost:8082 | UserResponse id=10 | cache.store invocado |  |  |
| MSG-03 | Messaging | Obtener respuesta delegando a cache | N/A | Given cache.awaitResponse retorna user / When getUserResponse / Then retorna user | http://localhost:8082 | userId=10, timeout=3000 | Retorna UserResponse |  |  |
| MSG-04 | Messaging | Orquestar solicitud y espera de respuesta | N/A | Given producer y consumer mock / When fetchUserInfo / Then request+getResponse | http://localhost:8082 | userId=10, timeout=3000 | Retorna UserResponse |  |  |
| MSG-05 | Messaging | Manejar error en orquestacion | N/A | Given producer lanza excepcion / When fetchUserInfo / Then null y warning | http://localhost:8082 | userId=10 | Retorna null |  |  |
| FLOW-01 | Integration Flow | Crear y recuperar pedido end-to-end | POST, GET | Given BD vacia / When crear y obtener / Then campos correctos | http://localhost:8082/orders ; http://localhost:8082/orders/{id} | name=E2E Test, idUser=1 | Pedido recuperado con state PROCESSING |  |  |
| FLOW-02 | Integration Flow | Soft-delete no aparece en listado activo | DELETE, GET | Given pedido activo / When delete y listar / Then no aparece | http://localhost:8082/orders/1 ; http://localhost:8082/orders | orderId=1 | Pedido no aparece en activos |  |  |
| FLOW-03 | Integration Flow | Transicion de estados completa | PATCH | Given pedido PROCESSING / When cambiar estados / Then state DELIVERED | http://localhost:8082/orders/1 | estados PROCESSING -> DELIVERED | Pedido state DELIVERED |  |  |
