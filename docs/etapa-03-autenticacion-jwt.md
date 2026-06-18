# Etapa 3 - Autenticacion con Spring Security y JWT

## Objetivo

Implementar autenticacion stateless con Spring Security + JWT, incluyendo registro, login y roles `ADMIN` y `EMPLEADO`, para proteger el resto de modulos del sistema.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory
├── auth
│   ├── controller
│   ├── dto
│   └── service
├── security
│   └── jwt
├── user
│   ├── entity
│   ├── repository
│   └── service
├── config
└── exception
```

## Flujo implementado

1. `POST /api/auth/register`
   Registra usuarios publicos como `EMPLEADO`.
2. `POST /api/auth/login`
   Autentica por email y password y devuelve JWT.
3. `JwtAuthenticationFilter`
   Lee el header `Authorization: Bearer ...` y autentica la request.
4. `SecurityConfig`
   Deja `/api/auth/**` y `/api/health` publicos, y protege el resto.
5. `DataInitializer`
   Crea un usuario administrador inicial para el ambiente de desarrollo.

## Decisiones de seguridad

- Registro publico limitado a `EMPLEADO` para evitar creacion libre de administradores.
- Usuario administrador bootstrap configurable por variables de entorno.
- Passwords cifradas con `BCryptPasswordEncoder`.
- Seguridad stateless lista para frontend React con JWT.

## Buenas practicas aplicadas

- DTOs separados de entidades.
- Roles modelados con enum explicito.
- Filtro JWT aislado de la logica de negocio.
- `UserDetailsService` propio para integrar dominio y Spring Security.
- Tests de integracion para registro, login y proteccion de endpoints.

## Commits sugeridos

- `feat: add jwt authentication and user roles`
- `test: add authentication integration tests`
- `docs: add stage 3 authentication notes`

