package com.multisucursal.inventory.stock;

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
import com.multisucursal.inventory.stock.dto.AdjustmentType;
import com.multisucursal.inventory.stock.dto.StockAdjustmentRequest;
import com.multisucursal.inventory.stock.dto.StockTransferRequest;
import java.math.BigDecimal;
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
class StockControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAdjustTransferAndListStockAsAdmin() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");

        Long categoryId = createCategory(adminToken, "Monitores", "Pantallas");
        Long productId = createProduct(adminToken, categoryId, "MON-001", "Monitor Samsung");
        Long sourceBranchId = createBranch(adminToken, "SCL-CENTRO", "Sucursal Centro");
        Long targetBranchId = createBranch(adminToken, "VAP-NORTE", "Sucursal Norte");

        StockAdjustmentRequest initialLoad = new StockAdjustmentRequest(
            sourceBranchId,
            productId,
            20,
            AdjustmentType.INITIAL_LOAD,
            5,
            "INIT-001",
            "Initial stock load"
        );

        mockMvc.perform(post("/api/stocks/adjustments")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialLoad)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(20))
            .andExpect(jsonPath("$.minimumStock").value(5));

        StockAdjustmentRequest decrease = new StockAdjustmentRequest(
            sourceBranchId,
            productId,
            3,
            AdjustmentType.DECREASE,
            5,
            "ADJ-001",
            "Damaged units"
        );

        mockMvc.perform(post("/api/stocks/adjustments")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decrease)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(17));

        StockTransferRequest transferRequest = new StockTransferRequest(
            sourceBranchId,
            targetBranchId,
            productId,
            7,
            "TRF-001",
            "Rebalance stock"
        );

        mockMvc.perform(post("/api/stocks/transfers")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/stocks/branch/{branchId}", sourceBranchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].quantity").value(10));

        mockMvc.perform(get("/api/stocks/branch/{branchId}", targetBranchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].quantity").value(7));

        mockMvc.perform(get("/api/stocks/movements/product/{productId}", productId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void shouldAllowEmployeeToReadStockButNotModifyIt() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        String employeeToken = registerAndLoginEmployee("stock-empleado@correo.cl", "Password123!");

        Long categoryId = createCategory(adminToken, "Teclados", "Perifericos de entrada");
        Long productId = createProduct(adminToken, categoryId, "KEY-001", "Teclado Mecanico");
        Long branchId = createBranch(adminToken, "TEM-SUR", "Sucursal Sur");

        StockAdjustmentRequest initialLoad = new StockAdjustmentRequest(
            branchId,
            productId,
            8,
            AdjustmentType.INITIAL_LOAD,
            2,
            "INIT-EMP",
            "Employee visibility test"
        );

        mockMvc.perform(post("/api/stocks/adjustments")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialLoad)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/stocks/branch/{branchId}", branchId)
                .header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].quantity").value(8));

        mockMvc.perform(post("/api/stocks/transfers")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new StockTransferRequest(
                    branchId, branchId + 1, productId, 1, "FAIL-001", "Should be forbidden"
                ))))
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
            "Monitor 24 pulgadas",
            new BigDecimal("100.00"),
            new BigDecimal("149.90"),
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
            "Direccion demo 123",
            "Gerente Demo",
            "+56 9 1111 1111",
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
                    new com.multisucursal.inventory.auth.dto.RegisterRequest("Empleado Stock", email, password)
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
