(prompt a Gemini)
Vamos a trabajar exclusivamente dentro de la carpeta pedido-service.

Dentro de esta carpeta encontraremos los siguientes directorios con sus respectivos archivos:

controller
dto
model
repository
service
mapper

Dentro de cada directorio encontraremos los siguientes archivos:

OrderController
OrderDto
Order
OrderRepository
OrderService
OrderMapper

Para cada uno implementaremos su respectiva funcionalidad descrita a continuación:

OrderController contendrá toda la api que expondrá los siguientes endpoints:

POST /order/add
DELETE /order/1
GET /order/1
GET /order/user/1
PATCH /order/1

Order contendrá la representación del objeto Order en formato java con los siguientes campos:

int id
String name
String description
int idUser
State state
boolean active

OrderDto contendrá la representación del objeto Order que en este caso quedará igual que el original.

State es un enumerado que va va dentro del directorio model que contiene los siguientes estados:

PROCESSING
TRAVELINGTOWAREHOUSE
IN WAREHOUSE
TRAVELINGTOYOURHOUSE
ONTHESTREET
DELIVERED
CANCELED

OrderMapper contendrá la funcionalidad de mapeo entre Order y OrderDto.

OrderService contendrá las siguientes funcionalidades:

CreateOrder
DeleteOrder
ChangeStateOrder
ListOrdersByIdUser
ShowOrderById

Por último OrderRepository contendrá la funcionalidad de persistencia, pero en lugar de usar una base de datos se usará el archivo /resources/orders.json 



Corrección de errores:

#OrderController
Spring no puede resolver cual es la variable que se pasa en la request http en el controller, debes indicar el nombre de la variable dentro de cada @PathVariable
