# Etapa 13 - Frontend de productos y categorias

## Objetivo

Construir el primer modulo CRUD real del frontend para administrar productos y categorias, conectado al backend autenticado y respetando permisos por rol.

## Estructura

```text
frontend/src
|-- lib
|   `-- formatters.js
|-- pages
|   `-- ProductsPage.jsx
|-- App.jsx
`-- index.css
```

## Funcionalidades implementadas

- Listado autenticado de productos y categorias.
- Filtro por texto y categoria para productos.
- Alta, edicion y eliminacion de productos.
- Alta, edicion y eliminacion de categorias.
- Restriccion de acciones de escritura para `EMPLEADO`.
- Reutilizacion de la sesion JWT para todas las operaciones.

## Buenas practicas aplicadas

- Separacion entre estado de formulario, filtros y carga de datos.
- Reutilizacion de `apiRequest` y `AuthContext` en vez de duplicar logica HTTP.
- Validaciones minimas en el formulario antes de enviar al backend.
- UI preparada para modo lectura cuando el usuario no es `ADMIN`.

## Commits sugeridos

- `feat: add frontend products and categories module`
- `docs: add stage 13 frontend products notes`
