import { Link } from "react-router-dom";

function ModulePlaceholderPage({ title, description, nextStep }) {
  return (
    <section className="content-card p-4">
      <span className="badge text-bg-light border mb-3">{title}</span>
      <h2 className="h4 mb-3">Modulo en preparacion</h2>
      <p className="text-secondary mb-3">{description}</p>
      <p className="text-secondary mb-4">
        {nextStep}
      </p>
      <Link className="btn btn-outline-dark" to="/app/dashboard">
        Volver al panel
      </Link>
    </section>
  );
}

export default ModulePlaceholderPage;
