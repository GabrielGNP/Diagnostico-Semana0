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

// ...existing code...
| ID | Feature | Scenario | Metodo | Gherkin (resumen) | Url | Datos de prueba | Resultado esperado | Resultado (Paso/Fallo) | Imagen | Observaciones |
|----|---------|----------|--------|------------------|-----|-----------------|-------------------|------------------------|--------|---------------|
| IC-01 | OrderController | Crear pedido con datos validos retorna 201 | POST | Given OrderDto valido / When POST /orders / Then 201 y Location | http://localhost:8082/orders | name Test Order, idUser=1 | 201 Created y body con state PROCESSING | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-01-create-order-201.jpg) |  |
| IC-02 | OrderController | Crear pedido con name vacio retorna 400 | POST | Given OrderDto invalido / When POST /orders / Then 400 con errores | http://localhost:8082/orders | name="" | 400 Bad Request con errores | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-02-create-order-without.data.jpg) |  |
| IC-03 | OrderController | Crear pedido sin idUser retorna 400 | POST | Given OrderDto invalido / When POST /orders / Then 400 | http://localhost:8082/orders | idUser=null | 400 Bad Request | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-03-create-order-without-idUser.jpg) |  |
| IC-04 | OrderController | Obtener pedido por ID existente retorna 200 | GET | Given pedido id=1 existe / When GET /orders/1 / Then 200 y body | http://localhost:8082/orders/1 | orderId=1 | 200 OK con pedido | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-04-get-order-by-id.jpg) |  |
| IC-05 | OrderController | Obtener pedido por ID inexistente retorna 404 | GET | Given pedido no existe / When GET /orders/999 / Then 404 | http://localhost:8082/orders/999 | orderId=999 | 404 Not Found con error | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-05-get-order-by-inexisting-id.jpg) |  |
| IC-06 | OrderController | Listar pedidos activos retorna lista con mas de 3 ordenes activas | GET | Given 3 pedidos activos / When GET /orders / Then lista con 3 | http://localhost:8082/orders | 3 pedidos activos | 200 OK con 3 pedidos, ejecuta el llamado si hay más de 3 ordenes existentes | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-06-get-order-3-available.jpg) |  |
| IC-07 | OrderController | Listar pedidos sin datos retorna lista vacia | GET | Given sin pedidos activos / When GET /orders / Then lista vacia | http://localhost:8082/orders | sin pedidos | 200 OK con lista vacia | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-07-get-only-available-orders.jpg) |  |
| IC-08 | OrderController | Listar pedidos por usuario retorna filtrado | GET | Given pedidos userId=5 / When GET /orders/user/5 / Then todos idUser=5 | http://localhost:8082/orders/user/5 | userId=5 | 200 OK solo userId=5 | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-08-get-order-with-existing-user.jpg) |  |
| IC-09 | OrderController | Listar todos los pedidos incluye inactivos | GET | Given activos e inactivos / When GET /orders/all / Then incluye active=false | http://localhost:8082/orders/all | mezcla de pedidos | 200 OK con inactivos | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-09-get-existing-and-non-existing-orders.jpg) |  |
| IC-10 | OrderController | Eliminar pedido (soft-delete) retorna 204 | DELETE | Given pedido id=1 / When DELETE /orders/1 / Then 204 y active=false | http://localhost:8082/orders/1 | orderId=1 | 204 No Content | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-10-soft-delete-order.jpg) |  |
| IC-11 | OrderController | Eliminar pedido inexistente retorna 404 | DELETE | Given pedido no existe / When DELETE /orders/999 / Then 404 | http://localhost:8082/orders/999 | orderId=999 | 404 Not Found | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-11-soft-delete-inexisting-order.jpg) |  |
| IC-12 | OrderController | Cambiar estado de pedido retorna 200 | PATCH | Given pedido state PROCESSING / When PATCH /orders/1 / Then 200 y state DELIVERED | http://localhost:8082/orders/1 | state=DELIVERED | 200 OK y state actualizado | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-12-patch.update-order-state.jpg) |  |
| IC-13 | OrderController | Obtener pedido con informacion de usuario | GET | Given pedido idUser=10 / When GET /orders/1/user / Then OrderWithUserDto | http://localhost:8082/orders/1/user | idUser=10 | 200 OK con OrderWithUserDto | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/IC-13-get-order-with-user.jpg) |  |
| EH-01 | GlobalExceptionHandler | OrderNotFoundException retorna 404 con ErrorResponse | GET | Given OrderNotFoundException / When handler / Then 404 y error Not Found | http://localhost:8082/orders/999 | id=999 | 404 con ErrorResponse | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-01-get-error-non-existing-order.jpg) |  |
| EH-02 | GlobalExceptionHandler | IllegalArgumentException retorna 400 con ErrorResponse | POST | Given IllegalArgumentException / When handler / Then 400 | http://localhost:8082/orders | mensaje invalido | 400 Bad Request | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-02-update-order-with-ilegal-argument.jpg) |  |
| EH-03 | GlobalExceptionHandler | Validacion fallida retorna 400 con errores | POST | Given request invalida / When POST /orders / Then 400 y validationErrors | http://localhost:8082/orders | name="", idUser=null | 400 y validationErrors | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-03-update-order-with-invalid-params.jpg) |  |
| EH-04 | GlobalExceptionHandler | JSON malformado retorna 400 | POST | Given JSON invalido / When POST /orders / Then 400 y message esperado | http://localhost:8082/orders | body invalido | 400 con message | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-04-update-with-wrong-json.jpg) |  |
| EH-05 | GlobalExceptionHandler | Error de creacion retorna 500 | POST | Given OrderCreationException / When handler / Then 500 | http://localhost:8082/orders | mensaje error | 500 Internal Server Error | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-05-update-order-with-server-error.jpg) |  |
| EH-06 | GlobalExceptionHandler | Excepcion generica retorna 500 | POST | Given RuntimeException / When handler / Then 500 y message generico | http://localhost:8082/orders | mensaje inesperado | 500 con message generico | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/EH-06-update-order-with-generic-error.jpg) |  |
| FLOW-01 | Integration Flow | Crear y recuperar pedido end-to-end, la configución de rabbit está en RabbitMQConfig.java | POST, GET | Given BD vacia / When crear y obtener / Then campos correctos | http://localhost:8082/orders ; http://localhost:8082/orders/{id} | name=E2E Test, idUser=1 | Pedido recuperado con state PROCESSING | Paso ✅ | [Evidencia 1](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/FLOW-1-end-to-end-edit-order.jpg) [Evidencia 2](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/FLOW-1-end-to-end-get-order.jpg) | El proyecto tiene la configuración de Rabbit activa, se hace prueba end-to-end |
| FLOW-02 | Integration Flow | Soft-delete no aparece en listado activo la configución de rabbit está en RabbitMQConfig.java | DELETE, GET | Given pedido activo / When delete y listar / Then no aparece | http://localhost:8082/orders/1 ; http://localhost:8082/orders | orderId=1 | Pedido no aparece en activos | Paso ✅ | [Evidencia 1](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/FLOW-2-end-to-end-delete-order.jpg) [Evidencia 2](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/FLOW-2-end-to-end-get-order-after-delete.jpg) | El proyecto tiene la configuración de Rabbit activa, se hace prueba end-to-end, y cómo la orden fue borrada con soft-delete aparece cómo active= false |
| FLOW-03 | Integration Flow | Transicion de estados completa | PATCH | Given pedido PROCESSING / When cambiar estados / Then state DELIVERED | http://localhost:8082/orders/1 | estados PROCESSING -> DELIVERED | Pedido state DELIVERED | Paso ✅ | [Evidencia](https://github.com/Sofka-U/Diagnostico-Semana0/blob/develop/Semana%203%20-%20Actual/POSTMAN-EVIDENCES/FLOW-13-ende-to-end-update-order.jpg) |  |
// ...existing code...