package com.multisucursal.inventory.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multisucursal.inventory.auth.dto.LoginRequest;
import com.multisucursal.inventory.branch.dto.BranchRequest;
import com.multisucursal.inventory.product.dto.CategoryRequest;
import com.multisucursal.inventory.product.dto.ProductRequest;
import com.multisucursal.inventory.purchase.dto.PurchaseItemRequest;
import com.multisucursal.inventory.purchase.dto.PurchaseRequest;
import com.multisucursal.inventory.sale.dto.SaleItemRequest;
import com.multisucursal.inventory.sale.dto.SaleRequest;
import com.multisucursal.inventory.stock.dto.AdjustmentType;
import com.multisucursal.inventory.stock.dto.StockAdjustmentRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnDashboardIndicatorsForAdmin() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1).withDayOfMonth(10);

        Long categoryId = createCategory(adminToken, "Dashboard Category", "Metricas de dashboard");
        Long branchId = createBranch(adminToken, "DSH-CENTRO", "Sucursal Dashboard");
        Long lowStockProductId = createProduct(adminToken, categoryId, "DSH-001", "Producto Critico");
        Long stableProductId = createProduct(adminToken, categoryId, "DSH-002", "Producto Estable");

        adjustInitialStock(adminToken, branchId, lowStockProductId, 6, 5, "INIT-DSH-1");
        adjustInitialStock(adminToken, branchId, stableProductId, 9, 2, "INIT-DSH-2");

        createPurchase(adminToken, branchId, "PO-DSH-001", today, List.of(
            new PurchaseItemRequest(stableProductId, 3, new BigDecimal("100.00"))
        ));
        createPurchase(adminToken, branchId, "PO-DSH-002", lastMonth, List.of(
            new PurchaseItemRequest(stableProductId, 2, new BigDecimal("110.00"))
        ));

        createSale(adminToken, branchId, "SO-DSH-001", today, List.of(
            new SaleItemRequest(lowStockProductId, 2, new BigDecimal("200.00"))
        ));
        createSale(adminToken, branchId, "SO-DSH-002", today.minusDays(1), List.of(
            new SaleItemRequest(stableProductId, 1, new BigDecimal("210.00"))
        ));

        mockMvc.perform(get("/api/dashboard")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.generatedDate").value(today.toString()))
            .andExpect(jsonPath("$.salesToday.count").value(1))
            .andExpect(jsonPath("$.salesToday.totalAmount").value(400.00))
            .andExpect(jsonPath("$.purchasesThisMonth.count").value(1))
            .andExpect(jsonPath("$.purchasesThisMonth.totalAmount").value(300.00))
            .andExpect(jsonPath("$.lowStockProducts.length()").value(1))
            .andExpect(jsonPath("$.lowStockProducts[0].productSku").value("DSH-001"))
            .andExpect(jsonPath("$.lowStockProducts[0].quantity").value(4))
            .andExpect(jsonPath("$.lowStockProducts[0].minimumStock").value(5));
    }

    @Test
    void shouldAllowEmployeeToReadDashboard() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        String employeeToken = registerAndLoginEmployee("dashboard-empleado@correo.cl", "Password123!");

        Long categoryId = createCategory(adminToken, "Dashboard Read", "Lectura dashboard");
        Long branchId = createBranch(adminToken, "DSH-SUR", "Sucursal Dashboard Sur");
        Long productId = createProduct(adminToken, categoryId, "DSH-READ-001", "Producto Lectura");

        adjustInitialStock(adminToken, branchId, productId, 2, 3, "INIT-READ-1");

        mockMvc.perform(get("/api/dashboard")
                .header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lowStockProducts.length()").value(1))
            .andExpect(jsonPath("$.lowStockProducts[0].productSku").value("DSH-READ-001"));
    }

    private void adjustInitialStock(
        String token,
        Long branchId,
        Long productId,
        int quantity,
        int minimumStock,
        String reference
    ) throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(
            branchId,
            productId,
            quantity,
            AdjustmentType.INITIAL_LOAD,
            minimumStock,
            reference,
            "Carga para dashboard"
        );

        mockMvc.perform(post("/api/stocks/adjustments")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    private void createPurchase(String token, Long branchId, String purchaseNumber, LocalDate purchaseDate, List<PurchaseItemRequest> items)
        throws Exception {
        PurchaseRequest request = new PurchaseRequest(
            purchaseNumber,
            "Proveedor Dashboard",
            purchaseDate,
            branchId,
            "Compra dashboard",
            items
        );

        mockMvc.perform(post("/api/purchases")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    private void createSale(String token, Long branchId, String saleNumber, LocalDate saleDate, List<SaleItemRequest> items)
        throws Exception {
        SaleRequest request = new SaleRequest(
            saleNumber,
            "Cliente Dashboard",
            saleDate,
            branchId,
            "Venta dashboard",
            items
        );

        mockMvc.perform(post("/api/sales")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    private Long createCategory(String token, String name, String description) throws Exception {
        CategoryRequest request = new CategoryRequest(name, description, true);
        MvcResult result = mockMvc.perform(post("/api/categories")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return readId(result);
    }

    private Long createProduct(String token, Long categoryId, String sku, String name) throws Exception {
        ProductRequest request = new ProductRequest(
            sku,
            name,
            "Producto para dashboard",
            new BigDecimal("100.00"),
            new BigDecimal("150.00"),
            true,
            categoryId
        );
        MvcResult result = mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return readId(result);
    }

    private Long createBranch(String token, String code, String name) throws Exception {
        BranchRequest request = new BranchRequest(
            code,
            name,
            "Santiago",
            "Direccion dashboard 123",
            "Encargado Dashboard",
            "+56 9 4444 4444",
            code.toLowerCase() + "@multisucursal.dev",
            true
        );
        MvcResult result = mockMvc.perform(post("/api/branches")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        return readId(result);
    }

    private String registerAndLoginEmployee(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new com.multisucursal.inventory.auth.dto.RegisterRequest("Empleado Dashboard", email, password)
                )))
            .andExpect(status().isCreated());
        return loginAndGetToken(email, password);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }

    private Long readId(MvcResult result) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
