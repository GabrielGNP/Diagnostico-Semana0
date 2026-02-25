# API Audit Report

## 📌 Resumen Ejecutivo

- Nivel de adherencia a REST: Alto. Los endpoints usan verbos HTTP apropiados y rutas sustantivas en plural (`/orders`).
- Problemas estructurales: pequeñas inconsistencias en comentarios vs comportamiento real, falta de validación declarativa (`@Valid`) en los cuerpos de petición y uso genérico de `ResponseEntity<?>` donde podrían usarse tipos concretos.
- Riesgos técnicos: validación de entrada delegada a la capa de servicio en lugar de usar validación de DTOs puede provocar respuestas 500 inesperadas o mensajes de error menos estructurados.

---

## 📂 Endpoints Analizados

### POST /orders

**Problema Detectado:**
- No hay validación declarativa en el `@RequestBody` (`OrderDto` no tiene anotaciones de validación y el controlador no usa `@Valid`).

**Impacto:**
- Consumidores pueden enviar peticiones incompletas y provocar errores no estandarizados desde la capa de servicio.

**Recomendación:**
- Añadir validaciones (`@NotNull`, `@NotBlank`, etc.) en `OrderDto` y usar `public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDto orderDto)` para aprovechar `MethodArgumentNotValidException`.
- Asegurar siempre el header `Location` al crear el recurso o documentar comportamiento asíncrono si aplica.

**Severidad:** 🟠 Medio

---

### DELETE /orders/{id}

**Problema Detectado:**
- La documentación en comentarios menciona retornar `200 OK`, pero la implementación usa `ResponseEntity.noContent()` (HTTP 204).

**Impacto:**
- Inconsistencia entre documentación y comportamiento; los consumidores que esperan body en 200 podrían fallar.

**Recomendación:**
- Alinear la documentación con la implementación (preferible `204 No Content` para borrado soft) o cambiar la implementación si se requiere `200` con cuerpo.

**Severidad:** 🟢 Mejora

---

### GET /orders/{id}

**Problema Detectado:**
- Uso de `expand=user` vía `@RequestParam` está bien; sin embargo las rutas retornan siempre `200 OK` para respuestas válidas y dependen de que el `OrderService` lance `OrderNotFoundException` para producir `404`.

**Impacto:**
- Correcto si el servicio lanza excepciones bien definidas (hay `OrderNotFoundException` y un `GlobalExceptionHandler` que lo mapea a 404).

**Recomendación:**
- Mantener el manejo actual; documentar claramente el parámetro `expand` y el shape de `OrderWithUserDto`.

**Severidad:** 🟢 Mejora

---

### GET /orders

**Problema Detectado:**
- Ninguno significativo. Soporta `userId` como filtro lógico.

**Recomendación:**
- Considerar paginación si la colección puede crecer (usar `page`/`size`).

**Severidad:** 🟢 Mejora

---

### PATCH /orders/{id}

**Problema Detectado:**
- El controlador espera un `OrderDto` completo en el cuerpo solo para leer `state`. Esto expone la API a enviar más campos de los necesarios.

**Impacto:**
- Menos explícito; riesgo de confusión y actualización parcial accidental de campos si la implementación cambia.

**Recomendación:**
- Usar un DTO específico para la operación de cambio de estado (por ejemplo `OrderStateUpdateDto { State state; }`) y declarar `@RequestBody @Valid` según corresponda.

**Severidad:** 🟢 Mejora

---

## 🧭 Revisión de Controllers y Manejo de Errores

- Los controladores delegan correctamente a `OrderService` (buena separación de responsabilidades).
- Existe un `GlobalExceptionHandler` con mapeos para `OrderNotFoundException` (404), `IllegalArgumentException` (400), `MethodArgumentNotValidException` (400) y `Exception` (500). Esto es una buena práctica.
- `ErrorResponse` no expone stacktraces y aporta `timestamp`, `status`, `message` y `path` (adecuado).
- Recomendación adicional: usar tipos concretos en las firmas `ResponseEntity<OrderDto>` o `ResponseEntity<Void>` donde aplique, en lugar de `ResponseEntity<?>`, para mejorar autogeneración de documentación (Swagger/OpenAPI) y tipado.

---

## ✅ Resumen de Acciones Recomendadas (priorizadas)

- (Medio) Añadir validación en `OrderDto` y usar `@Valid` en los `@RequestBody` para endpoints de creación/actualización.
- (Medio) Usar DTOs específicos para operaciones parciales (`OrderStateUpdateDto`) en `PATCH`.
- (Mejora) Alinear comentarios/documentación con la implementación (`DELETE` retorna 204 o ajustar a 200).
- (Mejora) Tipar `ResponseEntity` con el tipo específico para cada método.
- (Mejora) Considerar paginación en `GET /orders` si la cantidad de datos puede crecer.

---

Si quieres, aplico los cambios sugeridos (añadir validaciones y DTO para `PATCH`) y preparo los tests/integración necesarios.

---

_Reporte generado automáticamente por el agente de auditoría del proyecto._
