# E2E Integration Tests

Tests de integración de servicios end-to-end que validan el sistema completo corriendo con Docker Compose.

## Estructura

- `basic.test.js` - Tests básicos de ejemplo (incluye uno que falla intencionalmente)
- `services.test.js` - Tests E2E reales para los servicios (marcados como `.skip` por defecto)

## Ejecutar localmente

```bash
# Instalar dependencias
npm install

# Asegúrate de que los servicios estén corriendo
cd ../..
docker compose up -d

# Ejecutar tests
cd tests/e2e
npm test
```

## Tests incluidos

### basic.test.js
- ✅ Test que pasa: 2 + 2 = 4
- ❌ Test que falla: 3 + 3 = 5 (falla intencionalmente para demostrar)

### services.test.js (skip por defecto)
- Verificación de servicios corriendo
- Flujo E2E: crear usuario → crear pedido → verificar

Para habilitar estos tests, quita el `.skip` en los tests.

## Configuración

Los tests usan estas URLs por defecto:
- Usuario Service: `http://localhost:8083`
- Pedido Service: `http://localhost:8082`
- Frontend: `http://localhost:3000`

Puedes sobrescribirlas con variables de entorno:
- `USUARIO_SERVICE_URL`
- `PEDIDO_SERVICE_URL`
- `FRONTEND_URL`
