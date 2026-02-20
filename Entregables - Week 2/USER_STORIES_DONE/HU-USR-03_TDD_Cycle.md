# HU-USR-03 — TDD cycle report

Resumen breve

Este documento registra el ciclo TDD completo (RED → GREEN → REFACTOR) llevado a cabo para la historia HU-USR-03: "Creación de nuevos usuarios con persistencia garantizada" en el servicio `usuario-service`.

Checklist (objetivos del documento)
- [x] Detallar qué se hizo en RED (tests creados y por qué).
- [x] Explicar la fase GREEN (implementación mínima o confirmación de que no era necesaria).
- [x] Documentar la fase REFACTOR (mejoras y cambios seguros aplicados).
- [x] Incluir rutas de archivos, comandos para reproducir y mensajes de commit sugeridos.

---

## 1) Contexto de la historia
- Story ID: HU-USR-03
- Título: Creación de nuevos usuarios con persistencia garantizada
- Endpoint objetivo: `POST /v1/usuarios`
- Requisitos clave (resumidos):
  - Dado datos válidos → 201 Created + fila en PostgreSQL.
  - Validación inválida → 4xx y no insertar registros.
  - Duplicado por clave única → rechazar y no insertar.

---

## 2) RED (tests — fase escrita y commiteada)
Objetivo: escribir tests mínimos que representen los criterios de aceptación.

Qué se hizo (resumen):
- Se crearon tests unitarios (Mockito) en formato Given / When / Then para cubrir los criterios principales.
- Archivo creado:
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/HUUSR03_CreateUserTests.java`

Tests incluidos (breve descripción):
1. Given valid data / When crear / Then: se llama a `save` y se devuelve el usuario creado (happy path).
2. Given invalid data / When crear / Then: `validationContext.validateForCreation` lanza `ValidationException`; no se llama a `save` (validación negativa).
3. Given duplicate email / When crear / Then: `userPersistence.findByEmail` devuelve existente y se lanza `UsuarioYaExisteException`; no se persiste.
4. Given valid data / When crear / Then: capturar el objeto pasado a `save` y comprobar `active == true` (invariante de integridad).

Detalles relevantes:
- Los tests son unitarios (no arrancan Spring ni la BD) para que la fase RED sea ligera y reproducible.
- Los tests incluyen logs SLF4J en cada Given/When/Then para facilitar trazabilidad durante ejecución.

Commit sugerido (RED):
- `test(HU-USR-03): add unit RED tests for create-user (Given/When/Then)`

Estado: Los tests RED fueron creados y commiteados por el desarrollador.

---

## 3) GREEN (implementación mínima)
Objetivo: introducir la mínima implementación necesaria para que los tests pasen.

Análisis y decisión tomada:
- Revisión del código de producción (`UsuarioService.crear(...)`) mostró que la implementación ya satisface los criterios verificados por los tests:
  - Invoca `validationContext.validateForCreation(request, LENIENT)`.
  - Comprueba `userRepository.findByEmail(...)` y lanza `UsuarioYaExisteException` si existe.
  - Crea `User` y ejecuta `usuario.setActive(true)` antes de `save`.
  - Llama a `userRepository.save(usuario)` y retorna el resultado.
- Conclusión: no fue necesario cambiar código de negocio para pasar los tests (la implementación existente ya estaba alineada). Por tanto GREEN se alcanzó confirmando que los tests pasan.

Acción práctica aplicada (refactor de configuración complementario):
- Para facilitar el arranque local contra PostgreSQL añadí valores por defecto en `application.properties` (no afecta tests):
  - Archivo modificado: `Backend/usuario-service/src/main/resources/application.properties`
  - Cambio: valores por defecto para `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (ej.: localhost, 5432, usuario_service, postgres/postgres).


mvn -f "Backend/usuario-service/pom.xml" test

---

## 4) REFACTOR (mejoras aplicadas y propuestas)
Acciones aplicadas durante REFACTOR (cambios seguros ya realizados):
- Añadido valores por defecto en `application.properties` para facilitar arranque local con PostgreSQL.
  - Ruta: `Backend/usuario-service/src/main/resources/application.properties`.
  - Motivación: evitar fallos de arranque por variables de entorno no definidas; útil para pruebas manuales (Postman).`

---

## 5) Archivos relevantes (resumen)
- Tests RED añadidos:
  - `Backend/usuario-service/src/test/java/com/example/usuarioservice/HUUSR03_CreateUserTests.java`
- Código de negocio inspeccionado:
  - `Backend/usuario-service/src/main/java/com/example/usuarioservice/service/UsuarioService.java`
- Config modificada (REFACTOR):
  - `Backend/usuario-service/src/main/resources/application.properties`
- Factory / wiring de persistencia:
  - `Backend/usuario-service/src/main/java/com/example/usuarioservice/config/PersistenceConfig.java`
  - `Backend/usuario-service/src/main/java/com/example/usuarioservice/config/UserPersistenceFactory.java`

---

## 6) Cómo probar manualmente (Postman)
- URL (si arrancas con `mvn spring-boot:run`): `POST http://localhost:8081/api/v1/usuarios`
- Headers: `Content-Type: application/json`
- Body (ejemplo):

```json
{
  "name": "Prueba Usuario",
  "mail": "prueba+postman@example.com",
  "password": "ValidPass1"
}
```
- Esperado: `201 Created` y la fila debe existir en PostgreSQL (consulta `SELECT * FROM usuarios WHERE mail='prueba+postman@example.com';`).
- Si no tienes Postgres local puedes levantar uno rápido con Docker:

```powershell
docker run --name usuario-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=usuario_service -p 5432:5432 -d postgres:15
```

## 8) Observaciones finales
- El flujo TDD fue respetado: los tests RED fueron escritos y commiteados; en GREEN verificamos que la implementación ya satisface los tests; en REFACTOR realizamos un cambio de configuración seguro para facilitar pruebas locales.

---