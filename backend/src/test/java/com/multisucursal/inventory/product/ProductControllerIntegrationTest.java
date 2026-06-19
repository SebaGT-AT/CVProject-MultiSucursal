package com.multisucursal.inventory.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multisucursal.inventory.auth.dto.LoginRequest;
import com.multisucursal.inventory.auth.dto.RegisterRequest;
import com.multisucursal.inventory.product.dto.CategoryRequest;
import com.multisucursal.inventory.product.dto.ProductRequest;
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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUpdateListAndDeleteCategoryAndProductAsAdmin() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");

        CategoryRequest categoryRequest = new CategoryRequest("Perifericos", "Accesorios de PC", true);
        MvcResult categoryResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Perifericos"))
            .andReturn();

        Long categoryId = readId(categoryResult);

        ProductRequest productRequest = new ProductRequest(
            "MOU-001",
            "Mouse Logitech",
            "Mouse inalambrico",
            new BigDecimal("12.50"),
            new BigDecimal("19.90"),
            true,
            categoryId
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value("MOU-001"))
            .andExpect(jsonPath("$.categoryId").value(categoryId))
            .andReturn();

        Long productId = readId(productResult);

        ProductRequest updateRequest = new ProductRequest(
            "MOU-001",
            "Mouse Logitech MX",
            "Mouse actualizado",
            new BigDecimal("13.00"),
            new BigDecimal("21.50"),
            true,
            categoryId
        );

        mockMvc.perform(put("/api/products/{id}", productId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mouse Logitech MX"))
            .andExpect(jsonPath("$.salePrice").value(21.50));

        mockMvc.perform(get("/api/categories")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].productCount").value(1));

        mockMvc.perform(get("/api/products/{id}", productId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categoryName").value("Perifericos"));

        mockMvc.perform(delete("/api/products/{id}", productId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldForbidEmployeeFromCreatingProducts() throws Exception {
        registerEmployee("empleado@correo.cl", "Password123!");
        String employeeToken = loginAndGetToken("empleado@correo.cl", "Password123!");

        ProductRequest request = new ProductRequest(
            "KEY-001",
            "Teclado",
            "Teclado mecanico",
            new BigDecimal("20.00"),
            new BigDecimal("35.00"),
            true,
            1L
        );

        mockMvc.perform(post("/api/products")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    private void registerEmployee(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest("Empleado Demo", email, password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
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

