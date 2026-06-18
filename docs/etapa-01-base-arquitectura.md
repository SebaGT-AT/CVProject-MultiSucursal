# Etapa 1 - Base del proyecto y arquitectura inicial

## Objetivo

Dejar preparado un monorepo profesional con separación clara entre backend y frontend, configuración inicial consistente y una base mantenible para avanzar por etapas sin rehacer estructura.

## Estructura

```text
MultiSucursal/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/multisucursal/inventory/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   └── InventoryApplication.java
│       │   └── resources/application.yml
│       └── test/java/com/multisucursal/inventory/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── components/
│       ├── App.jsx
│       ├── index.css
│       └── main.jsx
├── docs/
└── docker-compose.yml
```

## Decisiones de arquitectura

- Monorepo simple para que el reclutador vea todo el sistema en un solo lugar.
- Backend organizado por capas al inicio: `controller`, `service`, `repository`, `entity`, `dto`, `config`, `security`.
- Frontend mínimo pero limpio, listo para crecer por features.
- PostgreSQL definido en `docker-compose` para tener entorno reproducible desde el comienzo.
- Configuración YAML centralizada para perfiles futuros como `dev` y `prod`.
- Perfil de test desacoplado de PostgreSQL usando H2 en memoria.

## Buenas prácticas aplicadas

- Nombrado consistente en inglés dentro del código.
- Documentación de etapa desde el primer commit.
- Separación de configuración, entrada HTTP y bootstrap de aplicación.
- CORS preparado para integración local con Vite.
- Tests de arranque independientes de infraestructura externa.
- Landing inicial del frontend orientada a portafolio, no a simple plantilla vacía.

## Commits sugeridos

- `chore: initialize monorepo structure for multisucursal system`
- `feat: add spring boot backend base configuration`
- `feat: add react vite frontend base layout`
- `docs: add stage 1 architecture notes and roadmap`
