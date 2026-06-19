# Etapa 6 - Stock por sucursal y movimientos

## Objetivo

Implementar la gestion de stock por sucursal, ajustes de inventario, transferencias internas e historial de movimientos, dejando la base lista para compras y ventas.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/stock
├── controller
├── dto
├── entity
├── repository
└── service
```

## Endpoints implementados

- `GET /api/stocks`
- `GET /api/stocks/branch/{branchId}`
- `GET /api/stocks/product/{productId}`
- `GET /api/stocks/movements`
- `GET /api/stocks/movements/branch/{branchId}`
- `GET /api/stocks/movements/product/{productId}`
- `POST /api/stocks/adjustments`
- `POST /api/stocks/transfers`

## Reglas aplicadas

- `ADMIN` puede ajustar stock y transferir entre sucursales.
- `ADMIN` y `EMPLEADO` pueden consultar stock y movimientos.
- No se permiten transferencias entre la misma sucursal.
- No se permite dejar stock negativo.
- `INITIAL_LOAD` solo se permite cuando el stock actual es cero.

## Buenas practicas aplicadas

- Servicio centralizado para reglas de inventario.
- Historial de movimientos generado automaticamente en cada ajuste o transferencia.
- DTOs separados para ajustes, transferencias y respuestas.
- Respuestas de stock con `lowStock` calculado.
- Tests de integracion con flujo completo de ajuste, transferencia y consultas.

## Commits sugeridos

- `feat: add branch stock and movement management`
- `test: add stock integration tests`
- `docs: add stage 6 stock module notes`

