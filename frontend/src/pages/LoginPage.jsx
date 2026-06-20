import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated, user } = useAuth();
  const [form, setForm] = useState({
    email: "admin@multisucursal.dev",
    password: ""
  });
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const redirectTo = location.state?.from?.pathname ?? "/app/dashboard";

  useEffect(() => {
    if (isAuthenticated) {
      navigate(redirectTo, { replace: true });
    }
  }, [isAuthenticated, navigate, redirectTo]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      await login(form);
      navigate(redirectTo, { replace: true });
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  return (
    <div className="auth-page">
      <section className="auth-hero p-4 p-lg-5">
        <span className="badge text-bg-warning text-dark mb-3">
          Portfolio Backend Java Junior
        </span>
        <h1 className="display-6 fw-bold mb-3">
          Inicia sesion para administrar MultiSucursal
        </h1>
        <p className="lead text-secondary mb-4">
          Esta etapa conecta el frontend con el backend real para manejar JWT,
          proteger rutas y preparar el sistema para operar con dashboard y
          modulos funcionales.
        </p>

        <div className="auth-points">
          <div className="auth-point">
            <strong>JWT real</strong>
            <span>La sesion se guarda localmente y habilita rutas privadas.</span>
          </div>
          <div className="auth-point">
            <strong>Roles visibles</strong>
            <span>El layout ya muestra el rol autenticado para futuros permisos.</span>
          </div>
          <div className="auth-point">
            <strong>Base escalable</strong>
            <span>La estructura queda lista para Dashboard, Productos y Stock.</span>
          </div>
        </div>
      </section>

      <section className="auth-card p-4 p-lg-5">
        <div className="mb-4">
          <h2 className="h4 mb-2">Acceso</h2>
          <p className="text-secondary mb-0">
            Usa el administrador bootstrap configurado en el backend para
            probar el flujo end to end.
          </p>
        </div>

        <form className="d-grid gap-3" onSubmit={handleSubmit}>
          <div>
            <label className="form-label" htmlFor="email">
              Email
            </label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control form-control-lg"
              value={form.email}
              onChange={handleChange}
              autoComplete="email"
              required
            />
          </div>

          <div>
            <label className="form-label" htmlFor="password">
              Contrasena
            </label>
            <input
              id="password"
              name="password"
              type="password"
              className="form-control form-control-lg"
              value={form.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />
          </div>

          {error ? (
            <div className="alert alert-danger mb-0" role="alert">
              {error}
            </div>
          ) : null}

          <button
            type="submit"
            className="btn btn-lg btn-auth"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Ingresando..." : "Ingresar al sistema"}
          </button>
        </form>

        <div className="auth-tip mt-4">
          <span className="auth-tip-label">Usuario sugerido</span>
          <strong>{user?.email ?? "admin@multisucursal.dev"}</strong>
        </div>
      </section>
    </div>
  );
}

export default LoginPage;
