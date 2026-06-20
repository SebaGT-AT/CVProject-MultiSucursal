# Etapa 12 - Frontend de dashboard

## Objetivo

Consumir el endpoint real `/api/dashboard` desde el frontend autenticado para mostrar indicadores operativos y productos con bajo stock.

## Estructura

```text
frontend/src
|-- lib
|   `-- formatters.js
|-- pages
|   `-- DashboardHomePage.jsx
`-- index.css
```

## Funcionalidades implementadas

- Carga autenticada del dashboard con JWT.
- Estados de carga, error y tabla vacia.
- Tarjetas para ventas del dia, compras del mes y bajo stock.
- Tabla de productos criticos por sucursal.

## Buenas practicas aplicadas

- Formateadores centralizados para moneda y fechas.
- Separacion entre fetch, presentacion y estilos.
- UI resiliente ante errores y cargas lentas.
- Base visual lista para reutilizar en modulos CRUD.

## Commits sugeridos

- `feat: add frontend dashboard view`
- `docs: add stage 12 dashboard frontend notes`
