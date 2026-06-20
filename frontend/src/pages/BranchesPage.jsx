import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../lib/api";
import { useAuth } from "../auth/AuthContext";

const emptyBranchForm = {
  code: "",
  name: "",
  city: "",
  address: "",
  managerName: "",
  phone: "",
  email: "",
  active: true
};

function BranchesPage() {
  const { token, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [branches, setBranches] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [screenError, setScreenError] = useState("");
  const [formError, setFormError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [editingBranchId, setEditingBranchId] = useState(null);
  const [branchForm, setBranchForm] = useState(emptyBranchForm);

  useEffect(() => {
    if (token) {
      loadBranches();
    }
  }, [token]);

  async function loadBranches() {
    setIsLoading(true);
    setScreenError("");

    try {
      const response = await authorizedRequest("/api/branches");
      setBranches(response);
    } catch (loadError) {
      setScreenError(loadError.message);
    } finally {
      setIsLoading(false);
    }
  }

  function authorizedRequest(path, options = {}) {
    return apiRequest(path, {
      ...options,
      headers: {
        Authorization: token,
        ...(options.headers ?? {})
      }
    });
  }

  function resetForm() {
    setEditingBranchId(null);
    setBranchForm(emptyBranchForm);
    setFormError("");
  }

  function handleInputChange(event) {
    const { name, value, type, checked } = event.target;
    setBranchForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (!isAdmin) {
      return;
    }

    setFormError("");
    setIsSaving(true);

    const payload = {
      code: branchForm.code.trim(),
      name: branchForm.name.trim(),
      city: branchForm.city.trim(),
      address: branchForm.address.trim(),
      managerName: branchForm.managerName.trim(),
      phone: branchForm.phone.trim(),
      email: branchForm.email.trim(),
      active: branchForm.active
    };

    try {
      if (editingBranchId) {
        await authorizedRequest(`/api/branches/${editingBranchId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await authorizedRequest("/api/branches", {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      resetForm();
      await loadBranches();
    } catch (saveError) {
      setFormError(saveError.message);
    } finally {
      setIsSaving(false);
    }
  }

  function startEditBranch(branch) {
    setEditingBranchId(branch.id);
    setFormError("");
    setBranchForm({
      code: branch.code,
      name: branch.name,
      city: branch.city,
      address: branch.address,
      managerName: branch.managerName ?? "",
      phone: branch.phone ?? "",
      email: branch.email ?? "",
      active: branch.active
    });
  }

  async function handleDeleteBranch(branch) {
    if (!isAdmin) {
      return;
    }

    const confirmed = window.confirm(
      `Eliminar la sucursal ${branch.name} (${branch.code})?`
    );
    if (!confirmed) {
      return;
    }

    try {
      await authorizedRequest(`/api/branches/${branch.id}`, {
        method: "DELETE"
      });

      if (editingBranchId === branch.id) {
        resetForm();
      }

      await loadBranches();
    } catch (deleteError) {
      setFormError(deleteError.message);
    }
  }

  const filteredBranches = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return branches.filter((branch) => {
      const matchesSearch =
        normalizedSearch.length === 0 ||
        branch.name.toLowerCase().includes(normalizedSearch) ||
        branch.code.toLowerCase().includes(normalizedSearch) ||
        branch.city.toLowerCase().includes(normalizedSearch) ||
        branch.address.toLowerCase().includes(normalizedSearch);

      const matchesStatus =
        statusFilter === "all" ||
        (statusFilter === "active" && branch.active) ||
        (statusFilter === "inactive" && !branch.active);

      return matchesSearch && matchesStatus;
    });
  }, [branches, search, statusFilter]);

  return (
    <div className="row g-4">
      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Modulo operativo
              </span>
              <h2 className="h4 mb-1">Sucursales</h2>
              <p className="text-secondary mb-0">
                Administra las sedes del negocio y deja lista la base para stock,
                compras y ventas por ubicacion.
              </p>
            </div>

            <div className="identity-pill">
              <span>{isAdmin ? "Edicion habilitada" : "Modo solo lectura"}</span>
            </div>
          </div>
        </section>
      </div>

      {screenError ? (
        <div className="col-12">
          <div className="alert alert-danger mb-0" role="alert">
            {screenError}
          </div>
        </div>
      ) : null}

      <div className="col-xl-8">
        <section className="content-card p-4 h-100">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4">
            <div>
              <h3 className="h5 mb-1">Red de sucursales</h3>
              <p className="text-secondary mb-0">
                {isLoading
                  ? "Cargando sucursales..."
                  : `${filteredBranches.length} sucursales visibles de ${branches.length} registradas.`}
              </p>
            </div>

            <div className="filters-row">
              <input
                type="search"
                className="form-control"
                placeholder="Buscar por codigo, nombre, ciudad o direccion"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />

              <select
                className="form-select"
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value)}
              >
                <option value="all">Todos los estados</option>
                <option value="active">Activas</option>
                <option value="inactive">Inactivas</option>
              </select>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando sucursales...</div>
          ) : filteredBranches.length === 0 ? (
            <div className="empty-state">
              <strong>No hay sucursales para mostrar.</strong>
              <span>Ajusta los filtros o registra una sucursal nueva.</span>
            </div>
          ) : (
            <div className="d-grid gap-3">
              {filteredBranches.map((branch) => (
                <article className="branch-card" key={branch.id}>
                  <div className="d-flex flex-column flex-lg-row justify-content-between gap-3">
                    <div className="d-grid gap-2">
                      <div className="d-flex flex-wrap align-items-center gap-2">
                        <span className="table-chip">{branch.code}</span>
                        <strong className="h5 mb-0">{branch.name}</strong>
                        <span
                          className={`status-pill ${branch.active ? "active" : "inactive"}`}
                        >
                          {branch.active ? "Activa" : "Inactiva"}
                        </span>
                      </div>

                      <div className="branch-meta-grid">
                        <div>
                          <span className="branch-meta-label">Ciudad</span>
                          <strong>{branch.city}</strong>
                        </div>
                        <div>
                          <span className="branch-meta-label">Direccion</span>
                          <strong>{branch.address}</strong>
                        </div>
                        <div>
                          <span className="branch-meta-label">Encargado</span>
                          <strong>{branch.managerName || "Sin asignar"}</strong>
                        </div>
                      </div>

                      <div className="branch-contact">
                        <span>{branch.phone || "Sin telefono"}</span>
                        <span>{branch.email || "Sin email"}</span>
                      </div>
                    </div>

                    <div className="d-inline-flex gap-2 align-self-start">
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-dark"
                        onClick={() => startEditBranch(branch)}
                        disabled={!isAdmin}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => handleDeleteBranch(branch)}
                        disabled={!isAdmin}
                      >
                        Eliminar
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>

      <div className="col-xl-4">
        <section className="content-card p-4">
          <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
            <div>
              <h3 className="h5 mb-1">
                {editingBranchId ? "Editar sucursal" : "Nueva sucursal"}
              </h3>
              <p className="text-secondary mb-0">
                {isAdmin
                  ? "Completa los datos operativos basicos de cada sede."
                  : "Tu rol puede consultar sucursales, pero no modificarlas."}
              </p>
            </div>
            {editingBranchId ? (
              <button
                type="button"
                className="btn btn-sm btn-outline-secondary"
                onClick={resetForm}
              >
                Cancelar
              </button>
            ) : null}
          </div>

          <form className="d-grid gap-3" onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-sm-5">
                <label className="form-label">Codigo</label>
                <input
                  className="form-control"
                  name="code"
                  value={branchForm.code}
                  onChange={handleInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div className="col-sm-7">
                <label className="form-label">Nombre</label>
                <input
                  className="form-control"
                  name="name"
                  value={branchForm.name}
                  onChange={handleInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>
            </div>

            <div>
              <label className="form-label">Ciudad</label>
              <input
                className="form-control"
                name="city"
                value={branchForm.city}
                onChange={handleInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Direccion</label>
              <input
                className="form-control"
                name="address"
                value={branchForm.address}
                onChange={handleInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Encargado</label>
              <input
                className="form-control"
                name="managerName"
                value={branchForm.managerName}
                onChange={handleInputChange}
                disabled={!isAdmin}
              />
            </div>

            <div className="row g-3">
              <div className="col-sm-6">
                <label className="form-label">Telefono</label>
                <input
                  className="form-control"
                  name="phone"
                  value={branchForm.phone}
                  onChange={handleInputChange}
                  disabled={!isAdmin}
                />
              </div>

              <div className="col-sm-6">
                <label className="form-label">Email</label>
                <input
                  type="email"
                  className="form-control"
                  name="email"
                  value={branchForm.email}
                  onChange={handleInputChange}
                  disabled={!isAdmin}
                />
              </div>
            </div>

            <div className="form-check">
              <input
                id="branch-active"
                type="checkbox"
                className="form-check-input"
                name="active"
                checked={branchForm.active}
                onChange={handleInputChange}
                disabled={!isAdmin}
              />
              <label className="form-check-label" htmlFor="branch-active">
                Sucursal activa
              </label>
            </div>

            {formError ? (
              <div className="alert alert-danger mb-0" role="alert">
                {formError}
              </div>
            ) : null}

            <button
              type="submit"
              className="btn btn-auth"
              disabled={!isAdmin || isSaving}
            >
              {isSaving
                ? "Guardando..."
                : editingBranchId
                  ? "Actualizar sucursal"
                  : "Crear sucursal"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}

export default BranchesPage;
