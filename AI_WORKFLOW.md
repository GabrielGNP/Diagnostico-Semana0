Creame un proyecto de MVP con microservicios con java. Debe tener un front con react versión 16.13.1, java 25, spring 4.0.2. Instala las dependencias para agregar testing con Mokito. La palicación debe dockerizarce así que creame el archivo docker-compose.yml.

version java: 25
version spring: 4.0.2
Mokito
version react: 16.13.1


La estructura del proyecto se debe dibidir en subcarpetas:
Carpeta Frontend,
Carpeta Backend,
Carpeta Test,


En la carpeta backend crea dos sub carpetas, una usuario-service y otra pedido-service.
Dentro de ambas carpetas crea un proyecto con las versiones de spring y java antes mencionadas con las siguientes dependencias:

Spring Web
Spring for RabbitMQ
Lombok
Validator

El resultado final deberá ser dos carpetas usuario-service y pedido-service con sus respectivos proyectos de spring inicializados.

Dentro de ambas sub carpetas deben existir las siguiente subcarpetas:

controller
dto
model
repository
service

Adicionalmente en la carpeta de resources debe existir un archivo pedidos.json y usuarios.json

En la carpeta frontend debes crear un proyecto de react en la versión especificada con vite

Crea un archivo docker para cada proyecto backend y frontend


Consulta antes de crear el proyecto y espera mi confirmación