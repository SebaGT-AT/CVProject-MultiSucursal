import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { getModuleByPath } from "../lib/modules";

function AppNavbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, logout, user } = useAuth();
  const currentModule = getModuleByPath(location.pathname);

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <nav className="navbar navbar-expand-lg border-bottom bg-white app-navbar">
      <div className="container">
        <Link className="navbar-brand fw-bold brand-mark" to={isAuthenticated ? "/app/dashboard" : "/login"}>
          MultiSucursal
        </Link>

        <div className="d-flex align-items-center gap-3">
          {isAuthenticated ? (
            <>
              <div className="navbar-module-chip">
                {currentModule.label}
              </div>
              <div className="text-end">
                <div className="navbar-user">{user?.name}</div>
                <div className="navbar-role">{user?.role}</div>
              </div>
              <button className="btn btn-sm btn-outline-dark" onClick={handleLogout} type="button">
                Salir
              </button>
            </>
          ) : (
            <span className="navbar-text text-secondary">
              Login y rutas protegidas
            </span>
          )}
        </div>
      </div>
    </nav>
  );
}

export default AppNavbar;
