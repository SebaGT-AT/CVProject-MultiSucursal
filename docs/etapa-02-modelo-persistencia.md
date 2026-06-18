# Etapa 2 - Modelo base de dominio y persistencia

## Objetivo

Definir el modelo inicial de datos del sistema y dejar configurada la persistencia JPA/Hibernate sobre PostgreSQL para que las siguientes etapas implementen reglas de negocio sobre una base estable.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory
├── branch
│   ├── entity
│   └── repository
├── common
│   └── entity
├── product
│   ├── entity
│   └── repository
└── stock
    ├── entity
    └── repository
```

## Modelo base

- `Category`: clasifica productos.
- `Product`: contiene SKU, nombre, precios y categoría.
- `Branch`: representa cada sucursal.
- `BranchStock`: mantiene stock por producto y sucursal.
- `StockMovement`: registra historial de movimientos.
- `MovementType`: enum para tipificar compras, ventas, transferencias y ajustes.
- `BaseEntity`: centraliza `id`, `createdAt` y `updatedAt`.

## Decisiones de diseño

- Se usó `BigDecimal` para precios para evitar errores de precisión.
- Se creó una tabla intermedia `branch_stocks` para soportar stock multi-sucursal de forma explícita.
- Se agregó historial de movimientos desde ahora para no rediseñar ventas, compras y transferencias más adelante.
- Se definieron restricciones únicas en `sku`, `category.name`, `branch.code` y `(branch_id, product_id)`.

## Buenas prácticas aplicadas

- Entidades enfocadas en responsabilidad única.
- Campos auditables comunes reutilizados desde una superclase.
- Repositorios pequeños y expresivos con queries derivadas por nombre.
- Tests de persistencia con H2 para validar mapeos sin depender de PostgreSQL real.
- Nombres de tablas y columnas pensados para lectura clara en base de datos.

## Commits sugeridos

- `feat: add base domain model for products branches and stock`
- `test: add repository integration test for inventory model`
- `docs: add stage 2 persistence and domain notes`

