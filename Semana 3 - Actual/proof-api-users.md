# Proof API - Orders (HU-ORD-08 + HU-ORD-09)

Este documento consolida peticiones **exitosas** y **fallidas** para validar la API de orders según los criterios de aceptación de HU-ORD-08 y HU-ORD-09.

## Supuestos de prueba

- Backwards compatibility deshabilitada (endpoint legacy `/order/*` debe responder 404).
- Base URL local de `pedido-service`: `http://localhost:8082`.
- Para el caso 500, se simula caída de PostgreSQL.

## Tabla de peticiones exitosas

| ID | Historia / CA | Método | Endpoint | Body / Query | Resultado esperado | Resultado | Especificaciones |
|---|---|---|---|---|---|---|---|
| S-01 | HU-ORD-09 CA-01 | GET | `/orders` | - | `200 OK` y arreglo con todas las órdenes | Exitosa | - |
| S-02 | HU-ORD-09 CA-02 + HU-ORD-08 CA-01 | POST | `/orders` | JSON válido de orden | `201 Created` + header `Location: /orders/{id}` | Exitosa | Aunque la ejecucion es exitosa, crea el producto pero con base en un usuario inexistente, corregir |
| S-03 | HU-ORD-09 CA-03 | GET | `/orders?userId=1` | `userId=1` | `200 OK` y solo órdenes del usuario 1 | Exitosa | - |
| S-04 | HU-ORD-09 CA-04 | GET | `/orders/1?expand=user` | `expand=user` | `200 OK` con datos de orden + datos de usuario | Exitosa | - |
| S-05 | HU-ORD-08 CA-02 | DELETE | `/orders/1` | - | `204 No Content` y cuerpo vacío | Exitosa | Ejecución exitosa, pero toca cambiar el método |
| S-06 | HU-ORD-09 CA-06 | GET | `/orders?invalidParam=xyz` | `invalidParam=xyz` | `200 OK` (parámetro ignorado) | Exitosa | Spring envía todas las ordenes cuando se ejecuta cualquier parametro |

## Tabla de peticiones fallidas

| ID | Historia / CA | Método | Endpoint | Body / Query | Resultado esperado | Resultado | Especificaciones |
|---|---|---|---|---|---|---|---|
| F-01 | HU-ORD-08 CA-03 | GET | `/orders/999` | - | `404 Not Found` + JSON con `timestamp,status,error,message,path` | Exitosa | - |
| F-02 | HU-ORD-08 CA-04 | POST | `/orders` | JSON inválido (sin `name`) | `400 Bad Request` + JSON estructurado de error | Exitosa | - |
| F-03 | HU-ORD-08 CA-06 | DELETE | `/orders/999` | - | `404 Not Found` + JSON estructurado de error | Exitosa | - |
| F-04 | HU-ORD-08 CA-05 | GET | `/orders` | PostgreSQL detenido | `500 Internal Server Error` + mensaje genérico (sin stack trace) | Exitosa | - |
| F-05 | HU-ORD-09 CA-07 | GET | `/order/all` | - | `404 Not Found` (endpoint legacy no disponible) | - | - |

## Datos mínimos esperados en errores (HU-ORD-08)

```json
{
  "timestamp": "2026-02-25T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order with id 999 not found",
  "path": "/orders/999"
}
```

