# Etapa 4 - CRUD de productos y categorias

## Objetivo

Implementar el modulo de productos y categorias con CRUD completo, validaciones de negocio y proteccion por roles para dejar operativa la primera parte funcional del inventario.

## Estructura

```text
backend/src/main/java/com/multisucursal/inventory/product
├── controller
├── dto
├── entity
├── repository
└── service
```

## Endpoints implementados

### Categorias

- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Productos

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

## Reglas aplicadas

- `ADMIN` puede crear, editar y eliminar.
- `ADMIN` y `EMPLEADO` pueden consultar.
- No se puede eliminar una categoria que tenga productos asociados.
- No se puede eliminar un producto con historial de stock.
- `sku` y `category.name` deben ser unicos.

## Buenas practicas aplicadas

- DTOs de request/response para no exponer entidades JPA directamente.
- Servicios con validaciones de negocio y control de integridad.
- Excepciones semanticas como `ResourceNotFoundException`.
- Proteccion por rol con `@PreAuthorize`.
- Tests de integracion con JWT real y flujo completo de CRUD.

## Commits sugeridos

- `feat: add category and product CRUD endpoints`
- `test: add product and category integration tests`
- `docs: add stage 4 product module notes`

