package com.zhhs.nong.controller;

import com.zhhs.nong.dto.trade.AddCartItemRequest;
import com.zhhs.nong.dto.trade.UpdateCartQtyRequest;
import com.zhhs.nong.model.CartItem;
import com.zhhs.nong.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Map<String, Object> getCart(Authentication authentication) {
        return cartService.getCart(userId(authentication));
    }

    @PostMapping("/items")
    public CartItem addItem(Authentication authentication, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(userId(authentication), request);
    }

    @PutMapping("/items/{id}")
    public CartItem updateQty(Authentication authentication,
                              @PathVariable Long id,
                              @Valid @RequestBody UpdateCartQtyRequest request) {
        return cartService.updateQty(userId(authentication), id, request);
    }

    @DeleteMapping("/items/{id}")
    public void removeItem(Authentication authentication, @PathVariable Long id) {
        cartService.removeItem(userId(authentication), id);
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(String.valueOf(authentication.getPrincipal()));
    }
}

