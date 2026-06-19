# Sistema de Inventario Multi-Sucursal

Proyecto de portafolio profesional orientado a demostrar capacidades de desarrollo backend con Java 21, Spring Boot, Spring Security, JWT, JPA/Hibernate y PostgreSQL, acompañado de un frontend en React + Vite + Bootstrap.

## Stack

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- PostgreSQL
- React + Vite
- Bootstrap
- Maven
- Git

## Estructura del repositorio

```text
MultiSucursal/
├── backend/
├── frontend/
├── docs/
├── docker-compose.yml
└── README.md
```

## Roadmap de construcción

1. Base del proyecto y arquitectura inicial
2. Persistencia, PostgreSQL y modelo base de dominio
3. Autenticación con Spring Security + JWT
4. Módulo de productos y categorías
5. Módulo de sucursales
6. Stock por sucursal y movimientos
7. Compras
8. Ventas
9. Dashboard e indicadores
10. Hardening, testing y despliegue

## Cómo ejecutar más adelante

### Base de datos

```bash
docker compose up -d
```

### Backend

```bash
cd backend
mvn test
```

La aplicación usa variables opcionales `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Si no existen, toma valores locales por defecto para PostgreSQL.

## Documentación por etapas

- [Etapa 1 - Base del proyecto](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-01-base-arquitectura.md)
- [Etapa 2 - Modelo y persistencia](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-02-modelo-persistencia.md)
- [Etapa 3 - Autenticacion JWT](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-03-autenticacion-jwt.md)
- [Etapa 4 - Productos y categorias](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-04-productos-categorias.md)
- [Etapa 5 - Sucursales](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-05-sucursales.md)
- [Etapa 6 - Stock y movimientos](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-06-stock-movimientos.md)
- [Etapa 7 - Compras](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-07-compras.md)
- [Etapa 8 - Ventas](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-08-ventas.md)
- [Etapa 9 - Dashboard e indicadores](C:/Users/sebag/Documents/MultiSucursal/docs/etapa-09-dashboard-indicadores.md)

### Frontend

```bash
cd frontend
npm install
npm run dev
```
