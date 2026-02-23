# Anatomía de un Incidente: Caso de Usuarios con Email Duplicado

## Contexto

Durante el desarrollo, se detectó que el sistema permitía crear múltiples usuarios con el mismo email, lo que generaba problemas de identificación y asignación de pedidos. Este incidente está documentado en la issue "Frontend y Backend: Usuarios con Email duplicado" (#17).

### Error (Acción humana incorrecta)

El equipo asumió que el frontend y backend validarían la unicidad del email, pero no se implementó ninguna verificación efectiva. Hubo una mala interpretación de los requisitos de negocio y falta de comunicación entre los responsables de cada capa.

### Defecto (Imperfección en el código)

El código de creación de usuario, tanto en el backend como en el frontend, no incluía una validación para evitar emails duplicados. Esto permitió que se almacenaran múltiples usuarios con el mismo email en el sistema.

### Fallo (Comportamiento erróneo en ejecución)

El sistema permitió la creación de usuarios con emails repetidos. Como consecuencia, al asignar pedidos o buscar usuarios por email, se producían inconsistencias, errores de asignación y confusión para los usuarios finales.

---

## Análisis de la Pirámide de Pruebas

En nuestro proyecto, la base de la pirámide de pruebas (pruebas unitarias) es fundamental porque la mayor parte de la lógica reside en funciones y servicios que pueden validarse de forma aislada. Las pruebas unitarias permiten detectar errores rápidamente, son rápidas de ejecutar y fáciles de mantener. Las pruebas de integración validan la interacción entre componentes (por ejemplo, servicios y repositorios), mientras que las pruebas E2E aseguran que los flujos críticos funcionan correctamente desde la perspectiva del usuario.

### Tesis Breve

La pirámide de pruebas prioriza la cantidad y profundidad de pruebas unitarias porque son más económicas, rápidas y permiten detectar defectos en etapas tempranas. Las pruebas E2E, aunque valiosas, son más costosas y lentas, por lo que deben reservarse para validar los flujos más críticos del sistema.

### Escenarios de Prueba de Alto Valor

- **Unitarias:**
  - _Escenario:_ Validar que la función de creación de usuario rechaza emails duplicados.
  - _Riesgo mitigado:_ Evita defectos lógicos que permitan inconsistencias en los datos desde el inicio.

- **Integración:**
  - _Escenario:_ Probar que el servicio de pedidos puede asociar correctamente un pedido a un usuario existente, integrando la lógica de ambos servicios y el acceso a datos.
  - _Riesgo mitigado:_ Detecta errores en la interacción entre módulos y en la persistencia de datos.

- **E2E:**
  - _Escenario:_ Simular el flujo completo de registro de usuario y creación de pedido desde la interfaz web, verificando que el usuario recibe confirmación y el pedido queda correctamente asignado.
  - _Riesgo mitigado:_ Garantiza que los procesos críticos funcionan para el usuario final y que no hay fallos en la integración global.

---

## Implementación de Aprendizaje: Suite Mínima de Pruebas

El proyecto cuenta con al menos una prueba de cada nivel de la pirámide, demostrando la comprensión de los distintos tipos de validación:

- **Pruebas Unitarias:**
  - Backend: `Backend/usuario-service/src/test/java/com/example/usuarioservice/repository/UserRepositoryTest.java` contiene tests unitarios para operaciones CRUD sobre usuarios.
  - Frontend: `Frontend/src/services/__tests__/usuarioService.test.ts` valida funciones de servicio de usuario.

- **Pruebas de Integración:**
  - Frontend: `Frontend/src/hooks/__tests__/useDashboardData.test.ts` prueba la integración de hooks con servicios de usuarios y pedidos.

- **Pruebas E2E:**
  - E2E: `tests/e2e/basic.test.js` y `tests/e2e/services.test.js` simulan flujos completos entre servicios, ejecutándose con los servicios corriendo en Docker Compose.

Esta suite mínima cubre los patrones implementados en la Fase 2 y permite validar tanto la lógica aislada como la interacción y los flujos críticos del sistema.
