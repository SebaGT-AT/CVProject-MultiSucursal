import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../lib/api";
import { useAuth } from "../auth/AuthContext";
import { formatCurrency, formatLongDate } from "../lib/formatters";

const emptySaleForm = {
  saleNumber: "",
  customerName: "",
  saleDate: new Date().toISOString().slice(0, 10),
  branchId: "",
  notes: "",
  items: [
    {
      productId: "",
      quantity: "",
      unitSalePrice: ""
    }
  ]
};

function createEmptyItem() {
  return {
    productId: "",
    quantity: "",
    unitSalePrice: ""
  };
}

function SalesPage() {
  const { token, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [sales, setSales] = useState([]);
  const [branches, setBranches] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [screenError, setScreenError] = useState("");
  const [formError, setFormError] = useState("");
  const [selectedBranchFilter, setSelectedBranchFilter] = useState("all");
  const [saleSearch, setSaleSearch] = useState("");
  const [expandedSaleId, setExpandedSaleId] = useState(null);
  const [saleForm, setSaleForm] = useState(emptySaleForm);

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token]);

  async function loadData() {
    setIsLoading(true);
    setScreenError("");

    try {
      const [salesResponse, branchesResponse, productsResponse] =
        await Promise.all([
          authorizedRequest("/api/sales"),
          authorizedRequest("/api/branches"),
          authorizedRequest("/api/products")
        ]);

      setSales(salesResponse);
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
    setSaleForm({
      ...emptySaleForm,
      saleDate: new Date().toISOString().slice(0, 10)
    });
    setFormError("");
  }

  function handleSaleInputChange(event) {
    const { name, value } = event.target;
    setSaleForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  function handleItemChange(index, field, value) {
    setSaleForm((current) => ({
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
    setSaleForm((current) => ({
      ...current,
      items: [...current.items, createEmptyItem()]
    }));
  }

  function removeItemRow(index) {
    setSaleForm((current) => ({
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
      saleNumber: saleForm.saleNumber.trim(),
      customerName: saleForm.customerName.trim(),
      saleDate: saleForm.saleDate,
      branchId: Number(saleForm.branchId),
      notes: saleForm.notes.trim(),
      items: saleForm.items.map((item) => ({
        productId: Number(item.productId),
        quantity: Number(item.quantity),
        unitSalePrice: Number(item.unitSalePrice)
      }))
    };

    try {
      const createdSale = await authorizedRequest("/api/sales", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      resetForm();
      setExpandedSaleId(createdSale.id);
      await loadData();
    } catch (saveError) {
      setFormError(saveError.message);
    } finally {
      setIsSaving(false);
    }
  }

  const filteredSales = useMemo(() => {
    const normalizedSearch = saleSearch.trim().toLowerCase();

    return sales.filter((sale) => {
      const matchesSearch =
        normalizedSearch.length === 0 ||
        sale.saleNumber.toLowerCase().includes(normalizedSearch) ||
        sale.customerName.toLowerCase().includes(normalizedSearch) ||
        sale.branchName.toLowerCase().includes(normalizedSearch) ||
        sale.branchCode.toLowerCase().includes(normalizedSearch);

      const matchesBranch =
        selectedBranchFilter === "all" ||
        String(sale.branchId) === selectedBranchFilter;

      return matchesSearch && matchesBranch;
    });
  }, [sales, saleSearch, selectedBranchFilter]);

  const totalSalesAmount = sales.reduce(
    (accumulator, sale) => accumulator + Number(sale.totalAmount ?? 0),
    0
  );

  const totalItemsInForm = saleForm.items.reduce(
    (accumulator, item) => accumulator + Number(item.quantity || 0),
    0
  );

  const estimatedFormTotal = saleForm.items.reduce(
    (accumulator, item) =>
      accumulator + Number(item.quantity || 0) * Number(item.unitSalePrice || 0),
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
              <h2 className="h4 mb-1">Ventas</h2>
              <p className="text-secondary mb-0">
                Registra ventas por sucursal y deja que el backend descuente el
                stock automaticamente por cada item vendido.
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
        <section className="metric-card metric-sales p-4 h-100">
          <span className="metric-label">Ventas registradas</span>
          <strong className="metric-value">
            {isLoading ? "--" : sales.length}
          </strong>
          <p className="metric-subtitle mb-0">
            Operaciones comerciales almacenadas en el sistema
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-purchases p-4 h-100">
          <span className="metric-label">Monto acumulado</span>
          <strong className="metric-value">
            {isLoading ? "--" : formatCurrency(totalSalesAmount)}
          </strong>
          <p className="metric-subtitle mb-0">
            Suma total de ventas registradas en el historial
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-stock p-4 h-100">
          <span className="metric-label">Items del formulario</span>
          <strong className="metric-value">{totalItemsInForm}</strong>
          <p className="metric-subtitle mb-0">
            Total de unidades preparadas en la venta actual
          </p>
        </section>
      </div>

      <div className="col-12">
        <section className="content-card p-4">
          <div className="filters-row purchases-filters">
            <input
              type="search"
              className="form-control"
              placeholder="Buscar por numero, cliente o sucursal"
              value={saleSearch}
              onChange={(event) => setSaleSearch(event.target.value)}
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
              <h3 className="h5 mb-1">Historial de ventas</h3>
              <p className="text-secondary mb-0">
                {isLoading
                  ? "Cargando ventas..."
                  : `${filteredSales.length} ventas visibles de ${sales.length} registradas.`}
              </p>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando ventas...</div>
          ) : filteredSales.length === 0 ? (
            <div className="empty-state">
              <strong>No hay ventas para mostrar.</strong>
              <span>Registra una venta nueva o ajusta los filtros.</span>
            </div>
          ) : (
            <div className="purchase-list">
              {filteredSales.map((sale) => {
                const isExpanded = expandedSaleId === sale.id;

                return (
                  <article className="purchase-card" key={sale.id}>
                    <div className="d-flex flex-column flex-lg-row justify-content-between gap-3">
                      <div className="d-grid gap-2">
                        <div className="d-flex flex-wrap align-items-center gap-2">
                          <span className="table-chip">{sale.saleNumber}</span>
                          <strong className="h5 mb-0">{sale.customerName}</strong>
                        </div>

                        <div className="purchase-meta-grid">
                          <div>
                            <span className="branch-meta-label">Sucursal</span>
                            <strong>
                              {sale.branchCode} - {sale.branchName}
                            </strong>
                          </div>
                          <div>
                            <span className="branch-meta-label">Fecha</span>
                            <strong>{formatLongDate(sale.saleDate)}</strong>
                          </div>
                          <div>
                            <span className="branch-meta-label">Total</span>
                            <strong>{formatCurrency(sale.totalAmount)}</strong>
                          </div>
                        </div>

                        <div className="text-secondary small">
                          {sale.notes || "Sin notas registradas"}
                        </div>
                      </div>

                      <div className="d-inline-flex align-self-start">
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-dark"
                          onClick={() =>
                            setExpandedSaleId((current) =>
                              current === sale.id ? null : sale.id
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
                            {sale.items.map((item) => (
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
                                <td>{formatCurrency(item.unitSalePrice)}</td>
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
              <h3 className="h5 mb-1">Nueva venta</h3>
              <p className="text-secondary mb-0">
                {isAdmin
                  ? "Carga encabezado e items para registrar una venta y descontar stock."
                  : "Tu rol puede consultar ventas, pero no registrarlas."}
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
                <label className="form-label">Numero venta</label>
                <input
                  className="form-control"
                  name="saleNumber"
                  value={saleForm.saleNumber}
                  onChange={handleSaleInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div className="col-sm-6">
                <label className="form-label">Fecha</label>
                <input
                  type="date"
                  className="form-control"
                  name="saleDate"
                  value={saleForm.saleDate}
                  onChange={handleSaleInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>
            </div>

            <div>
              <label className="form-label">Cliente</label>
              <input
                className="form-control"
                name="customerName"
                value={saleForm.customerName}
                onChange={handleSaleInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Sucursal</label>
              <select
                className="form-select"
                name="branchId"
                value={saleForm.branchId}
                onChange={handleSaleInputChange}
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
                {saleForm.items.map((item, index) => (
                  <article className="purchase-item-card" key={`sale-item-${index}`}>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <strong>Item {index + 1}</strong>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => removeItemRow(index)}
                        disabled={!isAdmin || saleForm.items.length === 1}
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
                            value={item.unitSalePrice}
                            onChange={(event) =>
                              handleItemChange(index, "unitSalePrice", event.target.value)
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
                value={saleForm.notes}
                onChange={handleSaleInputChange}
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
              {isSaving ? "Guardando..." : "Registrar venta"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}

export default SalesPage;
