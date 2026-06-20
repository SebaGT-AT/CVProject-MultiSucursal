# Etapa 15 - Frontend de stock y movimientos

## Objetivo

Construir una consola frontend para consultar stock por sucursal, revisar movimientos historicos y ejecutar ajustes o transferencias usando los endpoints reales del backend.

## Estructura

```text
frontend/src
|-- lib
|   `-- formatters.js
|-- pages
|   `-- StockPage.jsx
|-- App.jsx
`-- index.css
```

## Funcionalidades implementadas

- Consulta de stock consolidado por sucursal y producto.
- Filtros por sucursal, producto, tipo de movimiento y bajo stock.
- Historial reciente de movimientos con fecha, referencia y notas.
- Ajustes manuales de stock para `ADMIN`.
- Transferencias entre sucursales para `ADMIN`.
- Modo de solo lectura para `EMPLEADO`.

## Buenas practicas aplicadas

- Reutilizacion de `apiRequest`, `AuthContext` y formateadores compartidos.
- Separacion entre filtros, listado, historial y formularios operativos.
- Manejo consistente de carga, errores y reseteo de formularios.
- UI orientada a operacion real y trazabilidad, no solo a CRUD basico.

## Commits sugeridos

- `feat: add frontend stock operations module`
- `docs: add stage 15 frontend stock notes`
