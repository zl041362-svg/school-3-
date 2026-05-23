package com.zhhs.nong;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void profileShouldWorkAfterLogin() throws Exception {
        JsonNode customer = registerCustomer("13700000008");
        String token = login(customer.get("phone").asText(), "Abc123456");

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.phone").value(customer.get("phone").asText()))
                .andExpect(jsonPath("$.user.role").value("customer"));
    }

    @Test
    void disabledUserShouldBeBlockedOnNewLogin() throws Exception {
        JsonNode customer = registerCustomer("13700000009");
        String adminToken = issueAdminToken();

        mockMvc.perform(patch("/api/admin/users/" + customer.get("id").asLong())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "disabled"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", customer.get("phone").asText(),
                                "password", "Abc123456"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4030))
                .andExpect(jsonPath("$.message").value("user is disabled"));
    }

    @Test
    void unauthorizedShouldBeRejectedForProtectedTradeApis() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 1))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    void validationAndForbiddenErrorPayloadShouldContainCodeAndMessage() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", "",
                                "password", "Abc123456",
                                "role", "customer"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        JsonNode customer = registerCustomer("13700000010");
        String customerToken = login(customer.get("phone").asText(), "Abc123456");
        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("forbidden"));
    }

    @Test
    void customerCannotAccessAdminApis() throws Exception {
        JsonNode customer = registerCustomer("13700000002");
        String customerToken = login(customer.get("phone").asText(), "Abc123456");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden"));
    }
}
