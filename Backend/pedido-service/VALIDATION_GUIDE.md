# 🧪 Guía de Validación - HU-ORD-01 Refactorizado

## 📋 Pre-requisitos

- Java 17+
- Maven 3.6+
- PostgreSQL corriendo (o usar docker-compose)

---

## ✅ Pasos para Validar el Refactor

### 1. **Compilar el Proyecto**

```powershell
# Navegar al directorio del servicio
cd "C:\Users\Santiago\Documents\Equipo 2\Diagnostico-Semana0\Backend\pedido-service"

# Compilar (limpieza + compilación)
mvn clean compile
```

**Resultado esperado:** ✅ BUILD SUCCESS

---

### 2. **Ejecutar Tests Unitarios**

```powershell
# Ejecutar SOLO los tests de HU-ORD-01
mvn test -Dtest=OrderServiceHuOrd01Test

# O ejecutar todos los tests del servicio
mvn test
```

**Resultado esperado:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Tests que deben pasar:**
- ✅ `findAllActiveOrders_shouldReturnOnlyActiveOrders`
- ✅ `findAllActiveOrders_shouldReturnEmptyListWhenNoActiveOrders`
- ✅ `findAllActiveOrders_shouldReturnEmptyListWhenDatabaseEmpty`

---

### 3. **Verificar Cobertura de Código (Opcional)**

```powershell
# Generar reporte de cobertura con JaCoCo
mvn clean test jacoco:report

# Ver reporte en:
# Backend/pedido-service/target/site/jacoco/index.html
```

**Cobertura esperada:**
- OrderService.findAllActiveOrders(): **100%**

---

### 4. **Iniciar el Servicio Localmente**

#### Opción A: Con Docker Compose (Recomendado)

```powershell
# Desde la raíz del proyecto
cd "C:\Users\Santiago\Documents\Equipo 2\Diagnostico-Semana0"

# Iniciar todos los servicios
docker-compose up --build pedido-service
```

#### Opción B: Maven local (requiere PostgreSQL corriendo)

```powershell
cd "C:\Users\Santiago\Documents\Equipo 2\Diagnostico-Semana0\Backend\pedido-service"

# Ejecutar en modo desarrollo
mvn spring-boot:run
```

**Puerto:** http://localhost:8082

---

### 5. **Validación Manual con Curl**

#### 5.1. Listar todos los pedidos activos

```powershell
# Windows PowerShell
Invoke-WebRequest -Uri http://localhost:8082/order/all -Method GET | Select-Object -ExpandProperty Content | ConvertFrom-Json

# O usando curl (si está instalado)
curl http://localhost:8082/order/all
```

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "name": "Pedido 1",
    "description": "...",
    "idUser": 5,
    "state": "PROCESSING",
    "active": true
  }
]
```

**Validaciones:**
- ✅ Código HTTP: 200 OK
- ✅ Retorna array JSON
- ✅ **TODOS** los pedidos tienen `"active": true`
- ✅ **NINGUNO** tiene `"active": false`

#### 5.2. Verificar que NO retorna pedidos inactivos

**Preparación:** Crear un pedido y "eliminarlo" (soft-delete)

```powershell
# 1. Crear pedido
$body = @{
    name = "Pedido Test"
    description = "Test para validar filtrado"
    idUser = 10
} | ConvertTo-Json

Invoke-WebRequest -Uri http://localhost:8082/order/add -Method POST -Body $body -ContentType "application/json"

# 2. "Eliminar" el pedido (setea active=false)
Invoke-WebRequest -Uri http://localhost:8082/order/1 -Method DELETE

# 3. Listar pedidos activos
Invoke-WebRequest -Uri http://localhost:8082/order/all -Method GET | Select-Object -ExpandProperty Content
```

**Resultado esperado:**
- ✅ El pedido eliminado NO aparece en `/order/all`
- ✅ Solo aparecen pedidos con active=true

---

### 6. **Validación de Documentación**

#### 6.1. Verificar Javadoc generado

```powershell
# Generar Javadoc
mvn javadoc:javadoc

# Ver documentación en:
# Backend/pedido-service/target/site/apidocs/index.html
```

**Verificar que existe documentación para:**
- ✅ `OrderService.findAllActiveOrders()`
- ✅ `OrderController.listAllOrders()`

---

### 7. **Checklist Final de Validación**

#### Funcionalidad
- [ ] Tests unitarios pasan (3/3)
- [ ] Endpoint `/order/all` retorna solo activos
- [ ] Pedidos con active=false son excluidos
- [ ] Lista vacía cuando no hay activos
- [ ] Código HTTP 200 OK en todos los casos

#### Calidad de Código
- [ ] Sin errores de compilación
- [ ] Solo warnings menores en Javadoc (aceptables)
- [ ] Métodos helper utilizados en tests
- [ ] Javadoc completo en código de producción
- [ ] Referencias a HU-ORD-01 en comentarios

#### Documentación
- [ ] HU-ORD-01_REFACTOR_REPORT.md creado
- [ ] HU-ORD-01_REFACTOR_SUMMARY.md creado
- [ ] COMMIT_MESSAGE_HU-ORD-01_REFACTOR.md creado
- [ ] VALIDATION_GUIDE.md (este archivo) creado

---

## 🐛 Troubleshooting

### Problema: Maven no encontrado
```powershell
# Agregar Maven al PATH o usar ruta completa
C:\path\to\maven\bin\mvn.cmd clean test
```

### Problema: Puerto 8082 en uso
```powershell
# Cambiar puerto en application.properties
server.port=8083
```

### Problema: PostgreSQL no conecta
```powershell
# Verificar que PostgreSQL está corriendo
docker ps | Select-String postgres

# O iniciar solo PostgreSQL
docker-compose up -d postgres
```

### Problema: Tests fallan
```powershell
# Ver logs detallados
mvn test -Dtest=OrderServiceHuOrd01Test -X

# Verificar que no hay cambios no guardados
git status
```

---

## 📊 Criterios de Éxito

### ✅ Refactor Exitoso Si:

1. **Tests:** 3/3 tests en GREEN ✅
2. **Compilación:** Sin errores ✅
3. **Funcionalidad:** Endpoint retorna solo activos ✅
4. **Documentación:** Javadoc completo ✅
5. **SOLID:** Principios aplicados ✅
6. **Clean Code:** DRY, nombres descriptivos ✅

### ⚠️ Requiere Corrección Si:

- ❌ Algún test falla
- ❌ Errores de compilación
- ❌ Endpoint retorna pedidos inactivos
- ❌ Funcionalidad cambió (breaking change)

---

## 🚀 Siguiente Paso: Commit

Una vez validado todo:

```powershell
# Ver cambios
git status
git diff

# Stage cambios
git add Backend/pedido-service/src/main/java/com/example/pedidoservice/controller/OrderController.java
git add Backend/pedido-service/src/main/java/com/example/pedidoservice/service/OrderService.java
git add Backend/pedido-service/src/test/java/com/example/pedidoservice/service/OrderServiceHuOrd01Test.java
git add Backend/pedido-service/*.md

# Commit usando el mensaje estructurado
git commit -F Backend/pedido-service/COMMIT_MESSAGE_HU-ORD-01_REFACTOR.md
```

---

## 📚 Documentación de Referencia

- **HU-ORD-01_REFACTOR_REPORT.md**: Detalles técnicos completos
- **HU-ORD-01_REFACTOR_SUMMARY.md**: Resumen ejecutivo
- **USER_STORIES/HU-ORD-01.md**: Requisitos originales
- **TEST_PLAN.md**: Estrategia de testing general

---

## ✨ Conclusión

Si todos los pasos de validación pasan, el refactor está **READY FOR PRODUCTION** 🚀

**Fase TDD completada:**
- 🔴 RED: Tests escritos primero ✅
- 🟢 GREEN: Implementación mínima ✅
- ♻️ REFACTOR: Mejora de código ✅

---

**Happy Coding! 🎉**
