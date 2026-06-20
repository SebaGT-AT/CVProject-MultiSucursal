import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../lib/api";
import { useAuth } from "../auth/AuthContext";
import { formatDateTime } from "../lib/formatters";

const emptyAdjustmentForm = {
  branchId: "",
  productId: "",
  quantity: "",
  adjustmentType: "INCREASE",
  minimumStock: "",
  reference: "",
  notes: ""
};

const emptyTransferForm = {
  sourceBranchId: "",
  targetBranchId: "",
  productId: "",
  quantity: "",
  reference: "",
  notes: ""
};

const movementLabels = {
  INITIAL_LOAD: "Carga inicial",
  PURCHASE: "Compra",
  SALE: "Venta",
  TRANSFER_IN: "Transferencia entrada",
  TRANSFER_OUT: "Transferencia salida",
  ADJUSTMENT: "Ajuste"
};

function StockPage() {
  const { token, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [stocks, setStocks] = useState([]);
  const [movements, setMovements] = useState([]);
  const [branches, setBranches] = useState([]);
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingAdjustment, setIsSavingAdjustment] = useState(false);
  const [isSavingTransfer, setIsSavingTransfer] = useState(false);
  const [screenError, setScreenError] = useState("");
  const [adjustmentError, setAdjustmentError] = useState("");
  const [transferError, setTransferError] = useState("");
  const [selectedBranchFilter, setSelectedBranchFilter] = useState("all");
  const [selectedProductFilter, setSelectedProductFilter] = useState("all");
  const [movementTypeFilter, setMovementTypeFilter] = useState("all");
  const [stockSearch, setStockSearch] = useState("");
  const [showLowStockOnly, setShowLowStockOnly] = useState(false);
  const [adjustmentForm, setAdjustmentForm] = useState(emptyAdjustmentForm);
  const [transferForm, setTransferForm] = useState(emptyTransferForm);

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token]);

  async function loadData() {
    setIsLoading(true);
    setScreenError("");

    try {
      const [stocksResponse, movementsResponse, branchesResponse, productsResponse] =
        await Promise.all([
          authorizedRequest("/api/stocks"),
          authorizedRequest("/api/stocks/movements"),
          authorizedRequest("/api/branches"),
          authorizedRequest("/api/products")
        ]);

      setStocks(stocksResponse);
      setMovements(movementsResponse);
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

  function handleAdjustmentInputChange(event) {
    const { name, value } = event.target;
    setAdjustmentForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  function handleTransferInputChange(event) {
    const { name, value } = event.target;
    setTransferForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  function resetAdjustmentForm() {
    setAdjustmentForm(emptyAdjustmentForm);
    setAdjustmentError("");
  }

  function resetTransferForm() {
    setTransferForm(emptyTransferForm);
    setTransferError("");
  }

  async function handleAdjustmentSubmit(event) {
    event.preventDefault();
    if (!isAdmin) {
      return;
    }

    setAdjustmentError("");
    setIsSavingAdjustment(true);

    const payload = {
      branchId: Number(adjustmentForm.branchId),
      productId: Number(adjustmentForm.productId),
      quantity: Number(adjustmentForm.quantity),
      adjustmentType: adjustmentForm.adjustmentType,
      minimumStock:
        adjustmentForm.minimumStock === ""
          ? null
          : Number(adjustmentForm.minimumStock),
      reference: adjustmentForm.reference.trim(),
      notes: adjustmentForm.notes.trim()
    };

    try {
      await authorizedRequest("/api/stocks/adjustments", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      resetAdjustmentForm();
      await loadData();
    } catch (saveError) {
      setAdjustmentError(saveError.message);
    } finally {
      setIsSavingAdjustment(false);
    }
  }

  async function handleTransferSubmit(event) {
    event.preventDefault();
    if (!isAdmin) {
      return;
    }

    setTransferError("");
    setIsSavingTransfer(true);

    const payload = {
      sourceBranchId: Number(transferForm.sourceBranchId),
      targetBranchId: Number(transferForm.targetBranchId),
      productId: Number(transferForm.productId),
      quantity: Number(transferForm.quantity),
      reference: transferForm.reference.trim(),
      notes: transferForm.notes.trim()
    };

    try {
      await authorizedRequest("/api/stocks/transfers", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      resetTransferForm();
      await loadData();
    } catch (saveError) {
      setTransferError(saveError.message);
    } finally {
      setIsSavingTransfer(false);
    }
  }

  const filteredStocks = useMemo(() => {
    const normalizedSearch = stockSearch.trim().toLowerCase();

    return stocks.filter((stock) => {
      const matchesSearch =
        normalizedSearch.length === 0 ||
        stock.branchName.toLowerCase().includes(normalizedSearch) ||
        stock.branchCode.toLowerCase().includes(normalizedSearch) ||
        stock.productName.toLowerCase().includes(normalizedSearch) ||
        stock.productSku.toLowerCase().includes(normalizedSearch);

      const matchesBranch =
        selectedBranchFilter === "all" ||
        String(stock.branchId) === selectedBranchFilter;

      const matchesProduct =
        selectedProductFilter === "all" ||
        String(stock.productId) === selectedProductFilter;

      const matchesLowStock = !showLowStockOnly || stock.lowStock;

      return matchesSearch && matchesBranch && matchesProduct && matchesLowStock;
    });
  }, [
    stocks,
    stockSearch,
    selectedBranchFilter,
    selectedProductFilter,
    showLowStockOnly
  ]);

  const filteredMovements = useMemo(() => {
    return movements.filter((movement) => {
      const matchesBranch =
        selectedBranchFilter === "all" ||
        String(movement.branchId) === selectedBranchFilter;

      const matchesProduct =
        selectedProductFilter === "all" ||
        String(movement.productId) === selectedProductFilter;

      const matchesType =
        movementTypeFilter === "all" ||
        movement.movementType === movementTypeFilter;

      return matchesBranch && matchesProduct && matchesType;
    });
  }, [movements, selectedBranchFilter, selectedProductFilter, movementTypeFilter]);

  const lowStockCount = stocks.filter((stock) => stock.lowStock).length;

  return (
    <div className="row g-4">
      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Modulo operativo
              </span>
              <h2 className="h4 mb-1">Stock y movimientos</h2>
              <p className="text-secondary mb-0">
                Consulta inventario por sucursal, detecta bajo stock y registra
                ajustes o transferencias con trazabilidad.
              </p>
            </div>

            <div className="identity-pill">
              <span>{isAdmin ? "Gestion completa" : "Consulta operativa"}</span>
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
        <section className="metric-card metric-stock p-4 h-100">
          <span className="metric-label">Registros de stock</span>
          <strong className="metric-value">
            {isLoading ? "--" : stocks.length}
          </strong>
          <p className="metric-subtitle mb-0">
            Productos distribuidos entre sucursales activas
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-sales p-4 h-100">
          <span className="metric-label">Bajo stock</span>
          <strong className="metric-value">
            {isLoading ? "--" : lowStockCount}
          </strong>
          <p className="metric-subtitle mb-0">
            Alertas criticas para reposicion o transferencia
          </p>
        </section>
      </div>

      <div className="col-md-4">
        <section className="metric-card metric-purchases p-4 h-100">
          <span className="metric-label">Movimientos</span>
          <strong className="metric-value">
            {isLoading ? "--" : movements.length}
          </strong>
          <p className="metric-subtitle mb-0">
            Historial disponible con referencia y fecha
          </p>
        </section>
      </div>

      <div className="col-12">
        <section className="content-card p-4">
          <div className="stock-filters-grid">
            <input
              type="search"
              className="form-control"
              placeholder="Buscar por sucursal, codigo o producto"
              value={stockSearch}
              onChange={(event) => setStockSearch(event.target.value)}
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

            <select
              className="form-select"
              value={selectedProductFilter}
              onChange={(event) => setSelectedProductFilter(event.target.value)}
            >
              <option value="all">Todos los productos</option>
              {products.map((product) => (
                <option key={product.id} value={String(product.id)}>
                  {product.sku} - {product.name}
                </option>
              ))}
            </select>

            <select
              className="form-select"
              value={movementTypeFilter}
              onChange={(event) => setMovementTypeFilter(event.target.value)}
            >
              <option value="all">Todos los movimientos</option>
              {Object.entries(movementLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-check mt-3">
            <input
              id="low-stock-only"
              type="checkbox"
              className="form-check-input"
              checked={showLowStockOnly}
              onChange={(event) => setShowLowStockOnly(event.target.checked)}
            />
            <label className="form-check-label" htmlFor="low-stock-only">
              Mostrar solo productos con bajo stock
            </label>
          </div>
        </section>
      </div>

      <div className="col-xl-7">
        <section className="content-card p-4 h-100">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4">
            <div>
              <h3 className="h5 mb-1">Inventario por sucursal</h3>
              <p className="text-secondary mb-0">
                {isLoading
                  ? "Cargando stock..."
                  : `${filteredStocks.length} registros visibles de ${stocks.length} disponibles.`}
              </p>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando inventario...</div>
          ) : filteredStocks.length === 0 ? (
            <div className="empty-state">
              <strong>No hay stock para mostrar.</strong>
              <span>Ajusta los filtros o registra una carga inicial.</span>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle dashboard-table mb-0">
                <thead>
                  <tr>
                    <th>Sucursal</th>
                    <th>Producto</th>
                    <th>Actual</th>
                    <th>Minimo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredStocks.map((stock) => (
                    <tr key={stock.id}>
                      <td>
                        <div className="d-grid">
                          <strong>{stock.branchName}</strong>
                          <span className="text-secondary small">
                            {stock.branchCode}
                          </span>
                        </div>
                      </td>
                      <td>
                        <div className="d-grid">
                          <strong>{stock.productName}</strong>
                          <span className="text-secondary small">
                            {stock.productSku}
                          </span>
                        </div>
                      </td>
                      <td>{stock.quantity}</td>
                      <td>{stock.minimumStock}</td>
                      <td>
                        <span
                          className={`status-pill ${stock.lowStock ? "inactive" : "active"}`}
                        >
                          {stock.lowStock ? "Bajo stock" : "Disponible"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>

      <div className="col-xl-5">
        <section className="content-card p-4 h-100">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4">
            <div>
              <h3 className="h5 mb-1">Historial de movimientos</h3>
              <p className="text-secondary mb-0">
                Transferencias, compras, ventas y ajustes registrados por fecha.
              </p>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando movimientos...</div>
          ) : filteredMovements.length === 0 ? (
            <div className="empty-state">
              <strong>No hay movimientos para mostrar.</strong>
              <span>Los nuevos eventos apareceran aqui automaticamente.</span>
            </div>
          ) : (
            <div className="stock-movement-list">
              {filteredMovements.slice(0, 20).map((movement) => (
                <article className="movement-item" key={movement.id}>
                  <div className="d-flex justify-content-between gap-3 align-items-start">
                    <div className="d-grid gap-1">
                      <div className="d-flex flex-wrap gap-2 align-items-center">
                        <span className="table-chip">
                          {movementLabels[movement.movementType] ?? movement.movementType}
                        </span>
                        <strong>{movement.productName}</strong>
                      </div>
                      <span className="text-secondary small">
                        {movement.productSku} · {movement.branchCode} - {movement.branchName}
                      </span>
                      <span className="text-secondary small">
                        Ref: {movement.reference}
                      </span>
                      <span className="text-secondary small">
                        {movement.notes || "Sin notas"}
                      </span>
                    </div>

                    <div className="text-end movement-side">
                      <strong>{movement.quantity}</strong>
                      <span>{formatDateTime(movement.occurredAt)}</span>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>

      <div className="col-xl-6">
        <section className="content-card p-4 h-100">
          <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
            <div>
              <h3 className="h5 mb-1">Ajuste de stock</h3>
              <p className="text-secondary mb-0">
                {isAdmin
                  ? "Registra carga inicial, aumento o disminucion manual de inventario."
                  : "Tu rol puede consultar stock, pero no registrar ajustes."}
              </p>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={resetAdjustmentForm}
              disabled={!isAdmin}
            >
              Limpiar
            </button>
          </div>

          <form className="d-grid gap-3" onSubmit={handleAdjustmentSubmit}>
            <div className="row g-3">
              <div className="col-sm-6">
                <label className="form-label">Sucursal</label>
                <select
                  className="form-select"
                  name="branchId"
                  value={adjustmentForm.branchId}
                  onChange={handleAdjustmentInputChange}
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

              <div className="col-sm-6">
                <label className="form-label">Producto</label>
                <select
                  className="form-select"
                  name="productId"
                  value={adjustmentForm.productId}
                  onChange={handleAdjustmentInputChange}
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
            </div>

            <div className="row g-3">
              <div className="col-sm-4">
                <label className="form-label">Cantidad</label>
                <input
                  type="number"
                  min="1"
                  className="form-control"
                  name="quantity"
                  value={adjustmentForm.quantity}
                  onChange={handleAdjustmentInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div className="col-sm-4">
                <label className="form-label">Tipo</label>
                <select
                  className="form-select"
                  name="adjustmentType"
                  value={adjustmentForm.adjustmentType}
                  onChange={handleAdjustmentInputChange}
                  disabled={!isAdmin}
                  required
                >
                  <option value="INITIAL_LOAD">Carga inicial</option>
                  <option value="INCREASE">Aumento</option>
                  <option value="DECREASE">Disminucion</option>
                </select>
              </div>

              <div className="col-sm-4">
                <label className="form-label">Stock minimo</label>
                <input
                  type="number"
                  min="0"
                  className="form-control"
                  name="minimumStock"
                  value={adjustmentForm.minimumStock}
                  onChange={handleAdjustmentInputChange}
                  disabled={!isAdmin}
                />
              </div>
            </div>

            <div>
              <label className="form-label">Referencia</label>
              <input
                className="form-control"
                name="reference"
                value={adjustmentForm.reference}
                onChange={handleAdjustmentInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Notas</label>
              <textarea
                className="form-control"
                rows="3"
                name="notes"
                value={adjustmentForm.notes}
                onChange={handleAdjustmentInputChange}
                disabled={!isAdmin}
              />
            </div>

            {adjustmentError ? (
              <div className="alert alert-danger mb-0" role="alert">
                {adjustmentError}
              </div>
            ) : null}

            <button
              type="submit"
              className="btn btn-auth"
              disabled={!isAdmin || isSavingAdjustment}
            >
              {isSavingAdjustment ? "Guardando..." : "Registrar ajuste"}
            </button>
          </form>
        </section>
      </div>

      <div className="col-xl-6">
        <section className="content-card p-4 h-100">
          <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
            <div>
              <h3 className="h5 mb-1">Transferencia entre sucursales</h3>
              <p className="text-secondary mb-0">
                {isAdmin
                  ? "Mueve inventario entre sedes manteniendo historial de salida y entrada."
                  : "Tu rol puede consultar movimientos, pero no transferir stock."}
              </p>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary"
              onClick={resetTransferForm}
              disabled={!isAdmin}
            >
              Limpiar
            </button>
          </div>

          <form className="d-grid gap-3" onSubmit={handleTransferSubmit}>
            <div className="row g-3">
              <div className="col-sm-6">
                <label className="form-label">Origen</label>
                <select
                  className="form-select"
                  name="sourceBranchId"
                  value={transferForm.sourceBranchId}
                  onChange={handleTransferInputChange}
                  disabled={!isAdmin}
                  required
                >
                  <option value="">Selecciona sucursal origen</option>
                  {branches.map((branch) => (
                    <option key={branch.id} value={String(branch.id)}>
                      {branch.code} - {branch.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-sm-6">
                <label className="form-label">Destino</label>
                <select
                  className="form-select"
                  name="targetBranchId"
                  value={transferForm.targetBranchId}
                  onChange={handleTransferInputChange}
                  disabled={!isAdmin}
                  required
                >
                  <option value="">Selecciona sucursal destino</option>
                  {branches.map((branch) => (
                    <option key={branch.id} value={String(branch.id)}>
                      {branch.code} - {branch.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="row g-3">
              <div className="col-sm-8">
                <label className="form-label">Producto</label>
                <select
                  className="form-select"
                  name="productId"
                  value={transferForm.productId}
                  onChange={handleTransferInputChange}
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

              <div className="col-sm-4">
                <label className="form-label">Cantidad</label>
                <input
                  type="number"
                  min="1"
                  className="form-control"
                  name="quantity"
                  value={transferForm.quantity}
                  onChange={handleTransferInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>
            </div>

            <div>
              <label className="form-label">Referencia</label>
              <input
                className="form-control"
                name="reference"
                value={transferForm.reference}
                onChange={handleTransferInputChange}
                disabled={!isAdmin}
                required
              />
            </div>

            <div>
              <label className="form-label">Notas</label>
              <textarea
                className="form-control"
                rows="3"
                name="notes"
                value={transferForm.notes}
                onChange={handleTransferInputChange}
                disabled={!isAdmin}
              />
            </div>

            {transferError ? (
              <div className="alert alert-danger mb-0" role="alert">
                {transferError}
              </div>
            ) : null}

            <button
              type="submit"
              className="btn btn-outline-dark"
              disabled={!isAdmin || isSavingTransfer}
            >
              {isSavingTransfer ? "Guardando..." : "Registrar transferencia"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}

export default StockPage;
