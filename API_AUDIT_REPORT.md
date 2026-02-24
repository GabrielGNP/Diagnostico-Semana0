# API Audit Report

## 📌 Resumen Ejecutivo

- **Nivel de adherencia a REST:** Parcial. `usuario-service` muestra buenas prácticas (uso de DTOs, controlador delgado, `GlobalExceptionHandler`). `pedido-service` funciona pero presenta inconsistencias en códigos HTTP, nomenclatura de paths y manejo de errores.
- **Problemas estructurales encontrados:** Falta de manejo global de excepciones en `pedido-service`, endpoints no RESTful (`/add`, `/all`), y creación de recursos sin `201 Created` ni `Location` header.
- **Riesgos técnicos:** Consumidores pueden interpretar incorrectamente resultados (200 vs 201/404/204), y errores sin formato consistente dificultan el manejo de fallos en clientes.

---

## 📂 Endpoints Analizados

### Service: pedido-service (controller base: `/order`)

- **POST /order/add**
  - Problema Detectado: Retorna `200 OK` en creación en vez de `201 Created` y no incluye `Location` header.
  - Impacto: Consumidores no pueden diferenciar entre creación y respuesta normal; rompe expectativas REST.
  - Recomendación: Retornar `201 Created` con `Location: /order/{id}`; usar path plural `/orders` y evitar sufijo `/add`.
  - Severidad: 🟠 Medio

- **DELETE /order/{id}**
  - Problema Detectado: Retorna `200 OK` vacío; usa `IllegalArgumentException` para not-found.
  - Impacto: Inconsistencia en códigos; `204 No Content` es más apropiado para eliminaciones exitosas.
  - Recomendación: Retornar `204 No Content` al eliminar; mapear excepciones específicas (ej. `OrderNotFoundException`) a `404` desde un manejador global.
  - Severidad: 🟢 Mejora

- **GET /order/{id}**
  - Problema Detectado: Correcto retorno `404` cuando no existe. No se detectan problemas graves.
  - Recomendación: Mantener DTOs y controlar exposición de entidades.
  - Severidad: 🟢 Mejora

- **GET /order/{id}/with-user-info**
  - Problema Detectado: Captura `Exception`, escribe en `System.err` y retorna `500` sin body estructurado.
  - Impacto: Logging inconsistente y falta de formato uniforme para errores; dificulta trazabilidad y parsing por clientes.
  - Recomendación: No usar `System.err`; usar logger y delegar al `@ControllerAdvice` (añadir uno si no existe en `pedido-service`). Retornar respuesta con body de error consistente.
  - Severidad: 🟠 Medio

- **GET /order/user/{idUser}** y **GET /order/all**
  - Problema Detectado: Convenciones de paths: `/all` y `/user/{idUser}` son funcionales pero no REST idiomáticas.
  - Recomendación: Usar `/orders` (GET) y `/orders?userId={id}` o `/users/{id}/orders` para coherencia.
  - Severidad: 🟢 Mejora

- **PATCH /order/{id}**
  - Problema Detectado: Uso apropiado de `PATCH` para cambio de estado; valida `state` y retorna `400` si falta.
  - Recomendación: Considerar `PUT` vs `PATCH` según si se reemplaza completo o parcial; documentar contrato del body.
  - Severidad: 🟢 Mejora

---

### Service: usuario-service (controller base: mapped `/v1/usuarios`) 

- **Observación global**
  - La capa de Controller es delgada, utiliza DTOs, validación JSR-380 y existe `GlobalExceptionHandler` que mapea correctamente `404`, `409`, `400`, `500` con cuerpos `ErrorResponse`. Buen cumplimiento de responsabilidades.
  - Severidad: 🟢 Mejora

- **Inconsistencia de ruta (logs vs mapping)**
  - Problema Detectado: El `@RequestMapping` está en `/v1/usuarios` pero el `API_PATH` constante usado en logs es `/api/v1/usuarios` (incluye `/api`). Esto puede inducir confusión en documentación y logs.
  - Impacto: Logs y documentación pueden mostrar rutas diferentes a las reales; aumenta la fricción para integradores.
  - Recomendación: Unificar el `RequestMapping` y la constante `API_PATH` (usar la misma ruta en ambos lugares, preferible `/api/v1/usuarios` o `/v1/usuarios` consistente en todo el proyecto).
  - Severidad: 🟠 Medio

- **POST /v1/usuarios**
  - Problema Detectado: Retorna `201 Created` correctamente, pero no establece `Location` header hacia el nuevo recurso.
  - Recomendación: Añadir `Location` con URI del recurso creado (`/api/v1/usuarios/{id}`) para cumplir con buenas prácticas REST.
  - Severidad: 🟢 Mejora

- **PUT/PATCH/DELETE**
  - Problema Detectado: Buen uso de verbos: `PUT` para reemplazo, `PATCH` para parcial, `DELETE` retorna `204 No Content`. Errores por recurso no encontrado se manejan con excepciones custom y son convertidos a `404` por `GlobalExceptionHandler`.
  - Severidad: 🟢 Mejora

---

## 🔎 Hallazgos transversales y recomendaciones

- **Manejo global de errores**: `usuario-service` tiene `GlobalExceptionHandler` y formatos de error consistentes. `pedido-service` carece de un `@ControllerAdvice` equivalente; se recomienda añadirlo para homogeneizar respuestas de error y evitar `System.err`.
- **Códigos de estado en creación**: Normalizar `POST` para devolver `201 Created` y `Location` header (aplica a `pedido-service`).
- **Nomenclatura REST**: Preferir rutas plurales (`/orders`, `/users`) y evitar sufijos con verbos o acciones (`/add`, `/all`). Considerar usar query params o sub-resources (`/users/{id}/orders`) para listas por usuario.
- **Logging y trazabilidad**: Evitar println/System.err; usar `logger` y correlación de request IDs si es posible.
- **Consistencia en constantes y logs**: Unificar constantes de ruta (`API_PATH`) con los `@RequestMapping` reales para evitar discordancias.
- **Errores y exposición**: Evitar retorno de stacktraces al cliente; retornar mensajes genéricos y registrar detalles en el servidor.

---

## Prioridad de corrección (sugerida)

- Alta (Arreglos recomendados pronto):
  - `pedido-service`: implementar `ControllerAdvice` y remover `System.err` (Severidad 🟠)
  - `pedido-service`: cambiar `POST /order/add` para retornar `201 Created` + `Location` (Severidad 🟠)

- Media / Baja:
  - Unificar rutas y constantes en `usuario-service` (Severidad 🟠)
  - Normalizar nombres de paths a plural y eliminar `/all` y `/add` (Severidad 🟢)

---

¿Quieres que aplique cambios concretos (p. ej. añadir `ControllerAdvice` a `pedido-service` y ajustar `POST /order/add` para devolver `201` + `Location`)? Puedo proponer parches.
