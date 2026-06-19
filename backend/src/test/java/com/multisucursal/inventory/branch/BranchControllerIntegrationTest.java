package com.multisucursal.inventory.branch;

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
import com.multisucursal.inventory.branch.dto.BranchRequest;
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
class BranchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUpdateListAndDeleteBranchAsAdmin() throws Exception {
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");

        BranchRequest createRequest = new BranchRequest(
            "SCL-CENTRO",
            "Sucursal Centro",
            "Santiago",
            "Av. Libertador 123",
            "Maria Perez",
            "+56 9 1234 5678",
            "centro@multisucursal.dev",
            true
        );

        MvcResult createResult = mockMvc.perform(post("/api/branches")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SCL-CENTRO"))
            .andExpect(jsonPath("$.city").value("Santiago"))
            .andReturn();

        Long branchId = readId(createResult);

        BranchRequest updateRequest = new BranchRequest(
            "SCL-CENTRO",
            "Sucursal Centro Renovada",
            "Santiago",
            "Av. Providencia 456",
            "Ana Soto",
            "+56 9 9876 5432",
            "centro@multisucursal.dev",
            true
        );

        mockMvc.perform(put("/api/branches/{id}", branchId)
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Sucursal Centro Renovada"))
            .andExpect(jsonPath("$.address").value("Av. Providencia 456"));

        mockMvc.perform(get("/api/branches")
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("SCL-CENTRO"));

        mockMvc.perform(get("/api/branches/{id}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.managerName").value("Ana Soto"));

        mockMvc.perform(delete("/api/branches/{id}", branchId)
                .header("Authorization", bearer(adminToken)))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowEmployeeToReadButNotCreateBranch() throws Exception {
        registerEmployee("sucursal-empleado@correo.cl", "Password123!");
        String adminToken = loginAndGetToken("admin@multisucursal.dev", "Admin12345!");
        String employeeToken = loginAndGetToken("sucursal-empleado@correo.cl", "Password123!");

        BranchRequest createRequest = new BranchRequest(
            "VAL-NORTE",
            "Sucursal Norte",
            "Valparaiso",
            "Calle Norte 555",
            "Pedro Rojas",
            "+56 9 5555 5555",
            "norte@multisucursal.dev",
            true
        );

        MvcResult createResult = mockMvc.perform(post("/api/branches")
                .header("Authorization", bearer(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long branchId = readId(createResult);

        mockMvc.perform(get("/api/branches/{id}", branchId)
                .header("Authorization", bearer(employeeToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("VAL-NORTE"));

        mockMvc.perform(post("/api/branches")
                .header("Authorization", bearer(employeeToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    private void registerEmployee(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest("Empleado Sucursal", email, password);
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

