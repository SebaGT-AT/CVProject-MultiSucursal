# Etapa 7 - Compras

## Objetivo

Implementar el registro de compras por sucursal con detalle de items, calculo de totales y actualizacion automatica del stock y del historial de movimientos.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/purchase
|-- controller
|-- dto
|-- entity
|-- repository
`-- service
```

## Endpoints implementados

- `GET /api/purchases`
- `GET /api/purchases/{id}`
- `GET /api/purchases/branch/{branchId}`
- `POST /api/purchases`

## Reglas aplicadas

- `ADMIN` puede registrar compras.
- `ADMIN` y `EMPLEADO` pueden consultar compras.
- Cada compra debe tener al menos un item.
- El numero de compra debe ser unico.
- Cada item incrementa el stock de su producto en la sucursal seleccionada.
- Cada item genera un movimiento de stock de tipo `PURCHASE`.

## Buenas practicas aplicadas

- Servicio transaccional para guardar compra, detalle y actualizacion de stock en una sola operacion.
- DTOs separados para request y response.
- Totales calculados en backend para evitar inconsistencias.
- Consultas con `EntityGraph` para evitar carga perezosa al serializar respuestas.
- Tests de integracion cubriendo seguridad, compra y efecto real sobre stock.

## Commits sugeridos

- `feat: add purchase registration workflow`
- `test: add purchase integration tests`
- `docs: add stage 7 purchase module notes`
