package com.zhhs.nong;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TradeFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    void publicListApisShouldWork() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].farmer").isString());
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].title").isString());
    }

    @Test
    void customerTradeFlowShouldWork() throws Exception {
        JsonNode register = registerCustomer("13700000000");
        String token = login(register.get("phone").asText(), "Abc123456");

        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("五常有机大米"));

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].qty").value(2));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "张三",
                                "phone", "13800000000",
                                "address", "黑龙江省哈尔滨市道里区中央大街 88 号",
                                "items", List.of(Map.of("productId", 1, "qty", 2))
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
                .andExpect(jsonPath("$.items[0].name").value("五常有机大米"));
    }

    @Test
    void soldOutProductCannotBeAddedToCart() throws Exception {
        JsonNode customer = registerCustomer("13700000003");
        String token = login(customer.get("phone").asText(), "Abc123456");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 2, "qty", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("product is sold out"));
    }

    @Test
    void creatingOrderWithEmptyCartShouldFail() throws Exception {
        JsonNode customer = registerCustomer("13700000007");
        String token = login(customer.get("phone").asText(), "Abc123456");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "赵六",
                                "phone", "13800000015",
                                "address", "广州市天河区 8 号",
                                "items", List.of(Map.of("productId", 1, "qty", 1))
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("cart is empty"));
    }

    @Test
    void deletingDefaultAddressShouldPromoteAnotherDefault() throws Exception {
        JsonNode customer = registerCustomer("13700000004");
        String token = login(customer.get("phone").asText(), "Abc123456");

        String defaultAddressResp = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "王二",
                                "phone", "13800000012",
                                "address", "北京市海淀区 2 号",
                                "isDefault", false
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/addresses/" + firstId)
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
        String tokenA = login(customerA.get("phone").asText(), "Abc123456");

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 1))))
                .andExpect(status().isOk());

        String orderResp = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "receiver", "张三",
                                "phone", "13800000013",
                                "address", "上海市浦东新区 3 号",
                                "items", List.of(Map.of("productId", 1, "qty", 1))
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long orderId = objectMapper.readTree(orderResp).path("id").asLong();

        JsonNode customerB = registerCustomer("13700000006");
        String tokenB = login(customerB.get("phone").asText(), "Abc123456");

        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("order not found"));
    }

    @Test
    void cartAndAddressCrudShouldWorkForLoggedInUser() throws Exception {
        JsonNode customer = registerCustomer("13700000011");
        String token = login(customer.get("phone").asText(), "Abc123456");

        String cartItemResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long cartItemId = objectMapper.readTree(cartItemResponse).path("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("qty", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qty").value(3));

        mockMvc.perform(delete("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String addressResponse = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
        String tokenA = login(userA.get("phone").asText(), "Abc123456");

        String cartItemResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", 1, "qty", 1))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long cartItemId = objectMapper.readTree(cartItemResponse).path("id").asLong();

        String addressResponse = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
        String tokenB = login(userB.get("phone").asText(), "Abc123456");

        mockMvc.perform(put("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("qty", 2))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("cart item not found"));

        mockMvc.perform(delete("/api/cart/items/" + cartItemId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("cart item not found"));

        mockMvc.perform(put("/api/addresses/" + addressId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
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
}
