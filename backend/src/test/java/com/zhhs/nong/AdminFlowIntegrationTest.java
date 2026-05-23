package com.zhhs.nong;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void adminModerationFlowShouldWork() throws Exception {
        JsonNode tempUser = registerCustomer("13700000001");
        String adminToken = issueAdminToken();

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].farmer").isString());

        mockMvc.perform(get("/api/admin/farmer-verifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));

        mockMvc.perform(post("/api/admin/farmer-verifications/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "资料完整"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));

        mockMvc.perform(get("/api/admin/product-reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));

        mockMvc.perform(post("/api/admin/product-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "通过"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));

        mockMvc.perform(get("/api/admin/news-reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));

        mockMvc.perform(post("/api/admin/news-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", false, "reason", "内容需补充"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejected"));

        mockMvc.perform(patch("/api/admin/users/" + tempUser.get("id").asLong())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "disabled"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disabled"));

        mockMvc.perform(patch("/api/admin/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("members", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").value(2));

        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void adminShouldRejectInvalidPayloads() throws Exception {
        String adminToken = issueAdminToken();

        mockMvc.perform(patch("/api/admin/users/2")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "paused"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        mockMvc.perform(patch("/api/admin/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("members", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        mockMvc.perform(post("/api/admin/product-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "missing approved"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void adminReviewMissingResourceShouldReturnNotFound() throws Exception {
        String adminToken = issueAdminToken();

        mockMvc.perform(post("/api/admin/farmer-verifications/999/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("verification not found"));

        mockMvc.perform(post("/api/admin/product-reviews/999/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("product review not found"));

        mockMvc.perform(post("/api/admin/news-reviews/999/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("news review not found"));
    }
}
