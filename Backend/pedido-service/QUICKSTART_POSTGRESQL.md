# 🚀 Quick Start - PostgreSQL Migration

## ⚡ Inicio Rápido

### Opción 1: Docker Compose (Todo en Uno)

```powershell
# Construir e iniciar todos los servicios (PostgreSQL + RabbitMQ + Servicios)
docker-compose up --build

# En otra terminal, verificar que todo esté corriendo
docker ps
```

**Servicios disponibles:**
- Pedido Service: http://localhost:8082
- Usuario Service: http://localhost:8083
- RabbitMQ Management: http://localhost:15672
- PostgreSQL: localhost:5432

### Opción 2: Desarrollo Local

```powershell
# 1. Iniciar solo PostgreSQL y RabbitMQ
docker-compose up -d postgres rabbitmq

# 2. Ejecutar pedido-service en modo dev
cd Backend\pedido-service
mvn spring-boot:run

# 3. Ejecutar usuario-service en modo dev
cd Backend\usuario-service
mvn spring-boot:run

# 4. Ejecutar frontend
cd Frontend
npm install
npm run dev
```

---

## ✅ Verificación Rápida

### 1. Verificar PostgreSQL

```powershell
# Conectarse a la base de datos
docker exec -it orders_db psql -U pedido_user -d orders_db

# Ver datos
SELECT * FROM orders WHERE active = true;
\q
```

### 2. Probar API

```powershell
# Listar pedidos activos (GET /order/all)
Invoke-WebRequest -Uri http://localhost:8082/order/all | `
  Select-Object -ExpandProperty Content

# Crear pedido (POST /order/add)
$body = '{"name":"Test Order","description":"Test","idUser":1,"state":"PROCESSING","active":true}'
Invoke-WebRequest -Uri http://localhost:8082/order/add `
  -Method POST -Body $body -ContentType "application/json"
```

### 3. Ejecutar Tests

```powershell
cd Backend\pedido-service

# Tests unitarios
mvn test

# Tests específicos de HU-ORD-01
mvn test -Dtest=OrderServiceHuOrd01Test
```

---

## 📝 Configuración de Base de Datos

### Variables de Entorno

| Variable | Valor por Defecto | Descripción |
|----------|------------------|-------------|
| `DB_HOST` | localhost | Hostname de PostgreSQL |
| `DB_PORT` | 5432 | Puerto de PostgreSQL |
| `DB_NAME` | orders_db | Nombre de la base de datos |
| `DB_USER` | pedido_user | Usuario de la BD |
| `DB_PASSWORD` | pedido_pass | Contraseña |

### Cambiar Configuración

```powershell
# En Docker Compose (editar docker-compose.yml)
environment:
  - DB_HOST=mi-postgres-server
  - DB_NAME=mi_base_datos

# En desarrollo local (editar application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/mi_db
spring.datasource.username=mi_usuario
```

---

## 🗄️ Estructura de Base de Datos

### Tabla: orders

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,           -- Auto-incremental
    name VARCHAR(255) NOT NULL,      -- Nombre del pedido
    description TEXT,                -- Descripción
    id_user INTEGER NOT NULL,        -- ID del usuario
    state VARCHAR(50) NOT NULL,      -- PROCESSING | SHIPPED | DELIVERED | CANCELED
    active BOOLEAN NOT NULL,         -- Soft-delete flag
    created_at TIMESTAMP,            -- Fecha de creación
    updated_at TIMESTAMP             -- Última actualización
);
```

### Datos de Prueba

Se insertan automáticamente al iniciar PostgreSQL:
- 5 pedidos de ejemplo
- 4 activos, 1 inactivo
- Estados variados

---

## 🐛 Problemas Comunes

### "Cannot connect to PostgreSQL"

```powershell
# Verificar que PostgreSQL esté corriendo
docker ps | Select-String postgres

# Si no está corriendo, iniciarlo
docker-compose up -d postgres

# Ver logs
docker-compose logs postgres
```

### "Table 'orders' doesn't exist"

```powershell
# Recrear base de datos con script de inicialización
docker-compose down -v
docker-compose up --build postgres
```

### "Port 5432 already in use"

```powershell
# Detener servicios existentes
Stop-Process -Name postgres -Force

# O cambiar puerto en docker-compose.yml
ports:
  - "5433:5432"  # Usar puerto 5433 en host
```

---

## 📚 Documentación Completa

Para detalles completos sobre la migración, ver:
- **POSTGRESQL_MIGRATION.md** - Documentación completa de la migración
- **HU-ORD-01_REFACTOR_REPORT.md** - Reporte de refactorización
- **VALIDATION_GUIDE.md** - Guía de validación y testing

---

## 🎯 Siguiente Paso

Una vez que todo esté corriendo:

1. ✅ Verificar que PostgreSQL tenga datos
2. ✅ Probar endpoints REST
3. ✅ Ejecutar tests unitarios
4. ✅ Probar desde el frontend

---

**🎉 ¡Listo para desarrollo!**
