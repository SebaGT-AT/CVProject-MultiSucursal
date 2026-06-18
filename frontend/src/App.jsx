import AppNavbar from "./components/AppNavbar";

const cards = [
  {
    title: "Autenticacion",
    description: "Registro, login, JWT y roles ADMIN/EMPLEADO."
  },
  {
    title: "Inventario",
    description: "Productos, categorias, stock por sucursal y movimientos."
  },
  {
    title: "Operacion",
    description: "Compras, ventas, transferencias y dashboard ejecutivo."
  }
];

function App() {
  return (
    <div className="app-shell">
      <AppNavbar />

      <main className="container py-5">
        <section className="hero-panel p-4 p-lg-5 mb-4">
          <span className="badge text-bg-warning text-dark mb-3">
            Portfolio Backend Java Junior
          </span>
          <h1 className="display-5 fw-bold mb-3">
            Sistema de Inventario Multi-Sucursal
          </h1>
          <p className="lead text-secondary mb-4">
            Base inicial del proyecto construida con una arquitectura pensada
            para crecer por etapas y demostrar buenas practicas reales de
            backend con Spring Boot y frontend con React.
          </p>

          <div className="row g-3">
            {cards.map((card) => (
              <div className="col-md-4" key={card.title}>
                <article className="feature-card h-100 p-3">
                  <h2 className="h5 fw-semibold">{card.title}</h2>
                  <p className="mb-0 text-secondary">{card.description}</p>
                </article>
              </div>
            ))}
          </div>
        </section>

        <section className="row g-4">
          <div className="col-lg-7">
            <div className="content-card p-4 h-100">
              <h3 className="h4 mb-3">Objetivo de la etapa actual</h3>
              <p className="text-secondary mb-0">
                Dejar lista la base tecnica del monorepo para comenzar con el
                modelado del dominio, seguridad JWT y los modulos funcionales
                sin reestructuraciones posteriores.
              </p>
            </div>
          </div>

          <div className="col-lg-5">
            <div className="content-card p-4 h-100">
              <h3 className="h4 mb-3">Siguientes hitos</h3>
              <ul className="mb-0 text-secondary">
                <li>Modelo base y conexion PostgreSQL.</li>
                <li>Usuarios, roles y autenticacion JWT.</li>
                <li>CRUD de productos y categorias.</li>
              </ul>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;

