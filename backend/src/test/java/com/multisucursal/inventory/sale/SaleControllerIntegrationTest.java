package com.multisucursal.inventory.sale;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterSaleAndDecreaseBranchStock() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");

        Long categoryId = createCategory(adminToken, "Ventas Laptops", "Equipos para venta");
        Long branchId = createBranch(adminToken, "VTA-CENTRO", "Sucursal Ventas Centro");
        Long productOneId = createProduct(adminToken, categoryId, "LAP-VTA-001", "Laptop HP Ventas");
        Long productTwoId = createProduct(adminToken, categoryId, "LAP-VTA-002", "Laptop Asus Ventas");

        registerPurchase(adminToken, branchId, "PO-SALE-001", "Proveedor Ventas", List.of(
            new PurchaseItemRequest(productOneId, 6, new BigDecimal("400.00")),
            new PurchaseItemRequest(productTwoId, 4, new BigDecimal("430.00"))
        ));

        SaleRequest request = new SaleRequest(
            "SO-2026-001",
            "Cliente Demo",
            LocalDate.of(2026, 6, 19),
            branchId,
            "Venta mostrador",
            List.of(
                new SaleItemRequest(productOneId, 2, new BigDecimal("650.00")),
                new SaleItemRequest(productTwoId, 1, new BigDecimal("690.00"))
            )
        );

        mockMvc.perform(post("/api/sales")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.saleNumber").value("SO-2026-001"))
            .andExpect(jsonPath("$.totalAmount").value(1990.00))
            .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(get("/api/stocks/branch/{branchId}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.productSku=='LAP-VTA-001')].quantity").value(4))
            .andExpect(jsonPath("$[?(@.productSku=='LAP-VTA-002')].quantity").value(3));

        mockMvc.perform(get("/api/stocks/movements/branch/{branchId}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].movementType").value("SALE"))
            .andExpect(jsonPath("$[1].movementType").value("SALE"));
    }

    @Test
    void shouldRejectSaleWithoutSufficientStockAndAllowEmployeeReadOnly() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        String employeeToken = registerAndLoginEmployee("ventas-empleado@correo.cl", "Password123!");

        Long categoryId = createCategory(adminToken, "Ventas Accesorios", "Accesorios para venta");
        Long branchId = createBranch(adminToken, "VTA-SUR", "Sucursal Ventas Sur");
        Long productId = createProduct(adminToken, categoryId, "ACC-VTA-001", "Mouse Gamer Ventas");

        registerPurchase(adminToken, branchId, "PO-SALE-002", "Proveedor Stock", List.of(
            new PurchaseItemRequest(productId, 3, new BigDecimal("20.00"))
        ));

        mockMvc.perform(post("/api/sales")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new SaleRequest(
                        "SO-2026-002",
                        "Cliente Exceso",
                        LocalDate.of(2026, 6, 19),
                        branchId,
                        "Debe fallar por stock",
                        List.of(new SaleItemRequest(productId, 5, new BigDecimal("35.00")))
                    )
                )))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Insufficient stock for product ACC-VTA-001 in branch VTA-SUR"));

        mockMvc.perform(get("/api/sales/branch/{branchId}", branchId)
                .header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/sales")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new SaleRequest(
                        "SO-2026-003",
                        "Cliente Bloqueado",
                        LocalDate.of(2026, 6, 19),
                        branchId,
                        null,
                        List.of(new SaleItemRequest(productId, 1, new BigDecimal("35.00")))
                    )
                )))
            .andExpect(status().isForbidden());
    }

    private void registerPurchase(String token, Long branchId, String purchaseNumber, String supplierName, List<PurchaseItemRequest> items)
        throws Exception {
        PurchaseRequest request = new PurchaseRequest(
            purchaseNumber,
            supplierName,
            LocalDate.of(2026, 6, 18),
            branchId,
            "Carga para pruebas de ventas",
            items
        );

        mockMvc.perform(post("/api/purchases")
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
            "Producto para pruebas de ventas",
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
            "Direccion ventas 123",
            "Encargado Ventas",
            "+56 9 3333 3333",
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
                    new com.multisucursal.inventory.auth.dto.RegisterRequest("Empleado Ventas", email, password)
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
