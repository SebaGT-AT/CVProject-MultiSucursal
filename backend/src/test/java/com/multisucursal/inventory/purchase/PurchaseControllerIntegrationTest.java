package com.multisucursal.inventory.purchase;

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
class PurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterPurchaseAndIncreaseBranchStock() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");

        Long categoryId = createCategory(adminToken, "Laptops Compras", "Equipos portatiles");
        Long branchId = createBranch(adminToken, "CMP-CENTRO", "Sucursal Compras Centro");
        Long productOneId = createProduct(adminToken, categoryId, "LAP-CMP-001", "Laptop Lenovo Compras");
        Long productTwoId = createProduct(adminToken, categoryId, "LAP-CMP-002", "Laptop Dell Compras");

        PurchaseRequest request = new PurchaseRequest(
            "PO-2026-001",
            "Proveedor Tech SPA",
            LocalDate.of(2026, 6, 19),
            branchId,
            "Compra inicial de laptops",
            List.of(
                new PurchaseItemRequest(productOneId, 5, new BigDecimal("450.00")),
                new PurchaseItemRequest(productTwoId, 3, new BigDecimal("520.00"))
            )
        );

        mockMvc.perform(post("/api/purchases")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.purchaseNumber").value("PO-2026-001"))
            .andExpect(jsonPath("$.totalAmount").value(3810.00))
            .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(get("/api/stocks/branch/{branchId}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.productSku=='LAP-CMP-001')].quantity").value(5))
            .andExpect(jsonPath("$[?(@.productSku=='LAP-CMP-002')].quantity").value(3));

        mockMvc.perform(get("/api/stocks/movements/branch/{branchId}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].movementType").value("PURCHASE"))
            .andExpect(jsonPath("$[1].movementType").value("PURCHASE"))
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldAllowEmployeeToReadPurchasesButNotCreateThem() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        String employeeToken = registerAndLoginEmployee("compras-empleado@correo.cl", "Password123!");

        Long categoryId = createCategory(adminToken, "Accesorios Compras", "Accesorios de oficina");
        Long branchId = createBranch(adminToken, "CMP-SUR", "Sucursal Compras Sur");
        Long productId = createProduct(adminToken, categoryId, "ACC-CMP-001", "Mouse Compras");

        PurchaseRequest request = new PurchaseRequest(
            "PO-2026-002",
            "Proveedor Office Ltda",
            LocalDate.of(2026, 6, 18),
            branchId,
            "Reposicion semanal",
            List.of(new PurchaseItemRequest(productId, 10, new BigDecimal("12.50")))
        );

        mockMvc.perform(post("/api/purchases")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/purchases/branch/{branchId}", branchId)
                .header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].purchaseNumber").value("PO-2026-002"));

        mockMvc.perform(post("/api/purchases")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new PurchaseRequest(
                        "PO-2026-003",
                        "Proveedor Bloqueado",
                        LocalDate.of(2026, 6, 17),
                        branchId,
                        null,
                        List.of(new PurchaseItemRequest(productId, 1, new BigDecimal("10.00")))
                    )
                )))
            .andExpect(status().isForbidden());
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
            "Producto para pruebas de compras",
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
            "Direccion compras 123",
            "Encargado Compras",
            "+56 9 2222 2222",
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
                    new com.multisucursal.inventory.auth.dto.RegisterRequest("Empleado Compras", email, password)
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
