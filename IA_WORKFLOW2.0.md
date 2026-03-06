# Puerta de Calidad (Quality Gate)

La puerta de calidad se establece para garantizar que el código implementado cumpla con los estándares de calidad definidos para el proyecto. Esto incluye la revisión del código, la ejecución de pruebas unitarias y de integración, y la verificación de que todas las funcionalidades implementadas se ajusten a los requisitos del proyecto.

## Revisión y auditoria del código

La IA revisará el código implementado para asegurarse de que siga las mejores prácticas de programación, que sea legible y mantenible, y que no contenga errores evidentes. Cualquier problema identificado durante la revisión del código deberá ser registrado en el archivo AUDITORIA.md, junto con una descripción del problema, principios vulnerados, el impacto en la escalabilidad y mantenibilidad, y una sugerencia de cómo solucionarlo.

Se debe buscar cualquier Violación de principios SOLID, código duplicado, falta de comentarios o documentación, acoplamiento rígido, lógica duplicada, falta de abstracción y cualquier otro aspecto que pueda afectar la calidad del código.

También se deben solicitar a la IA que busque y registre los casos donde se hubiera hecho un buen uso de los principios SOLID, la implementación de patrones de diseño adecuados, la creación de código limpio y mantenible, y cualquier otro aspecto positivo que contribuya a la calidad del código.

## Refactorización incremental

Una vez registrados todos los problemas identificados durante la revisión del código, se procede a realizar una refactorización incremental para abordar cada uno de los problemas. Esto implica hacer cambios específicos en el código para mejorar su calidad, siguiendo las sugerencias proporcionadas en el archivo AUDITORIA.md. La refactorización debe realizarse de manera cuidadosa para evitar introducir nuevos errores y para asegurar que el código resultante sea más limpio, mantenible y escalable.

Antes de cada commit se deberá utilizar la IA para que verifique el código refactorizado cumple con los estándares de calidad y que la solución implementada es adecuada para resolver el problema identificado.

Cada corrección y refactorización debe ser acompañado de un commit que deberá tener la estructura de:
refactor: implementado patrín [Nombre] para resolver acoplamiento en [Componente]

## Testeo de aplicación

Después de la refactorización, se deben ejecutar nuevamente todas las pruebas unitarias, de integración y funcionales para asegurarse de que los cambios realizados no hayan introducido nuevos errores y que el sistema siga funcionando correctamente. Si alguna prueba falla, se debe investigar la causa del fallo, corregir el problema y volver a ejecutar las pruebas hasta que todas pasen exitosamente.

## Evaluación de deuda técnica

Una vez que se han abordado los problemas identificados durante la revisión del código y se han realizado las refactorizaciones necesarias, se debe evaluar la deuda técnica restante en el proyecto. Esto implica revisar el código para identificar cualquier área que aún pueda ser mejorada, aunque no sea crítica para el funcionamiento del sistema. La evaluación de la deuda técnica debe ser documentada en el archivo AUDITORIA.md, indicando las áreas que podrían beneficiarse de futuras mejoras, junto con una descripción de los beneficios que se obtendrían al abordar esa deuda técnica en el futuro.

La evaluación se deberá realizar clasificando las deudas técnicas utilizando el cuadrante de Martin Fowler, que categoriza la deuda técnica en cuatro cuadrantes:

- Cuadrante 1: (Pruedente/Deliberada)
- Cuadrante 2: (Prudente/Inadvertida)
- Cuadrante 3: (Imprudente/Deliberada)
- Cuadrante 4: (Imprudente/Inadvertida)
