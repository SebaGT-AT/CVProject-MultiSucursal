# Etapa 16 - Frontend de compras

## Objetivo

Construir el modulo frontend de compras para registrar abastecimiento por sucursal con items dinamicos y consultar el historial de compras ya procesadas por el backend.

## Estructura

```text
frontend/src
|-- pages
|   `-- PurchasesPage.jsx
|-- App.jsx
|-- components
|   `-- AuthShell.jsx
`-- index.css
```

## Funcionalidades implementadas

- Listado autenticado de compras registradas.
- Filtros por sucursal y busqueda por numero, proveedor o sede.
- Detalle expandible por compra con sus items.
- Formulario de alta con encabezado y multiples items.
- Calculo estimado del total en el frontend antes de enviar.
- Restriccion de escritura para `EMPLEADO`.

## Buenas practicas aplicadas

- Reutilizacion de capa API y autenticacion existentes.
- Manejo dinamico de items sin librerias extras.
- Separacion entre historial, filtros y formulario.
- Flujo alineado con el backend para que el stock se actualice desde la logica de negocio central.

## Commits sugeridos

- `feat: add frontend purchases module`
- `docs: add stage 16 frontend purchases notes`
