# 📊 Guía: Visualización de Resultados de Tests - HU-ORD-05

## ❓ Pregunta: ¿Por qué solo veo "tests pasados" sin detalles?

**Respuesta corta**: Es el comportamiento **normal y esperado** de JUnit 5.

## 🎯 Comportamiento de JUnit por Diseño

### Tests que PASAN (✅)
```
✅ testCrearPedidoConDatosValidos
✅ testValoresPorDefectoAsignadosAutomaticamente
✅ testPerformanceCreacionPedido
```
- **Output**: Nombre del test + ícono verde
- **Detalles**: Mínimos (solo indica "passed")
- **Razón**: No necesitas información cuando todo funciona correctamente

### Tests que FALLAN (❌)
```
❌ testCrearPedidoConDatosValidos
    Expected: 100
    Actual: 99
    at OrderServiceHuOrd05Test.java:115
    
    Stack trace:
    ...
```
- **Output**: Nombre del test + ícono rojo + stack trace completo
- **Detalles**: Valores esperados vs actuales, línea del error, trace completo
- **Razón**: Necesitas toda la información para debuggear

## 📝 Cómo Ver Más Información de Tests Exitosos

### ✅ Opción 1: Logs con `System.out.println()` (YA IMPLEMENTADO)

He agregado logs en algunos tests clave. Ahora verás output como:

```
🧪 [TEST] Iniciando test: Creación exitosa con datos válidos
   ✓ Input DTO creado: name=Pedido Test, idUser=1
   ✓ Mocks configurados correctamente
   ⚙️ Invocando orderService.createOrder()...
   ✓ Pedido creado exitosamente con ID: 100
   ✅ Todas las aserciones pasaron correctamente
   📊 Resultado: ID=100, State=PROCESSING, Active=true
   ✅ Verificación de mock: orderJpaRepository.save() fue invocado 1 vez
✅ [TEST PASADO] Creación exitosa con datos válidos
```

**Cómo verlo en IntelliJ IDEA**:
1. Ejecuta los tests
2. Ve a la pestaña "Run" (parte inferior)
3. Haz clic en el test específico
4. Verás el output en la consola

### ✅ Opción 2: Ejecutar Tests con Maven en Verbose

```powershell
# Ejecutar con output detallado
mvn test -Dtest=OrderServiceHuOrd05Test -X

# O con menos verbosidad pero más info
mvn test -Dtest=OrderServiceHuOrd05Test --debug
```

### ✅ Opción 3: Usar @DisplayName (YA IMPLEMENTADO)

Cada test tiene un `@DisplayName` descriptivo que explica qué hace:

```java
@DisplayName("Dado datos válidos (name, description, idUser), Cuando invoco createOrder, Entonces recibo pedido con ID asignado")
```

Esto ya aparece en el runner de tests del IDE.

### ✅ Opción 4: Configurar Logging de JUnit

Crea un archivo `junit-platform.properties` en `src/test/resources/`:

```properties
# Mostrar resumen detallado
junit.jupiter.execution.parallel.enabled = false
junit.jupiter.execution.parallel.mode.default = same_thread

# Logging más verboso
java.util.logging.config.file = logging.properties
```

### ✅ Opción 5: Usar Maven Surefire Report

Genera un reporte HTML con todos los detalles:

```powershell
mvn test -Dtest=OrderServiceHuOrd05Test
mvn surefire-report:report
```

El reporte estará en: `target/site/surefire-report.html`

## 🔍 Qué Información Puedes Ver Ahora

Con los logs agregados, cuando ejecutes los tests verás:

### 1. **Flujo de Ejecución**
- Inicio del test
- Creación de datos de prueba
- Invocación del método bajo prueba
- Resultado obtenido
- Fin del test

### 2. **Datos de Entrada y Salida**
```
Input DTO creado: name=Pedido Test, idUser=1
Pedido creado exitosamente con ID: 100
Resultado: ID=100, State=PROCESSING, Active=true
```

### 3. **Verificaciones de Mocks**
```
Verificación de mock: orderJpaRepository.save() fue invocado 1 vez
```

### 4. **Excepciones Esperadas**
```
⚠️ DTO creado SIN campo 'name' (esperamos que falle)
✅ Excepción lanzada correctamente: El campo 'name' es requerido
✅ Mensaje de error validado correctamente
```

## 🎨 Interpretación de Íconos en IntelliJ

| Ícono | Significado | Acción |
|-------|-------------|--------|
| ✅ Verde | Test pasó | No requiere acción |
| ❌ Rojo | Test falló | Revisar stack trace |
| ⚠️ Amarillo | Test ignorado (`@Disabled`) | Revisar por qué está deshabilitado |
| ⏸️ Gris | Test no ejecutado | Ejecutar tests |

## 📊 Ejemplo de Output Esperado

Cuando ejecutes `OrderServiceHuOrd05Test` completo, verás algo como:

```
Running OrderServiceHuOrd05Test

🧪 [TEST] Iniciando test: Creación exitosa con datos válidos
   ✓ Input DTO creado: name=Pedido Test, idUser=1
   ✓ Mocks configurados correctamente
   ⚙️ Invocando orderService.createOrder()...
   ✓ Pedido creado exitosamente con ID: 100
   ✅ Todas las aserciones pasaron correctamente
   📊 Resultado: ID=100, State=PROCESSING, Active=true
   ✅ Verificación de mock: orderJpaRepository.save() fue invocado 1 vez
✅ [TEST PASADO] Creación exitosa con datos válidos

🧪 [TEST] Iniciando test: Validación de campo 'name' requerido
   ⚠️ DTO creado SIN campo 'name' (esperamos que falle)
   ⚙️ Invocando createOrder (debe lanzar IllegalArgumentException)...
   ✅ Excepción lanzada correctamente: El campo 'name' es requerido
   ✅ Mensaje de error validado correctamente
✅ [TEST PASADO] Validación de campo 'name' requerido

[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## 🚀 Cómo Ejecutar para Ver los Logs

### En IntelliJ IDEA:
1. Clic derecho en `OrderServiceHuOrd05Test.java`
2. Selecciona "Run 'OrderServiceHuOrd05Test'"
3. Ve a la pestaña "Console" (parte inferior)
4. Verás todos los logs con emojis y detalles

### Con Maven (si está instalado):
```powershell
mvn test -Dtest=OrderServiceHuOrd05Test
```

### Con Docker (si Maven no está disponible):
```powershell
docker run --rm -v "${PWD}:/app" -w /app eclipse-temurin:21-jdk sh -c "cd Backend/pedido-service && ./mvnw test -Dtest=OrderServiceHuOrd05Test"
```

## 💡 Mejores Prácticas

### ✅ HACER:
- Usar logs en tests **complejos** o **críticos**
- Agregar logs cuando **debuggeas** un problema
- Usar `@DisplayName` descriptivos (ya lo tenemos)
- Confiar en el ícono verde cuando el test pasa

### ❌ EVITAR:
- Agregar logs en **todos** los tests (ruido innecesario)
- Dejar logs en producción (eliminar después de debuggear)
- Preocuparse cuando solo ves "tests pasados" (es lo esperado)

## 🎓 Filosofía de Testing

> **"No news is good news"**  
> Los tests silenciosos cuando pasan son una **característica**, no un bug.

Los detalles solo son necesarios cuando algo falla. Un test que pasa significa:
- ✅ Todas las aserciones pasaron
- ✅ No hubo excepciones inesperadas
- ✅ El código cumple con el contrato esperado

## 📚 Referencias

- [JUnit 5 User Guide - Console Launcher](https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher)
- [Maven Surefire Plugin - Test Reports](https://maven.apache.org/surefire/maven-surefire-plugin/usage.html)
- [IntelliJ IDEA - Run Tests](https://www.jetbrains.com/help/idea/performing-tests.html)

---

**Resumen**: Es normal ver solo "tests pasados" sin detalles. He agregado logs en tests clave para que veas el flujo cuando los ejecutes. Si necesitas más información, usa las opciones descritas arriba.
