# 1️⃣ EPIC

## Epic Title  
Gestión completa de usuarios con persistencia en PostgreSQL

## Epic Purpose  
Capacitar al backend para gestionar usuarios (listado, consulta, creación, actualización y eliminación con soft-delete) con garantías de consistencia y trazabilidad directa en PostgreSQL, de modo que cada interacción refleje fielmente el estado de la base de datos.

## Business Objective  
Reducir la brecha entre la API REST de usuarios y la fuente de verdad en PostgreSQL para que los Product Owners puedan confiar en la integridad oportuna de los datos del catálogo de usuarios.

## Success Metrics  
- 100% de los endpoints de usuario reflejan transacciones exitosas en PostgreSQL según auditorías de la base de datos.
- Las verificaciones de inconsistencias detectan cero discrepancias entre las respuestas API y los registros en PostgreSQL.

---

# 2️⃣ USER STORIES

## Story ID: HU-USR-01  
## Story Title  
Listado completo de usuarios registrados en PostgreSQL

### Role  
Product Owner

### Objective  
Verificar que puedo consultar todos los usuarios almacenados (activos o marcados).

### Benefit  
Asegurarme de que el catálogo visible en la aplicación refleje la lista real registrada en la base de datos sin saltos ni datos desactualizados.

### Detailed Description  
Como Product Owner quiero obtener la lista completa de usuarios para confirmar que la información mostrada coincide con lo persistido en PostgreSQL. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. Recibo un 200 OK con el conjunto completo de usuarios que existen en la tabla de PostgreSQL sin omitir registros válidos.  
2. Cada respuesta incluye al menos los campos clave definidos por el arquitecto, lo que permite cruzar la salida con los datos persistidos.  
3. Si no hay usuarios activos, la respuesta es 200 OK con una colección vacía y se verifica que la tabla en PostgreSQL no contiene filas activas.  
4. No se acepta un payload parcial ni inconsistencias en el número de usuarios reportados comparado con la base de datos.

## Story ID: HU-USR-02  
## Story Title  
Consulta de usuario específico por identificador

### Role  
Product Owner

### Objective  
Asegurar la recuperación fiel de un usuario concreto.

### Benefit  
Puedo validar casos de soporte o auditoría sabiendo que los datos obtenidos reflejan el estado real en PostgreSQL.

### Detailed Description  
Como Product Owner quiero obtener un usuario por ID o correo para confirmar que los datos retornados coinciden con la fila correspondiente en PostgreSQL. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. Cuando el identificador existe, recibo 200 OK con los datos completos del usuario almacenado en PostgreSQL.  
2. Si el identificador no existe, obtengo un 404 y la respuesta indica que no se encontraron registros en la base de datos, evitando supuestos silenciosos.  
3. El payload incluye los campos definidos por el arquitecto e iguala el estado actual del registro en PostgreSQL.

## Story ID: HU-USR-03  
## Story Title  
Creación de nuevos usuarios con persistencia garantizada

### Role  
Product Owner

### Objective  
Validar que la creación de usuarios se refleja inmediatamente en PostgreSQL.

### Benefit  
Confío en que cada nuevo usuario aprobado se materializa en la base de datos sin necesidad de verificaciones manuales posteriores.

### Detailed Description  
Como Product Owner quiero crear usuarios y asegurar que cada nueva entidad se persiste correctamente en PostgreSQL. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. Cuando envío datos válidos, recibo 201 Created con el usuario recién creado y la tabla de PostgreSQL contiene esa fila con los mismos valores.  
2. Si los datos no cumplen con las reglas impuestas por el arquitecto, la API responde con un error 4xx y no se inserta ningún registro en PostgreSQL.  
3. No se generan duplicados: la respuesta debe reflejar una única fila persistida y se valida que la base de datos rechaza entradas repetidas según las reglas definidas.  
4. El cuerpo de respuesta incluye los campos clave definidos por el arquitecto para hacer conciliación con la tabla.

## Story ID: HU-USR-04  
## Story Title  
Actualización completa de un usuario existente

### Role  
Product Owner

### Objective  
Verificar que reemplazar un usuario actualiza su registro en PostgreSQL.

### Benefit  
Puedo aprobar cambios masivos sabiendo que la base de datos refleja el estado actualizado del usuario sin residuos del estado anterior.

### Detailed Description  
Como Product Owner quiero realizar una actualización completa de un usuario específico para que todos los valores persistidos en PostgreSQL se reemplacen correctamente. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. Cuando el usuario existe y envío datos válidos, la API responde 200 OK y la fila en PostgreSQL contiene exactamente los nuevos valores.  
2. Si el usuario no existe, la API responde 404 y PostgreSQL permanece sin cambios.  
3. El payload actualizado incluye todos los campos definidos por el arquitecto, y se verifica que coinciden con la fila persistida.  
4. No se permite que queden campos obsoletos: el registro en PostgreSQL no debe contener datos anteriores no sobreescritos.

## Story ID: HU-USR-05  
## Story Title  
Actualización parcial de datos de usuario

### Role  
Product Owner

### Objective  
Poder modificar atributos específicos sin afectar el resto del registro.

### Benefit  
Mantengo precisión en los datos críticos mientras actualizo solo lo necesario, con la certeza de que PostgreSQL persiste los cambios parciales correctamente.

### Detailed Description  
Como Product Owner quiero aplicar cambios parciales a un usuario para garantizar que solo los campos modificados se actualicen en PostgreSQL. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. La API responde 200 OK cuando el usuario existe y los campos enviados son válidos, y PostgreSQL refleja únicamente esos valores nuevos sin alterar otros.  
2. Si el usuario no existe, la respuesta es 404 y no se produce ningún cambio en PostgreSQL.  
3. Si el payload incluye campos vacíos o inválidos según lo definido por el arquitecto, se recibe un error 4xx y la fila en PostgreSQL permanece intacta.  
4. La respuesta contiene los campos actualizados confirmando su persistencia real.

## Story ID: HU-USR-06  
## Story Title  
Soft-delete seguro de un usuario

### Role  
Product Owner

### Objective  
Confirmar que la eliminación de usuarios marca su registro como borrado en PostgreSQL.

### Benefit  
Evito inconsistencias entre la interfaz y la base de datos mientras mantengo trazabilidad legal de los usuarios eliminados.

### Detailed Description  
Como Product Owner quiero eliminar un usuario y verificar que su fila queda marcada como soft-delete en PostgreSQL, de modo que desaparezca de los listados activos sin perder la referencia histórica. Los requisitos de datos concretos (campos obligatorios o reglas de unicidad) los definirá el arquitecto.

### Criterios de aceptación  
1. Cuando el usuario existe, la API responde 204 No Content y la fila en PostgreSQL se marca como borrada (soft-delete) sin eliminarse físicamente.  
2. Si el usuario no existe, la respuesta es 404 y PostgreSQL permanece sin cambios.  
3. Los listados de usuarios activos no muestran filas marcadas como eliminadas; solo un endpoint administrativo (si existe) puede exponerlas.  
4. La segunda llamada para eliminar el mismo usuario devuelve 404 y confirma que el registro ya estaba marcado como borrado.

---

# 3️⃣ PROCESS FLOW

1. El PO solicita un endpoint de usuario (GET/POST/PUT/PATCH/DELETE) según la acción requerida.  
2. El backend valida el payload (según definiciones del arquitecto) y lo compara con PostgreSQL.  
3. Si la petición es de lectura, se verifica que PostgreSQL devuelva registros activos consistentes (ignorando soft-deletes).  
4. Para escrituras o actualizaciones, la transacción persiste los cambios y se comprueba la coherencia con el estado esperado.  
5. Para borrados, el registro se marca como soft-delete en PostgreSQL y se verifica que ya no aparece en vistas activas.  
6. Ante errores (datos inválidos, duplicados, recursos no encontrados), se devuelve el status correspondiente sin alterar PostgreSQL.  
7. Toda operación se registra para auditoría y trazabilidad.

---

# 4️⃣ FUNCTIONAL REQUIREMENTS

- FR-01: El endpoint GET /v1/usuarios debe devolver el conjunto completo de usuarios activos actualmente almacenados en PostgreSQL, omitiendo soft-deletes.  
- FR-02: El endpoint GET /v1/usuarios/{identificador} debe recuperar exactamente un registro activo existente o emitir 404 si no existe.  
- FR-03: El endpoint POST /v1/usuarios debe persistir un nuevo usuario en PostgreSQL cuando los datos cumplan las reglas del arquitecto y reportar errores en caso contrario.  
- FR-04: El endpoint PUT /v1/usuarios/{id} debe reemplazar completamente el registro en PostgreSQL si existe, o responder 404 sin impacto si no.  
- FR-05: El endpoint PATCH /v1/usuarios/{id} debe aplicar únicamente los campos enviados y dejar intactos los demás, marcando error si el usuario no existe.  
- FR-06: El endpoint DELETE /v1/usuarios/{id} debe marcar un registro existente como soft-delete en PostgreSQL o devolver 404 sin cambios si ya estaba borrado.

---

# 5️⃣ NON-FUNCTIONAL REQUIREMENTS

- Rendimiento: Las respuestas a lecturas y escrituras no deben exceder 500 ms en condiciones normales de carga.  
- Escalabilidad: La solución debe permitir crecer la tabla de usuarios sin degradar la consistencia de las respuestas y el control de soft-delete.  
- Seguridad: Solo usuarios autorizados deben acceder a los endpoints de usuario.  
- Integridad de datos: Todas las transacciones que tocan PostgreSQL deben completar con commit o revert completo; no se deben dejar estados intermedios.  
- Observabilidad: Cada endpoint debe generar logs que indiquen qué operación se intentó y si PostgreSQL confirmó la persistencia o el soft-delete.  
- Mantenibilidad: Las descripciones de campos y reglas de soft-delete se documentan en la especificación del arquitecto.

---

# 6️⃣ ACCEPTANCE CRITERIA

### Positive Scenarios (Acceptance)  
- GIVEN que existen usuarios persistidos, WHEN se llama a GET /v1/usuarios, THEN la respuesta 200 refleja exactamente las filas activas en PostgreSQL (sin soft-deletes).  
- GIVEN que envío una creación válida, WHEN la API responde 201, THEN puedo ver la nueva fila activa en PostgreSQL con los campos definidos por el arquitecto.  
- GIVEN que actualizo parcialmente un usuario existente, WHEN se devuelve 200, THEN la base de datos muestra únicamente los campos modificados.  
- GIVEN que elimino un usuario existente, WHEN la respuesta es 204, THEN la fila está marcada como soft-delete y no aparece en listados activos.

### Negative Scenarios (Non-Acceptance)  
- Validación fallida: datos incompletos o inválidos deben regresar 4xx y PostgreSQL no debe persistir nada.  
- Duplicados: un intento de crear un usuario con claves únicas repetidas debe ser rechazado sin nuevas filas.  
- Acceso no autorizado: llamadas sin credenciales válidas reciben un 401/403 y no impactan PostgreSQL.  
- Timeout: si PostgreSQL no responde en la ventana esperada, la API reporta un 503 y no aplica cambios.  
- Formato inválido: payloads mal formados deben retornar 400 y dejar la base de datos sin alterar.  
- Violación de seguridad: cualquier manipulación detectada debe bloquearse sin persistencia.  
- Estado doblemente borrado: intentar eliminar nuevamente un usuario que ya fue marcado como soft-delete debe regresar 404 sin cambios nuevos.

---

# 7️⃣ ASSUMPTIONS

- La autenticación y autorización están en su lugar (por ejemplo, Spring Security), aunque no se describen aquí.  
- El arquitecto definirá campos obligatorios, límites de longitud y reglas de unicidad para cada entidad.  
- PostgreSQL es la fuente de verdad; no hay sincronización con otras bases de datos.  
- Detectores de duplicados y validación siguen políticas definidas por el arquitecto.  
- El modelo de datos tolera soft-delete, manteniendo la fila pero agregando una bandera o similar.

---

# 8️⃣ CONSTRAINTS

- Todas las operaciones de usuario deben pasar por PostgreSQL; no se permite almacenamiento temporal en memoria.  
- La interfaz REST ya está determinada por los endpoints descritos en el controlador actual.  
- No se debe asumir infraestructura adicional más allá del stack Java/Spring y PostgreSQL.  
- Las soft-deletes deben ser reversibles o auditablemente rastreables según las políticas definidas.

---

# 9️⃣ DEPENDENCIES

- PostgreSQL como motor persistente.  
- Spring Boot/Spring Data para transacciones y repositorios.  
- Servicio de autenticación/autoría (token JWT u otro).  
- Arquitecto define esquemas detallados y restricciones de datos.  
- Documentación de integraciones de RabbitMQ, aunque no se toca directamente en estos endpoints.

---

# 🔟 OPEN QUESTIONS (IF ANY REMAIN)

- ¿Hay reglas específicas de validación que debamos levantar con el arquitecto antes de priorizar estas historias?  
- ¿Se requiere exponer un endpoint administrativo para consultar registros soft-deleted o bastan auditorías internas?
