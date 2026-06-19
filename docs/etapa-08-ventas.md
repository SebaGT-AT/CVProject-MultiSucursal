# Etapa 8 - Ventas

## Objetivo

Implementar el registro de ventas por sucursal, con detalle de items, calculo de totales y descuento automatico del stock disponible.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/sale
|-- controller
|-- dto
|-- entity
|-- repository
`-- service
```

## Endpoints implementados

- `GET /api/sales`
- `GET /api/sales/{id}`
- `GET /api/sales/branch/{branchId}`
- `POST /api/sales`

## Reglas aplicadas

- `ADMIN` puede registrar ventas.
- `ADMIN` y `EMPLEADO` pueden consultar ventas.
- Cada venta debe tener al menos un item.
- El numero de venta debe ser unico.
- No se permite vender mas unidades que las disponibles en la sucursal.
- Cada item descuenta stock y genera un movimiento `SALE`.

## Buenas practicas aplicadas

- Servicio transaccional para asegurar consistencia entre venta, stock y movimientos.
- Validacion explicita de stock disponible antes de descontar.
- DTOs separados para requests y responses.
- Totales calculados en backend.
- Pruebas de integracion para flujo exitoso, seguridad y rechazo por stock insuficiente.

## Commits sugeridos

- `feat: add sales registration workflow`
- `test: add sales integration tests`
- `docs: add stage 8 sales module notes`
