# HU-USR-01 — TDD cycle report

Resumen breve

Documento que registra el ciclo TDD (RED → GREEN → REFACTOR) para la historia HU-USR-01: "Listado completo de usuarios registrados en PostgreSQL" en el servicio `usuario-service`.

Checklist
- [x] Detallar RED: tests creados (qué cubren y por qué).
- [x] Explicar GREEN: implementación mínima aplicada o confirmación de que ya existía.
- [x] Documentar REFACTOR: cambios, mejoras y cómo reproducir.

---

## 1) Contexto de la historia
- Story ID: HU-USR-01
- Título: Listado completo de usuarios registrados en PostgreSQL
- Endpoint objetivo: `GET /v1/usuarios`
- Requisitos clave:
  - Devolver los usuarios activos (omitidos soft-deletes).
  - Responder 200 OK con colección (posible vacía).

---

## 2) RED (tests)
Objetivo: crear pruebas que expresen los criterios de aceptación principales con un conjunto de tests mínimos.

Qué se hizo:
- Se añadieron tests unitarios/ de integración ligera que validan el comportamiento del endpoint y la capa de servicio:
  - Verificar que `UsuarioService.obtenerTodos()` delega a la persistencia y filtra por activos.
  - Verificar que `UsuarioController.obtenerTodos()` mapea correctamente los resultados a `UsuarioResponse` y devuelve 200 OK.
- Ubicación de tests (ejemplos):
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/service/UsuarioServiceObtenerTodosTest.java`
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/controller/UsuarioControllerObtenerTodosTest.java`

Casos cubiertos (mínimos):
1. Given existen usuarios activos When llamo GET /v1/usuarios Then recibo 200 con colección no vacía.
2. Given no hay usuarios activos When llamo GET /v1/usuarios Then recibo 200 con colección vacía.
3. Given usuarios inactivos Estos no deben aparecer en el listado activo.

Commit sugerido (RED):
- `test(HU-USR-01): add tests for listing active users`

Estado: tests RED añadidos y commiteados.

Resultados de la fase RED (pruebas):
- Los tests unitarios y de controller añadidos se ejecutaron localmente: BUILD SUCCESS para los tests focales (no se detectaron fallos).
- Esto indica que la capa de servicio y controller cumplen con los criterios básicos del endpoint.

---

## 3) GREEN (implementación mínima)
Objetivo: aplicar el cambio mínimo para pasar los tests.

Análisis y acción tomada:
- Revisión de `UsuarioService.obtenerTodos()` mostró que ya invoca `userRepository.findAllActive()` y retorna la colección. El controlador mapea a `UsuarioResponse` y devuelve 200.
- Por ello, no fue necesario añadir lógica adicional en producción para cumplir los tests.
- Se verificó que la persistencia `IUserPersistence` tiene una implementación que filtra `active=true` (JSON/JPA) según configuración.

Resultados de la fase GREEN (pruebas):
- Al ejecutar los tests de integración ligera y unitarios relevantes, todos pasaron: BUILD SUCCESS.
- Conclusión: la implementación existente satisface la historia; GREEN alcanzado sin cambios funcionales.

Commit sugerido (GREEN):
- `feat(HU-USR-01): green — tests pass for list-users`

Comandos para verificar localmente:
```powershell
mvn -f "Backend/usuario-service/pom.xml" -DskipTests=false -Dtest=UsuarioServiceObtenerTodosTest test
mvn -f "Backend/usuario-service/pom.xml" -DskipTests=false -Dtest=UsuarioControllerObtenerTodosTest test
```

---

## 4) REFACTOR (mejoras aplicadas y razones)
Acciones aplicadas:
- No se aplicaron cambios funcionales críticos para esta historia.

Por qué no se aplicó un refactor de código para cumplir la HU:
- La funcionalidad solicitada ya estaba soportada por implementaciones previas del servicio y la capa de persistencia (factory/decorator y mapeo de controller). Por tanto no fue necesario modificar la lógica de negocio.
- Se optó por proponer mejoras adicionales (paging, tests de integración) en lugar de cambiar código que ya era correcto.

Propuestas de mejora (opcionales):
- Añadir test de integración contra H2/Postgres para verificar la consulta `findByActiveTrue()` en JPA.
- Añadir paging y límites en el endpoint para soportar tablas grandes.
- Documentar en README la semántica de `active` y comportamiento de soft-delete.

Commit sugerido (REFACTOR):
- `refactor: document active filtering and add integration test suggestion`

---

## 5) Archivos relevantes
- `Backend/usuario-service/src/main/java/com/example/usuarioservice/service/UsuarioService.java`
- `Backend/usuario-service/src/main/java/com/example/usuarioservice/controller/UsuarioController.java`
- Tests (ejemplos):
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/service/UsuarioServiceObtenerTodosTest.java`
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/controller/UsuarioControllerObtenerTodosTest.java`

---

## 6) Cómo probar manualmente (Postman)
- URL: `GET http://localhost:8081/api/v1/usuarios`
- Esperado: 200 OK con array JSON de `UsuarioResponse` con usuarios activos.

---

## 7) Observaciones finales
- Foco en mantener contrato REST y la semántica de `active` como filtro de soft-delete.
- Si quieres que implemente paging o tests de integración adicionales, lo hago en el próximo paso.
