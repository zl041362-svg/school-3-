package com.zhhs.nong;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.User;
import com.zhhs.nong.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtService jwtService;
    @Test
    void publicListApisShouldWork() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].farmer").value("赣南果园合作社"));
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].title").isString());
    }
    @Test
    void customerTradeFlowShouldWork() throws Exception {
        JsonNode register = registerCustomer("13700000000");
        String token = login(register.get("phone").asText(), "123456");
        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "张三",
                                "phone", "13800000000",
                                "address", "黑龙江省哈尔滨市道里区中央大街 88 号",
                                "isDefault", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiver").value("张三"));
        mockMvc.perform(get("/api/addresses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].isDefault").value(1));
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", 1,
                                "qty", 2
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wuchang Rice"));
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].qty").value(2));
        String orderResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "张三",
                                "phone", "13800000000",
                                "address", "黑龙江省哈尔滨市道里区中央大街 88 号",
                                "items", java.util.List.of(Map.of("productId", 1, "qty", 2))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("pending_shipment"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode orderNode = objectMapper.readTree(orderResponse);
        long orderId = orderNode.get("id").asLong();
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].id").value((int) orderId));
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].name").value("Wuchang Rice"));
    }
    @Test
    void adminModerationFlowShouldWork() throws Exception {
        JsonNode tempUser = registerCustomer("13700000001");
        String adminToken = issueAdminToken();
        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].farmer").value("赣南果园合作社"));
        mockMvc.perform(get("/api/admin/farmer-verifications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));
        mockMvc.perform(post("/api/admin/farmer-verifications/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "approved", true,
                                "reason", "资料完整"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
        mockMvc.perform(get("/api/admin/product-reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));
        mockMvc.perform(post("/api/admin/product-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "approved", true,
                                "reason", "通过"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
        mockMvc.perform(get("/api/admin/news-reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].status").value("pending"));
        mockMvc.perform(post("/api/admin/news-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "approved", false,
                                "reason", "内容需补充"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejected"));
        mockMvc.perform(patch("/api/admin/users/" + tempUser.get("id").asLong())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "disabled"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disabled"));
        mockMvc.perform(patch("/api/admin/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("members", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members").value(2));
        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void customerCannotAccessAdminApis() throws Exception {
        JsonNode customer = registerCustomer("13700000002");
        String customerToken = login(customer.get("phone").asText(), "123456");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden"));
    }

    @Test
    void soldOutProductCannotBeAddedToCart() throws Exception {
        JsonNode customer = registerCustomer("13700000003");
        String token = login(customer.get("phone").asText(), "123456");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", 2,
                                "qty", 1
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("product is sold out"));
    }

    @Test
    void creatingOrderWithEmptyCartShouldFail() throws Exception {
        JsonNode customer = registerCustomer("13700000007");
        String token = login(customer.get("phone").asText(), "123456");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "赵六",
                                "phone", "13800000015",
                                "address", "广州市天河区 8 号",
                                "items", java.util.List.of(Map.of("productId", 1, "qty", 1))
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("cart is empty"));
    }

    @Test
    void deletingDefaultAddressShouldPromoteAnotherDefault() throws Exception {
        JsonNode customer = registerCustomer("13700000004");
        String token = login(customer.get("phone").asText(), "123456");

        String defaultAddressResp = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "王一",
                                "phone", "13800000011",
                                "address", "北京市朝阳区 1 号",
                                "isDefault", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long firstId = objectMapper.readTree(defaultAddressResp).path("id").asLong();

        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "王二",
                                "phone", "13800000012",
                                "address", "北京市海淀区 2 号",
                                "isDefault", false
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/addresses/" + firstId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/addresses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].isDefault").value(1));
    }

    @Test
    void orderDetailShouldBeIsolatedByUser() throws Exception {
        JsonNode customerA = registerCustomer("13700000005");
        String tokenA = login(customerA.get("phone").asText(), "123456");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", 1,
                                "qty", 1
                        ))))
                .andExpect(status().isOk());

        String orderResp = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "张三",
                                "phone", "13800000013",
                                "address", "上海市浦东新区 3 号",
                                "items", java.util.List.of(Map.of("productId", 1, "qty", 1))
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long orderId = objectMapper.readTree(orderResp).path("id").asLong();

        JsonNode customerB = registerCustomer("13700000006");
        String tokenB = login(customerB.get("phone").asText(), "123456");

        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("order not found"));
    }

    @Test
    void profileShouldWorkAfterLogin() throws Exception {
        JsonNode customer = registerCustomer("13700000008");
        String token = login(customer.get("phone").asText(), "123456");

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "disabled"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("disabled"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", customer.get("phone").asText(),
                                "password", "123456"
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 1))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "A",
                                "phone", "13800000000",
                                "address", "B"
                        ))))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", "",
                                "password", "123456",
                                "role", "customer"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        JsonNode customer = registerCustomer("13700000010");
        String customerToken = login(customer.get("phone").asText(), "123456");
        mockMvc.perform(get("/api/admin/logs")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("forbidden"));
    }

    @Test
    void cartAndAddressCrudShouldWorkForLoggedInUser() throws Exception {
        JsonNode customer = registerCustomer("13700000011");
        String token = login(customer.get("phone").asText(), "123456");

        String cartItemResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", 1,
                                "qty", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long cartItemId = objectMapper.readTree(cartItemResponse).path("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("qty", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qty").value(3));

        mockMvc.perform(delete("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String addressResponse = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "李雷",
                                "phone", "13800000021",
                                "address", "成都市高新区 88 号",
                                "isDefault", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiver").value("李雷"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long addressId = objectMapper.readTree(addressResponse).path("id").asLong();

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "李雷-更新",
                                "phone", "13800000022",
                                "address", "成都市高新区 99 号",
                                "isDefault", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiver").value("李雷-更新"));

        mockMvc.perform(delete("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void cartAndAddressShouldBeIsolatedByUser() throws Exception {
        JsonNode userA = registerCustomer("13700000012");
        String tokenA = login(userA.get("phone").asText(), "123456");

        String cartItemResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", 1,
                                "qty", 1
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long cartItemId = objectMapper.readTree(cartItemResponse).path("id").asLong();

        String addressResponse = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "甲",
                                "phone", "13800000031",
                                "address", "南京市玄武区 1 号",
                                "isDefault", true
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long addressId = objectMapper.readTree(addressResponse).path("id").asLong();

        JsonNode userB = registerCustomer("13700000013");
        String tokenB = login(userB.get("phone").asText(), "123456");

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("qty", 2))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("cart item not found"));

        mockMvc.perform(delete("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("cart item not found"));

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "乙",
                                "phone", "13800000032",
                                "address", "南京市玄武区 2 号",
                                "isDefault", false
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("address not found"));

        mockMvc.perform(delete("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("address not found"));
    }

    @Test
    void adminShouldRejectInvalidPayloads() throws Exception {
        String adminToken = issueAdminToken();

        mockMvc.perform(patch("/api/admin/users/2")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "paused"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        mockMvc.perform(patch("/api/admin/roles/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("members", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isString());

        mockMvc.perform(post("/api/admin/product-reviews/1/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("verification not found"));

        mockMvc.perform(post("/api/admin/product-reviews/999/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("product review not found"));

        mockMvc.perform(post("/api/admin/news-reviews/999/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("approved", true, "reason", "x"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("news review not found"));
    }

    private JsonNode registerCustomer(String phone) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", phone,
                                "password", "123456",
                                "role", "customer"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(phone))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }
    private String login(String phone, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "phone", phone,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("token").asText();
    }

    private String issueAdminToken() {
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getRole, "admin").last("limit 1"));
        if (admin == null) {
            throw new IllegalStateException("seed admin user not found");
        }
        return jwtService.createToken(admin.getId(), admin.getRole(), admin.getPhone());
    }
}