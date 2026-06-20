import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../lib/api";
import { useAuth } from "../auth/AuthContext";
import { formatCurrency, formatLongDate } from "../lib/formatters";

const emptyPurchaseForm = {
  purchaseNumber: "",
  supplierName: "",
  purchaseDate: new Date().toISOString().slice(0, 10),
  branchId: "",
  notes: "",
  items: [
    {
      productId: "",
      quantity: "",
      unitPurchasePrice: ""
    }
  ]
};

function createEmptyItem() {
  return {
    productId: "",
    quantity: "",
    unitPurchasePrice: ""
  };
}

function PurchasesPage() {
  const { token, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [purchases, setPurchases] = useState([]);
  const [branches, setBranches] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [screenError, setScreenError] = useState("");
  const [formError, setFormError] = useState("");
  const [selectedBranchFilter, setSelectedBranchFilter] = useState("all");
  const [purchaseSearch, setPurchaseSearch] = useState("");
  const [expandedPurchaseId, setExpandedPurchaseId] = useState(null);
  const [purchaseForm, setPurchaseForm] = useState(emptyPurchaseForm);

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token]);

  async function loadData() {
    setIsLoading(true);
    setScreenError("");

    try {
      const [purchasesResponse, branchesResponse, productsResponse] =
        await Promise.all([
          authorizedRequest("/api/purchases"),
          authorizedRequest("/api/branches"),
          authorizedRequest("/api/products")
        ]);

      setPurchases(purchasesResponse);
      setBranches(branchesResponse);
      setProducts(productsResponse);
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
    setPurchaseForm({
      ...emptyPurchaseForm,
      purchaseDate: new Date().toISOString().slice(0, 10)
    });
    setFormError("");
  }

  function handlePurchaseInputChange(event) {
    const { name, value } = event.target;
    setPurchaseForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  function handleItemChange(index, field, value) {
    setPurchaseForm((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              [field]: value
            }
          : item
      )
    }));
  }

  function addItemRow() {
    setPurchaseForm((current) => ({
      ...current,
      items: [...current.items, createEmptyItem()]
    }));
  }

  function removeItemRow(index) {
    setPurchaseForm((current) => ({
      ...current,
      items:
        current.items.length === 1
          ? current.items
          : current.items.filter((_, itemIndex) => itemIndex !== index)
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
      purchaseNumber: purchaseForm.purchaseNumber.trim(),
      supplierName: purchaseForm.supplierName.trim(),
      purchaseDate: purchaseForm.purchaseDate,
      branchId: Number(purchaseForm.branchId),
      notes: purchaseForm.notes.trim(),
      items: purchaseForm.items.map((item) => ({
        productId: Number(item.productId),
        quantity: Number(item.quantity),
        unitPurchasePrice: Number(item.unitPurchasePrice)
      }))
    };

    try {
      const createdPurchase = await authorizedRequest("/api/purchases", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      resetForm();
      setExpandedPurchaseId(createdPurchase.id);
      await loadData();
    } catch (saveError) {
      setFormError(saveError.message);
    } finally {
      setIsSaving(false);
    }
  }

  const filteredPurchases = useMemo(() => {
    const normalizedSearch = purchaseSearch.trim().toLowerCase();

    return purchases.filter((purchase) => {
      const matchesSearch =
        normalizedSearch.length === 0 ||
        purchase.purchaseNumber.toLowerCase().includes(normalizedSearch) ||
        purchase.supplierName.toLowerCase().includes(normalizedSearch) ||
        purchase.branchName.toLowerCase().includes(normalizedSearch) ||
        purchase.branchCode.toLowerCase().includes(normalizedSearch);

      const matchesBranch =
        selectedBranchFilter === "all" ||
        String(purchase.branchId) === selectedBranchFilter;

      return matchesSearch && matchesBranch;
    });
  }, [purchases, purchaseSearch, selectedBranchFilter]);

  const totalPurchasesAmount = purchases.reduce(
    (accumulator, purchase) => accumulator + Number(purchase.totalAmount ?? 0),
    0
  );

  const totalItemsInForm = purchaseForm.items.reduce(
    (accumulator, item) => accumulator + Number(item.quantity || 0),
    0
  );

  const estimatedFormTotal = purchaseForm.items.reduce(
    (accumulator, item) =>
      accumulator +
      Number(item.quantity || 0) * Number(item.unitPurchasePrice || 0),
    0
  );

  return (
    <div className="row g-4">
      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Modulo operativo
              </span>
              <h2 className="h4 mb-1">Compras</h2>
              <p className="text-secondary mb-0">
                Registra compras por sucursal y deja que el backend actualice el
                stock automaticamente por cada item.
              </p>
            </div>

            <div className="identity-pill">
              <span>{isAdmin ? "Registro habilitado" : "Consulta operativa"}</span>
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

      <div className="col-md-4">
        <section className="metric-card metric-purchases p-4 h-100">
          <span className="metric-label">Compras registradas</span>
          <strong className="metric-value">
            {isLoading ? "--" : purchases.length}
          </strong>
          <p className="metric-subtitle mb-0">
            Operaciones de abastecimiento cargadas en el sistema
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-sales p-4 h-100">
          <span className="metric-label">Monto acumulado</span>
          <strong className="metric-value">
            {isLoading ? "--" : formatCurrency(totalPurchasesAmount)}
          </strong>
          <p className="metric-subtitle mb-0">
            Suma total de compras registradas en el historial
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-stock p-4 h-100">
          <span className="metric-label">Items del formulario</span>
          <strong className="metric-value">{totalItemsInForm}</strong>
          <p className="metric-subtitle mb-0">
            Total de unidades preparadas en la compra actual
          </p>
        </section>
      </div>

      <div className="col-12">
        <section className="content-card p-4">
          <div className="filters-row purchases-filters">
            <input
              type="search"
              className="form-control"
              placeholder="Buscar por numero, proveedor o sucursal"
              value={purchaseSearch}
              onChange={(event) => setPurchaseSearch(event.target.value)}
            />

            <select
              className="form-select"
              value={selectedBranchFilter}
              onChange={(event) => setSelectedBranchFilter(event.target.value)}
            >
              <option value="all">Todas las sucursales</option>
              {branches.map((branch) => (
                <option key={branch.id} value={String(branch.id)}>
                  {branch.code} - {branch.name}
                </option>
              ))}
            </select>
          </div>
        </section>
      </div>

      <div className="col-xl-7">
        <section className="content-card p-4 h-100">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4">
            <div>
              <h3 className="h5 mb-1">Historial de compras</h3>
              <p className="text-secondary mb-0">
                {isLoading
                  ? "Cargando compras..."
                  : `${filteredPurchases.length} compras visibles de ${purchases.length} registradas.`}
              </p>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando compras...</div>
          ) : filteredPurchases.length === 0 ? (
            <div className="empty-state">
              <strong>No hay compras para mostrar.</strong>
              <span>Registra una compra nueva o ajusta los filtros.</span>
            </div>
          ) : (
            <div className="purchase-list">
              {filteredPurchases.map((purchase) => {
                const isExpanded = expandedPurchaseId === purchase.id;

                return (
                  <article className="purchase-card" key={purchase.id}>
                    <div className="d-flex flex-column flex-lg-row justify-content-between gap-3">
                      <div className="d-grid gap-2">
                        <div className="d-flex flex-wrap align-items-center gap-2">
                          <span className="table-chip">{purchase.purchaseNumber}</span>
                          <strong className="h5 mb-0">{purchase.supplierName}</strong>
                        </div>

                        <div className="purchase-meta-grid">
                          <div>
                            <span className="branch-meta-label">Sucursal</span>
                            <strong>
                              {purchase.branchCode} - {purchase.branchName}
                            </strong>
                          </div>
                          <div>
                            <span className="branch-meta-label">Fecha</span>
                            <strong>{formatLongDate(purchase.purchaseDate)}</strong>
                          </div>
                          <div>
                            <span className="branch-meta-label">Total</span>
                            <strong>{formatCurrency(purchase.totalAmount)}</strong>
                          </div>
                        </div>

                        <div className="text-secondary small">
                          {purchase.notes || "Sin notas registradas"}
                        </div>
                      </div>

                      <div className="d-inline-flex align-self-start">
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-dark"
                          onClick={() =>
                            setExpandedPurchaseId((current) =>
                              current === purchase.id ? null : purchase.id
                            )
                          }
                        >
                          {isExpanded ? "Ocultar detalle" : "Ver detalle"}
                        </button>
                      </div>
                    </div>

                    {isExpanded ? (
                      <div className="table-responsive mt-3">
                        <table className="table align-middle dashboard-table mb-0">
                          <thead>
                            <tr>
                              <th>Producto</th>
                              <th>Cantidad</th>
                              <th>Unitario</th>
                              <th>Subtotal</th>
                            </tr>
                          </thead>
                          <tbody>
                            {purchase.items.map((item) => (
                              <tr key={item.id}>
                                <td>
                                  <div className="d-grid">
                                    <strong>{item.productName}</strong>
                                    <span className="text-secondary small">
                                      {item.productSku}
                                    </span>
                                  </div>
                                </td>
                                <td>{item.quantity}</td>
                                <td>{formatCurrency(item.unitPurchasePrice)}</td>
                                <td>{formatCurrency(item.subtotal)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : null}
                  </article>
                );
              })}
            </div>
          )}
        </section>
      </div>

      <div className="col-xl-5">
        <section className="content-card p-4 h-100">
          <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
            <div>
              <h3 className="h5 mb-1">Nueva compra</h3>
              <p className="text-secondary mb-0">
                {isAdmin
                  ? "Carga encabezado e items para registrar abastecimiento y actualizar stock."
                  : "Tu rol puede consultar compras, pero no registrarlas."}
              </p>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={resetForm}
              disabled={!isAdmin}
            >
              Limpiar
            </button>
          </div>

          <form className="d-grid gap-3" onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-sm-6">
                <label className="form-label">Numero compra</label>
                <input
                  className="form-control"
                  name="purchaseNumber"
                  value={purchaseForm.purchaseNumber}
                  onChange={handlePurchaseInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div className="col-sm-6">
                <label className="form-label">Fecha</label>
                <input
                  type="date"
                  className="form-control"
                  name="purchaseDate"
                  value={purchaseForm.purchaseDate}
                  onChange={handlePurchaseInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>
            </div>

            <div>
              <label className="form-label">Proveedor</label>
              <input
                className="form-control"
                name="supplierName"
                value={purchaseForm.supplierName}
                onChange={handlePurchaseInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Sucursal</label>
              <select
                className="form-select"
                name="branchId"
                value={purchaseForm.branchId}
                onChange={handlePurchaseInputChange}
                disabled={!isAdmin}
                required
              >
                <option value="">Selecciona una sucursal</option>
                {branches.map((branch) => (
                  <option key={branch.id} value={String(branch.id)}>
                    {branch.code} - {branch.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <div className="d-flex justify-content-between align-items-center mb-2">
                <label className="form-label mb-0">Items</label>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-dark"
                  onClick={addItemRow}
                  disabled={!isAdmin}
                >
                  Agregar item
                </button>
              </div>

              <div className="purchase-items-grid">
                {purchaseForm.items.map((item, index) => (
                  <article className="purchase-item-card" key={`purchase-item-${index}`}>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <strong>Item {index + 1}</strong>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => removeItemRow(index)}
                        disabled={!isAdmin || purchaseForm.items.length === 1}
                      >
                        Quitar
                      </button>
                    </div>

                    <div className="d-grid gap-3">
                      <div>
                        <label className="form-label">Producto</label>
                        <select
                          className="form-select"
                          value={item.productId}
                          onChange={(event) =>
                            handleItemChange(index, "productId", event.target.value)
                          }
                          disabled={!isAdmin}
                          required
                        >
                          <option value="">Selecciona un producto</option>
                          {products.map((product) => (
                            <option key={product.id} value={String(product.id)}>
                              {product.sku} - {product.name}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="row g-3">
                        <div className="col-sm-4">
                          <label className="form-label">Cantidad</label>
                          <input
                            type="number"
                            min="1"
                            className="form-control"
                            value={item.quantity}
                            onChange={(event) =>
                              handleItemChange(index, "quantity", event.target.value)
                            }
                            disabled={!isAdmin}
                            required
                          />
                        </div>

                        <div className="col-sm-8">
                          <label className="form-label">Precio unitario</label>
                          <input
                            type="number"
                            min="0.01"
                            step="0.01"
                            className="form-control"
                            value={item.unitPurchasePrice}
                            onChange={(event) =>
                              handleItemChange(
                                index,
                                "unitPurchasePrice",
                                event.target.value
                              )
                            }
                            disabled={!isAdmin}
                            required
                          />
                        </div>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </div>

            <div>
              <label className="form-label">Notas</label>
              <textarea
                className="form-control"
                rows="3"
                name="notes"
                value={purchaseForm.notes}
                onChange={handlePurchaseInputChange}
                disabled={!isAdmin}
              />
            </div>

            <div className="purchase-summary">
              <span className="session-label">Total estimado</span>
              <strong>{formatCurrency(estimatedFormTotal)}</strong>
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
              {isSaving ? "Guardando..." : "Registrar compra"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}

export default PurchasesPage;
