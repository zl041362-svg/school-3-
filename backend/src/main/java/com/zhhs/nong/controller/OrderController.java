package com.zhhs.nong.controller;

import com.zhhs.nong.common.CurrentUser;
import com.zhhs.nong.dto.trade.CreateOrderRequest;
import com.zhhs.nong.model.Order;
import com.zhhs.nong.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Map<String, Object> list(Authentication authentication,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize) {
        return orderService.getOrders(CurrentUser.id(authentication), page, pageSize);
    }

    @GetMapping("/{id}")
    public Order detail(Authentication authentication, @PathVariable Long id) {
        return orderService.getDetail(CurrentUser.id(authentication), id);
    }

    @PostMapping
    public Order create(Authentication authentication, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(CurrentUser.id(authentication), request);
    }

    @PostMapping("/{id}/confirm-receipt")
    public Order confirmReceipt(Authentication authentication, @PathVariable Long id) {
        return orderService.confirmReceipt(CurrentUser.id(authentication), id);
    }
}


