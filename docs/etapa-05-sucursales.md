# Etapa 5 - CRUD de sucursales

## Objetivo

Implementar el modulo de sucursales con CRUD completo, validaciones de negocio y control de acceso por roles, dejando preparada la base para el stock por sucursal de la siguiente etapa.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/branch
├── controller
├── dto
├── entity
├── repository
└── service
```

## Endpoints implementados

- `GET /api/branches`
- `GET /api/branches/{id}`
- `POST /api/branches`
- `PUT /api/branches/{id}`
- `DELETE /api/branches/{id}`

## Reglas aplicadas

- `ADMIN` puede crear, editar y eliminar sucursales.
- `ADMIN` y `EMPLEADO` pueden consultar.
- `code` debe ser unico.
- `email` debe ser unico cuando se informa.
- No se puede eliminar una sucursal si ya tiene historial de stock o movimientos.

## Buenas practicas aplicadas

- DTOs de entrada y salida.
- Normalizacion de `code` y `email`.
- Validaciones de unicidad en servicio antes de persistir.
- Excepcion semantica `ResourceNotFoundException`.
- Tests de integracion con JWT y escenarios por rol.

## Commits sugeridos

- `feat: add branch CRUD endpoints`
- `test: add branch integration tests`
- `docs: add stage 5 branch module notes`

