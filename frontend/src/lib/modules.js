export const appModules = [
  {
    to: "/app/dashboard",
    label: "Dashboard",
    title: "Vision operativa",
    description:
      "Resumen ejecutivo con indicadores, estado de sesion y alertas de inventario."
  },
  {
    to: "/app/productos",
    label: "Productos",
    title: "Catalogo comercial",
    description:
      "Gestion del catalogo de productos y categorias con permisos por rol."
  },
  {
    to: "/app/sucursales",
    label: "Sucursales",
    title: "Red de sucursales",
    description:
      "Administracion de sedes para preparar operaciones de stock, compras y ventas."
  },
  {
    to: "/app/stock",
    label: "Stock",
    title: "Inventario y movimientos",
    description:
      "Consulta de stock, alertas criticas y operaciones de ajuste o transferencia."
  },
  {
    to: "/app/compras",
    label: "Compras",
    title: "Abastecimiento",
    description:
      "Registro de compras con multiples items y actualizacion automatica del inventario."
  },
  {
    to: "/app/ventas",
    label: "Ventas",
    title: "Salida comercial",
    description:
      "Registro de ventas por sucursal con descuento automatico de stock."
  }
];

export function getModuleByPath(pathname) {
  return appModules.find((module) => pathname.startsWith(module.to)) ?? appModules[0];
}
