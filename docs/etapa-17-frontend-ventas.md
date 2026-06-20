# Etapa 17 - Frontend de ventas

## Objetivo

Construir el modulo frontend de ventas para registrar salidas comerciales por sucursal con items dinamicos y consultar el historial de ventas procesadas por el backend.

## Estructura

```text
frontend/src
|-- pages
|   `-- SalesPage.jsx
|-- App.jsx
|-- components
|   `-- AuthShell.jsx
`-- index.css
```

## Funcionalidades implementadas

- Listado autenticado de ventas registradas.
- Filtros por sucursal y busqueda por numero, cliente o sede.
- Detalle expandible por venta con sus items.
- Formulario de alta con encabezado y multiples items.
- Calculo estimado del total en el frontend antes de enviar.
- Restriccion de escritura para `EMPLEADO`.

## Buenas practicas aplicadas

- Reutilizacion de la misma arquitectura usada en compras para mantener coherencia.
- Manejo dinamico de items sin dependencias adicionales.
- Separacion entre historial, filtros y formulario.
- Flujo alineado con el backend para que el descuento de stock ocurra desde la logica de negocio central.

## Commits sugeridos

- `feat: add frontend sales module`
- `docs: add stage 17 frontend sales notes`
