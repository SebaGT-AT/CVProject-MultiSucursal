import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

function AppNavbar() {
  const navigate = useNavigate();
  const { isAuthenticated, logout, user } = useAuth();

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
