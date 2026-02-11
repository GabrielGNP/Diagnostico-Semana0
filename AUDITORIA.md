# Auditoría de Código: Diagnostico-Semana0

**Fecha:** 11 de febrero de 2026

Este documento detalla los hallazgos críticos de la revisión de código, enfocándose en los principios SOLID vulnerados y su impacto directo en la escalabilidad del sistema.

## 1. Persistencia Ineficiente y Bloqueante

### Hallazgo
Los repositorios `OrderRepository` y `UserRepository` implementan la persistencia mediante la lectura y escritura completa de archivos JSON en disco para **cada** operación (crear, leer, actualizar, borrar).
*   *Ejemplo:* Al guardar un pedido, `OrderService` llama a `save()`, que lee todo el archivo `orders.json`, deserializa la lista completa, modifica un elemento, serializa de nuevo la lista completa y sobreescribe el archivo.

### Principio Vulnerado
*   **Single Responsibility Principle (SRP):** El repositorio mezcla la lógica de acceso a datos con la lógica de bajo nivel de gestión de archivos (I/O) y serialización.
*   **Interface Segregation Principle (ISP):** (Indirectamente) El servicio se ve forzado a depender de una implementación que expone detalles de archivos.

### Impacto en la Escalabilidad (Crítico)
*   **Complejidad Temporal O(N):** El tiempo de respuesta crece linealmente con la cantidad de datos. Con 100 usuarios es rápido; con 10,000 usuarios será inusable.
*   **Cuello de Botella de I/O:** El acceso a disco es millones de veces más lento que el acceso a memoria. Las operaciones de escritura bloqueantes limitarán drásticamente el throughput (peticiones por segundo) que el sistema puede manejar.
*   **Bloqueo de Hilos:** Los hilos del servidor web quedarán bloqueados esperando I/O, agotando el pool de conexiones bajo carga.

---

## 2. Condición de Carrera (Race Condition) en Datos

### Hallazgo
El `OrderRepository` no implementa ningún mecanismo de sincronización o bloqueo optimista/pesimista para las operaciones de escritura.
*   *Escenario:* Si dos usuarios intentan actualizar o crear órdenes casi simultáneamente, la segunda operación leerá una versión del archivo que aún no tiene los cambios de la primera, y al guardar, sobrescribirá y perderá los datos de la primera transacción.

### Principio Vulnerado
*   No es un principio SOLID per se, pero viola principios fundamentales de **Integridad y Consistencia de Datos** en sistemas concurrentes.

### Impacto en la Escalabilidad (Fatal)
*   **Pérdida de Datos:** En un entorno escalado horizontalmente (múltiples instancias del servicio) o incluso verticalmente con alta concurrencia, la integridad de los datos se pierde inmediatamente.
*   **Imposibilidad de Escalar:** No se puede aumentar la carga sin garantizar la corrupción de la base de datos (archivo JSON).

---

## 3. Acoplamiento Fuerte a Implementaciones Concretas

### Hallazgo
Las clases de alto nivel como `OrderService` dependen directamente de clases concretas de bajo nivel como `OrderRepository`, `UserServiceProducer` y `UserServiceConsumer`, instanciándolas o inyectándolas directamente sin interfaces intermedias.

### Principio Vulnerado
*   **Dependency Inversion Principle (DIP):** Los módulos de alto nivel no deberían depender de detalles de implementación. Ambos deberían depender de abstracciones (interfaces).
*   **Open/Closed Principle (OCP):** El sistema no está "abierto a la extensión y cerrado a la modificación". Para cambiar el almacenamiento de archivos JSON a una base de datos real (necesaria para escalar), se tendría que modificar el código fuente de los servicios `OrderService` y `UserService`, rompiendo la funcionalidad existente.

### Impacto en la Escalabilidad (Alto)
*   **Rigidez Arquitectónica:** Migrar a una infraestructura escalable (como PostgreSQL, Redis, Kafka gestionado) requerirá una reescritura significativa y arriesgada de la lógica de negocio, en lugar de simplemente inyectar una nueva implementación de repositorio.
*   **Dificultad de Testing:** No se pueden hacer pruebas de carga aisladas fácilmente porque no se puede "mockear" el sistema de archivos o la red de RabbitMQ de manera sencilla sin interfaces.

---

## 4. Mezcla de Responsabilidades en Servicios

### Hallazgo
La clase `OrderService` contiene lógica de negocio (validación de estados), lógica de orquestación de infraestructura (llamadas a RabbitMQ, timeouts manuales) y lógica de transformación de datos (uso de mappers).

### Principio Vulnerado
*   **Single Responsibility Principle (SRP):** La clase tiene múltiples razones para cambiar: reglas de negocio, cambios en la estructura de mensajes de RabbitMQ, o cambios en el modelo de datos.

### Impacto en la Escalabilidad (Medio)
*   **Mantenibilidad:** A medida que el sistema crece en funcionalidades, esta clase se convertirá en una "Clase Dios" difícil de mantener y optimizar.
*   **Ciclos de Desarrollo Lentos:** La falta de separación hace que añadir nuevas características (como caché o validaciones complejas) sea más propenso a errores, frenando la velocidad del equipo de ingeniería.
