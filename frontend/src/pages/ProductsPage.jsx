import { useEffect, useMemo, useState } from "react";
import { apiRequest } from "../lib/api";
import { formatCurrency } from "../lib/formatters";
import { useAuth } from "../auth/AuthContext";

const emptyProductForm = {
  sku: "",
  name: "",
  description: "",
  purchasePrice: "",
  salePrice: "",
  active: true,
  categoryId: ""
};

const emptyCategoryForm = {
  name: "",
  description: "",
  active: true
};

function ProductsPage() {
  const { token, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingProduct, setIsSavingProduct] = useState(false);
  const [isSavingCategory, setIsSavingCategory] = useState(false);
  const [screenError, setScreenError] = useState("");
  const [productError, setProductError] = useState("");
  const [categoryError, setCategoryError] = useState("");
  const [search, setSearch] = useState("");
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState("all");
  const [editingProductId, setEditingProductId] = useState(null);
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [productForm, setProductForm] = useState(emptyProductForm);
  const [categoryForm, setCategoryForm] = useState(emptyCategoryForm);

  useEffect(() => {
    loadData();
  }, [token]);

  async function loadData() {
    setIsLoading(true);
    setScreenError("");

    try {
      const [productsResponse, categoriesResponse] = await Promise.all([
        authorizedRequest("/api/products"),
        authorizedRequest("/api/categories")
      ]);

      setProducts(productsResponse);
      setCategories(categoriesResponse);
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

  function resetProductForm() {
    setEditingProductId(null);
    setProductForm(emptyProductForm);
    setProductError("");
  }

  function resetCategoryForm() {
    setEditingCategoryId(null);
    setCategoryForm(emptyCategoryForm);
    setCategoryError("");
  }

  function handleProductInputChange(event) {
    const { name, value, type, checked } = event.target;
    setProductForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  function handleCategoryInputChange(event) {
    const { name, value, type, checked } = event.target;
    setCategoryForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  async function handleProductSubmit(event) {
    event.preventDefault();
    if (!isAdmin) {
      return;
    }

    setProductError("");
    setIsSavingProduct(true);

    const payload = {
      sku: productForm.sku.trim(),
      name: productForm.name.trim(),
      description: productForm.description.trim(),
      purchasePrice: Number(productForm.purchasePrice),
      salePrice: Number(productForm.salePrice),
      active: productForm.active,
      categoryId: Number(productForm.categoryId)
    };

    try {
      if (editingProductId) {
        await authorizedRequest(`/api/products/${editingProductId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await authorizedRequest("/api/products", {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      resetProductForm();
      await loadData();
    } catch (saveError) {
      setProductError(saveError.message);
    } finally {
      setIsSavingProduct(false);
    }
  }

  async function handleCategorySubmit(event) {
    event.preventDefault();
    if (!isAdmin) {
      return;
    }

    setCategoryError("");
    setIsSavingCategory(true);

    const payload = {
      name: categoryForm.name.trim(),
      description: categoryForm.description.trim(),
      active: categoryForm.active
    };

    try {
      if (editingCategoryId) {
        await authorizedRequest(`/api/categories/${editingCategoryId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await authorizedRequest("/api/categories", {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      resetCategoryForm();
      await loadData();
    } catch (saveError) {
      setCategoryError(saveError.message);
    } finally {
      setIsSavingCategory(false);
    }
  }

  function startEditProduct(product) {
    setEditingProductId(product.id);
    setProductError("");
    setProductForm({
      sku: product.sku,
      name: product.name,
      description: product.description ?? "",
      purchasePrice: product.purchasePrice,
      salePrice: product.salePrice,
      active: product.active,
      categoryId: String(product.categoryId)
    });
  }

  function startEditCategory(category) {
    setEditingCategoryId(category.id);
    setCategoryError("");
    setCategoryForm({
      name: category.name,
      description: category.description ?? "",
      active: category.active
    });
  }

  async function handleDeleteProduct(product) {
    if (!isAdmin) {
      return;
    }

    const confirmed = window.confirm(
      `Eliminar el producto ${product.name} (${product.sku})?`
    );
    if (!confirmed) {
      return;
    }

    try {
      await authorizedRequest(`/api/products/${product.id}`, {
        method: "DELETE"
      });

      if (editingProductId === product.id) {
        resetProductForm();
      }

      await loadData();
    } catch (deleteError) {
      setProductError(deleteError.message);
    }
  }

  async function handleDeleteCategory(category) {
    if (!isAdmin) {
      return;
    }

    const confirmed = window.confirm(`Eliminar la categoria ${category.name}?`);
    if (!confirmed) {
      return;
    }

    try {
      await authorizedRequest(`/api/categories/${category.id}`, {
        method: "DELETE"
      });

      if (editingCategoryId === category.id) {
        resetCategoryForm();
      }

      await loadData();
    } catch (deleteError) {
      setCategoryError(deleteError.message);
    }
  }

  const filteredProducts = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return products.filter((product) => {
      const matchesSearch =
        normalizedSearch.length === 0 ||
        product.name.toLowerCase().includes(normalizedSearch) ||
        product.sku.toLowerCase().includes(normalizedSearch) ||
        product.categoryName.toLowerCase().includes(normalizedSearch);

      const matchesCategory =
        selectedCategoryFilter === "all" ||
        String(product.categoryId) === selectedCategoryFilter;

      return matchesSearch && matchesCategory;
    });
  }, [products, search, selectedCategoryFilter]);

  return (
    <div className="row g-4">
      <div className="col-12">
        <section className="content-card p-4">
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 align-items-lg-center">
            <div>
              <span className="badge text-bg-warning text-dark mb-2">
                Modulo operativo
              </span>
              <h2 className="h4 mb-1">Productos y categorias</h2>
              <p className="text-secondary mb-0">
                Gestiona el catalogo del sistema desde el frontend real conectado al backend.
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
              <h3 className="h5 mb-1">Catalogo de productos</h3>
              <p className="text-secondary mb-0">
                {isLoading
                  ? "Cargando productos..."
                  : `${filteredProducts.length} productos visibles de ${products.length} registrados.`}
              </p>
            </div>

            <div className="filters-row">
              <input
                type="search"
                className="form-control"
                placeholder="Buscar por nombre, SKU o categoria"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />

              <select
                className="form-select"
                value={selectedCategoryFilter}
                onChange={(event) => setSelectedCategoryFilter(event.target.value)}
              >
                <option value="all">Todas las categorias</option>
                {categories.map((category) => (
                  <option key={category.id} value={String(category.id)}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {isLoading ? (
            <div className="table-placeholder">Cargando catalogo...</div>
          ) : filteredProducts.length === 0 ? (
            <div className="empty-state">
              <strong>No hay productos para mostrar.</strong>
              <span>Ajusta los filtros o crea un producto nuevo.</span>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle dashboard-table mb-0">
                <thead>
                  <tr>
                    <th>SKU</th>
                    <th>Producto</th>
                    <th>Categoria</th>
                    <th>Compra</th>
                    <th>Venta</th>
                    <th>Estado</th>
                    <th className="text-end">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredProducts.map((product) => (
                    <tr key={product.id}>
                      <td>
                        <span className="table-chip">{product.sku}</span>
                      </td>
                      <td>
                        <div className="d-grid">
                          <strong>{product.name}</strong>
                          <span className="text-secondary small">
                            {product.description || "Sin descripcion"}
                          </span>
                        </div>
                      </td>
                      <td>{product.categoryName}</td>
                      <td>{formatCurrency(product.purchasePrice)}</td>
                      <td>{formatCurrency(product.salePrice)}</td>
                      <td>
                        <span className={`status-pill ${product.active ? "active" : "inactive"}`}>
                          {product.active ? "Activo" : "Inactivo"}
                        </span>
                      </td>
                      <td className="text-end">
                        <div className="d-inline-flex gap-2">
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-dark"
                            onClick={() => startEditProduct(product)}
                            disabled={!isAdmin}
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleDeleteProduct(product)}
                            disabled={!isAdmin}
                          >
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>

      <div className="col-xl-4">
        <div className="d-grid gap-4">
          <section className="content-card p-4">
            <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
              <div>
                <h3 className="h5 mb-1">
                  {editingProductId ? "Editar producto" : "Nuevo producto"}
                </h3>
                <p className="text-secondary mb-0">
                  {isAdmin
                    ? "Crea o actualiza productos vinculados a una categoria."
                    : "Tu rol puede consultar, pero no modificar productos."}
                </p>
              </div>
              {editingProductId ? (
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={resetProductForm}>
                  Cancelar
                </button>
              ) : null}
            </div>

            <form className="d-grid gap-3" onSubmit={handleProductSubmit}>
              <div>
                <label className="form-label">SKU</label>
                <input
                  className="form-control"
                  name="sku"
                  value={productForm.sku}
                  onChange={handleProductInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div>
                <label className="form-label">Nombre</label>
                <input
                  className="form-control"
                  name="name"
                  value={productForm.name}
                  onChange={handleProductInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div>
                <label className="form-label">Descripcion</label>
                <textarea
                  className="form-control"
                  rows="3"
                  name="description"
                  value={productForm.description}
                  onChange={handleProductInputChange}
                  disabled={!isAdmin}
                />
              </div>

              <div className="row g-3">
                <div className="col-sm-6">
                  <label className="form-label">Precio compra</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="form-control"
                    name="purchasePrice"
                    value={productForm.purchasePrice}
                    onChange={handleProductInputChange}
                    disabled={!isAdmin}
                    required
                  />
                </div>

                <div className="col-sm-6">
                  <label className="form-label">Precio venta</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="form-control"
                    name="salePrice"
                    value={productForm.salePrice}
                    onChange={handleProductInputChange}
                    disabled={!isAdmin}
                    required
                  />
                </div>
              </div>

              <div>
                <label className="form-label">Categoria</label>
                <select
                  className="form-select"
                  name="categoryId"
                  value={productForm.categoryId}
                  onChange={handleProductInputChange}
                  disabled={!isAdmin}
                  required
                >
                  <option value="">Selecciona una categoria</option>
                  {categories.map((category) => (
                    <option key={category.id} value={String(category.id)}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-check">
                <input
                  id="product-active"
                  type="checkbox"
                  className="form-check-input"
                  name="active"
                  checked={productForm.active}
                  onChange={handleProductInputChange}
                  disabled={!isAdmin}
                />
                <label className="form-check-label" htmlFor="product-active">
                  Producto activo
                </label>
              </div>

              {productError ? (
                <div className="alert alert-danger mb-0" role="alert">
                  {productError}
                </div>
              ) : null}

              <button
                type="submit"
                className="btn btn-auth"
                disabled={!isAdmin || isSavingProduct}
              >
                {isSavingProduct
                  ? "Guardando..."
                  : editingProductId
                    ? "Actualizar producto"
                    : "Crear producto"}
              </button>
            </form>
          </section>

          <section className="content-card p-4">
            <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
              <div>
                <h3 className="h5 mb-1">
                  {editingCategoryId ? "Editar categoria" : "Categorias"}
                </h3>
                <p className="text-secondary mb-0">
                  Administra la base del catalogo para organizar productos.
                </p>
              </div>
              {editingCategoryId ? (
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={resetCategoryForm}>
                  Cancelar
                </button>
              ) : null}
            </div>

            <form className="d-grid gap-3 mb-4" onSubmit={handleCategorySubmit}>
              <div>
                <label className="form-label">Nombre</label>
                <input
                  className="form-control"
                  name="name"
                  value={categoryForm.name}
                  onChange={handleCategoryInputChange}
                  disabled={!isAdmin}
                  required
                />
              </div>

              <div>
                <label className="form-label">Descripcion</label>
                <textarea
                  className="form-control"
                  rows="2"
                  name="description"
                  value={categoryForm.description}
                  onChange={handleCategoryInputChange}
                  disabled={!isAdmin}
                />
              </div>

              <div className="form-check">
                <input
                  id="category-active"
                  type="checkbox"
                  className="form-check-input"
                  name="active"
                  checked={categoryForm.active}
                  onChange={handleCategoryInputChange}
                  disabled={!isAdmin}
                />
                <label className="form-check-label" htmlFor="category-active">
                  Categoria activa
                </label>
              </div>

              {categoryError ? (
                <div className="alert alert-danger mb-0" role="alert">
                  {categoryError}
                </div>
              ) : null}

              <button
                type="submit"
                className="btn btn-outline-dark"
                disabled={!isAdmin || isSavingCategory}
              >
                {isSavingCategory
                  ? "Guardando..."
                  : editingCategoryId
                    ? "Actualizar categoria"
                    : "Crear categoria"}
              </button>
            </form>

            <div className="d-grid gap-2">
              {categories.map((category) => (
                <article className="category-item" key={category.id}>
                  <div>
                    <strong>{category.name}</strong>
                    <div className="text-secondary small">
                      {category.productCount} productos
                    </div>
                  </div>

                  <div className="d-inline-flex gap-2">
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-dark"
                      onClick={() => startEditCategory(category)}
                      disabled={!isAdmin}
                    >
                      Editar
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDeleteCategory(category)}
                      disabled={!isAdmin}
                    >
                      Eliminar
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}

export default ProductsPage;
