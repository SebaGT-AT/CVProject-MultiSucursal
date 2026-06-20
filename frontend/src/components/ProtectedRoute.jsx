import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

function ProtectedRoute() {
  const { isAuthenticated, isBootstrapped } = useAuth();
  const location = useLocation();

  if (!isBootstrapped) {
    return (
      <div className="auth-loading">
        <div className="spinner-border text-warning" role="status" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }

  return <Outlet />;
}

export default ProtectedRoute;
