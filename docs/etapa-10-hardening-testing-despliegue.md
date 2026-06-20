# Etapa 10 - Hardening, testing y despliegue

## Objetivo

Cerrar el proyecto con una configuracion mas segura, artefactos reales de despliegue y verificaciones tecnicas que mejoren su calidad como proyecto de portafolio.

## Estructura

```text
backend
|-- Dockerfile
|-- .dockerignore
`-- src/main/resources
    |-- application.yml
    |-- application-dev.yml
    `-- application-prod.yml

frontend
|-- Dockerfile
|-- .dockerignore
`-- nginx.conf

docs
`-- etapa-10-hardening-testing-despliegue.md
```

## Cambios implementados

- Perfiles `dev`, `test` y `prod`.
- Validacion de secretos inseguros al iniciar en `prod`.
- Headers HTTP de seguridad en Spring Security.
- CORS configurable por propiedad.
- Dockerfile para backend con Java 21.
- Dockerfile para frontend con build de Vite y Nginx.
- `docker-compose.yml` extendido para levantar postgres, backend y frontend.
- `.env.example` para documentar variables.
- Test de hardening para salud publica y proteccion de endpoints privados.

## Buenas practicas aplicadas

- Sin secretos hardcodeados en produccion.
- `ddl-auto: validate` en `prod`.
- Logging SQL solo en `dev`.
- Despliegue reproducible con contenedores.
- Seguridad verificada con tests automatizados.

## Commits sugeridos

- `feat: add deployment and runtime hardening`
- `test: add security hardening integration tests`
- `docs: add stage 10 hardening notes`
