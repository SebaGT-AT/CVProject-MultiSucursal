# Etapa 9 - Dashboard e indicadores

## Objetivo

Implementar un endpoint de dashboard backend que concentre los indicadores principales del negocio: productos con bajo stock, ventas del dia y compras del mes.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/dashboard
|-- controller
|-- dto
`-- service
```

## Endpoints implementados

- `GET /api/dashboard`

## Reglas aplicadas

- `ADMIN` y `EMPLEADO` pueden consultar el dashboard.
- Los productos con bajo stock son los que cumplen `quantity <= minimumStock`.
- Las ventas del dia consideran solo registros con fecha actual.
- Las compras del mes consideran solo registros del mes actual.

## Buenas practicas aplicadas

- Servicio de dashboard desacoplado de los modulos transaccionales.
- Consultas agregadas en repositorios para evitar calculos innecesarios en memoria.
- Respuesta compuesta lista para consumo del frontend.
- Tests de integracion validando filtros temporales y permisos.

## Commits sugeridos

- `feat: add dashboard summary endpoint`
- `test: add dashboard integration tests`
- `docs: add stage 9 dashboard module notes`
