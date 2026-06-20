# Etapa 14 - Frontend de sucursales

## Objetivo

Construir el modulo frontend de sucursales para administrar sedes del negocio desde React, consumiendo el backend autenticado y respetando permisos por rol.

## Estructura

```text
frontend/src
|-- pages
|   `-- BranchesPage.jsx
|-- App.jsx
`-- index.css
```

## Funcionalidades implementadas

- Listado autenticado de sucursales.
- Busqueda por codigo, nombre, ciudad y direccion.
- Filtro por estado activo/inactivo.
- Alta, edicion y eliminacion de sucursales.
- Modo solo lectura para `EMPLEADO`.

## Buenas practicas aplicadas

- Reutilizacion de autenticacion y capa HTTP ya existente.
- Formularios desacoplados del listado principal.
- Estados de carga, error y edicion consistentes con otros modulos.
- Base visual alineada con dashboard y productos para mantener coherencia UX.

## Commits sugeridos

- `feat: add frontend branches module`
- `docs: add stage 14 frontend branches notes`
