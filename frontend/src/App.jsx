import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import AuthShell from "./components/AuthShell";
import BranchesPage from "./pages/BranchesPage";
import DashboardHomePage from "./pages/DashboardHomePage";
import LoginPage from "./pages/LoginPage";
import ModulePlaceholderPage from "./pages/ModulePlaceholderPage";
import ProductsPage from "./pages/ProductsPage";
import StockPage from "./pages/StockPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate replace to="/app/dashboard" />} />
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/app" element={<AuthShell />}>
          <Route path="" element={<Navigate replace to="/app/dashboard" />} />
          <Route path="dashboard" element={<DashboardHomePage />} />
          <Route path="productos" element={<ProductsPage />} />
          <Route path="sucursales" element={<BranchesPage />} />
          <Route path="stock" element={<StockPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate replace to="/app/dashboard" />} />
    </Routes>
  );
}

export default App;
