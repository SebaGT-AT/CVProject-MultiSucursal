import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { formatCurrency, formatLongDate } from "../lib/formatters";
import { apiRequest } from "../lib/api";
import { appModules } from "../lib/modules";

function DashboardHomePage() {
  const { token, user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadDashboard() {
      setIsLoading(true);
      setError("");

      try {
        const response = await apiRequest("/api/dashboard", {
          headers: {
            Authorization: token
          }
        });

        if (isMounted) {
          setDashboard(response);
        }
      } catch (requestError) {
        if (isMounted) {
          setError(requestError.message);
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (token) {
      loadDashboard();
    }

    return () => {
      isMounted = false;
    };
  }, [token]);

  const lowStockProducts = dashboard?.lowStockProducts ?? [];

  return (
    <div className="row g-4">
      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Dashboard conectado
              </span>
              <h2 className="h4 mb-1">Indicadores operativos</h2>
              <p className="text-secondary mb-0">
                Esta vista consume el backend real con JWT y muestra métricas del
                sistema para el usuario autenticado.
              </p>
            </div>

            <div className="identity-pill dashboard-date-pill">
              <span>{formatLongDate(dashboard?.generatedDate)}</span>
            </div>
          </div>
        </section>
      </div>

      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center mb-3">
            <div>
              <h3 className="h5 mb-1">Accesos rapidos</h3>
              <p className="text-secondary mb-0">
                Entrada directa a los modulos funcionales principales del sistema.
              </p>
            </div>
          </div>

          <div className="quick-links-grid">
            {appModules
              .filter((module) => module.to !== "/app/dashboard")
              .map((module) => (
                <Link className="quick-link-card" key={module.to} to={module.to}>
                  <span className="session-label">{module.label}</span>
                  <strong>{module.title}</strong>
                  <span className="text-secondary">{module.description}</span>
                </Link>
              ))}
          </div>
        </section>
      </div>

      <div className="col-lg-8">
        <section className="content-card p-4 h-100">
          <div className="d-flex flex-column flex-md-row justify-content-between gap-3 mb-4">
            <div>
              <h3 className="h5 mb-1">Sesion activa</h3>
              <p className="text-secondary mb-0">
                El contexto global mantiene la identidad y el token de acceso.
              </p>
            </div>

            <div className="identity-pill">
              <span>{user?.role}</span>
            </div>
          </div>

          <div className="session-grid">
            <article className="session-card">
              <span className="session-label">Nombre</span>
              <strong>{user?.name}</strong>
            </article>
            <article className="session-card">
              <span className="session-label">Email</span>
              <strong>{user?.email}</strong>
            </article>
            <article className="session-card">
              <span className="session-label">Estado</span>
              <strong>Autenticado</strong>
            </article>
          </div>
        </section>
      </div>

      <div className="col-lg-4">
        <section className="content-card p-4 h-100">
          <h3 className="h5 mb-3">Estado del dashboard</h3>
          {isLoading ? (
            <div className="d-flex align-items-center gap-2 text-secondary">
              <div className="spinner-border spinner-border-sm text-warning" role="status" />
              <span>Cargando indicadores...</span>
            </div>
          ) : error ? (
            <div className="alert alert-danger mb-0" role="alert">
              {error}
            </div>
          ) : (
            <p className="text-secondary mb-0">
              Datos cargados correctamente desde `/api/dashboard`.
            </p>
          )}
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-sales p-4 h-100">
          <span className="metric-label">Ventas del dia</span>
          <strong className="metric-value">
            {isLoading ? "--" : dashboard?.salesToday?.count ?? 0}
          </strong>
          <p className="metric-subtitle mb-0">
            Total: {isLoading ? "--" : formatCurrency(dashboard?.salesToday?.totalAmount)}
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-purchases p-4 h-100">
          <span className="metric-label">Compras del mes</span>
          <strong className="metric-value">
            {isLoading ? "--" : dashboard?.purchasesThisMonth?.count ?? 0}
          </strong>
          <p className="metric-subtitle mb-0">
            Total: {isLoading ? "--" : formatCurrency(dashboard?.purchasesThisMonth?.totalAmount)}
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-stock p-4 h-100">
          <span className="metric-label">Productos con bajo stock</span>
          <strong className="metric-value">
            {isLoading ? "--" : lowStockProducts.length}
          </strong>
          <p className="metric-subtitle mb-0">
            Sucursales criticas detectadas en tiempo real
          </p>
        </section>
      </div>

      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center mb-3">
            <div>
              <h3 className="h5 mb-1">Alerta de bajo stock</h3>
              <p className="text-secondary mb-0">
                Productos cuya cantidad actual ya alcanzó o quedó por debajo del mínimo configurado.
              </p>
            </div>
            <span className="text-secondary small">
              {isLoading ? "Sincronizando..." : `${lowStockProducts.length} registros`}
            </span>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Preparando tabla de indicadores...</div>
          ) : lowStockProducts.length === 0 ? (
            <div className="empty-state">
              <strong>No hay productos en bajo stock.</strong>
              <span>Cuando alguna sucursal llegue a su mínimo, aparecerá aquí.</span>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle dashboard-table mb-0">
                <thead>
                  <tr>
                    <th>SKU</th>
                    <th>Producto</th>
                    <th>Sucursal</th>
                    <th>Actual</th>
                    <th>Minimo</th>
                  </tr>
                </thead>
                <tbody>
                  {lowStockProducts.map((item) => (
                    <tr key={`${item.branchId}-${item.productId}`}>
                      <td>
                        <span className="table-chip">{item.productSku}</span>
                      </td>
                      <td>{item.productName}</td>
                      <td>
                        <div className="d-grid">
                          <strong>{item.branchName}</strong>
                          <span className="text-secondary small">{item.branchCode}</span>
                        </div>
                      </td>
                      <td>{item.quantity}</td>
                      <td>{item.minimumStock}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

export default DashboardHomePage;
