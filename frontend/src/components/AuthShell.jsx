import { NavLink, Outlet, useLocation } from "react-router-dom";
import AppNavbar from "./AppNavbar";
import { appModules, getModuleByPath } from "../lib/modules";

function AuthShell() {
  const location = useLocation();
  const currentModule = getModuleByPath(location.pathname);

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
              <h1 className="h3 mb-1">{currentModule.title}</h1>
              <p className="text-secondary mb-0">
                {currentModule.description}
              </p>
            </div>

            <nav className="section-tabs">
              {appModules.map((module) => (
                <NavLink
                  key={module.to}
                  to={module.to}
                  className={({ isActive }) =>
                    `section-tab${isActive ? " active" : ""}`
                  }
                >
                  {module.label}
                </NavLink>
              ))}
            </nav>
          </div>
        </section>

        <section className="module-summary-strip mb-4">
          <div className="module-summary-card">
            <span className="session-label">Modulo actual</span>
            <strong>{currentModule.label}</strong>
          </div>
          <div className="module-summary-card">
            <span className="session-label">Objetivo</span>
            <strong>{currentModule.title}</strong>
          </div>
          <div className="module-summary-card">
            <span className="session-label">Cobertura</span>
            <strong>Frontend conectado al backend</strong>
          </div>
        </section>

        <Outlet />
      </main>
    </div>
  );
}

export default AuthShell;
