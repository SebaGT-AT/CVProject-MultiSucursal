import { NavLink, Outlet } from "react-router-dom";
import AppNavbar from "./AppNavbar";

const sections = [
  { to: "/app/dashboard", label: "Dashboard" },
  { to: "/app/productos", label: "Productos" },
  { to: "/app/sucursales", label: "Sucursales" },
  { to: "/app/stock", label: "Stock" },
  { to: "/app/compras", label: "Compras" },
  { to: "/app/ventas", label: "Ventas" }
];

function AuthShell() {
  return (
    <div className="app-shell">
      <AppNavbar />

      <main className="container py-4 py-lg-5">
        <section className="workspace-panel p-3 p-lg-4 mb-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Frontend operativo
              </span>
              <h1 className="h3 mb-1">Panel de administracion</h1>
              <p className="text-secondary mb-0">
                Base lista para operar con JWT, dashboard y modulos
                funcionales conectados al backend.
              </p>
            </div>

            <nav className="section-tabs">
              {sections.map((section) => (
                <NavLink
                  key={section.to}
                  to={section.to}
                  className={({ isActive }) =>
                    `section-tab${isActive ? " active" : ""}`
                  }
                >
                  {section.label}
                </NavLink>
              ))}
            </nav>
          </div>
        </section>

        <Outlet />
      </main>
    </div>
  );
}

export default AuthShell;
