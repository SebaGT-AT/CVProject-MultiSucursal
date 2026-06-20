# Etapa 11 - Frontend de autenticacion

## Objetivo

Transformar el frontend inicial en una aplicacion autenticada real, conectada al backend con JWT, con rutas protegidas y una base lista para consumir modulos funcionales.

## Estructura

```text
frontend/src
|-- auth
|   |-- AuthContext.jsx
|   `-- auth-storage.js
|-- components
|   |-- AppNavbar.jsx
|   |-- AuthShell.jsx
|   `-- ProtectedRoute.jsx
|-- lib
|   `-- api.js
|-- pages
|   |-- DashboardHomePage.jsx
|   |-- LoginPage.jsx
|   `-- ModulePlaceholderPage.jsx
|-- App.jsx
|-- main.jsx
`-- index.css
```

## Funcionalidades implementadas

- Login conectado a `POST /api/auth/login`.
- Persistencia de sesion en `localStorage`.
- Contexto global de autenticacion.
- Rutas protegidas con redireccion a login.
- Logout.
- Layout autenticado base para las siguientes etapas.

## Buenas practicas aplicadas

- Separacion entre capa de API, estado de autenticacion y paginas.
- Persistencia encapsulada en utilidades propias.
- Rutas protegidas desacopladas del layout.
- Base visual reutilizable para dashboard y CRUDs futuros.

## Commits sugeridos

- `feat: add frontend authentication flow`
- `docs: add stage 11 frontend auth notes`
