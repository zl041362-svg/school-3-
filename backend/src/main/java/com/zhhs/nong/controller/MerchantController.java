package com.zhhs.nong.controller;

import com.zhhs.nong.dto.merchant.SaveNewsRequest;
import com.zhhs.nong.dto.merchant.SaveProductRequest;
import com.zhhs.nong.dto.merchant.SubmitVerificationRequest;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.News;
import com.zhhs.nong.model.Order;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/verify")
    public FarmerVerification submitVerification(Authentication authentication,
                                                  @Valid @RequestBody SubmitVerificationRequest request) {
        return merchantService.submitVerification(userId(authentication), request);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return merchantService.getDashboard(userId(authentication));
    }

    @GetMapping("/products")
    public Map<String, Object> listProducts(Authentication authentication,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer pageSize) {
        return merchantService.getProducts(userId(authentication), page, pageSize);
    }

    @PostMapping("/products")
    public Product createProduct(Authentication authentication,
                                 @Valid @RequestBody SaveProductRequest request) {
        return merchantService.createProduct(userId(authentication), request);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody SaveProductRequest request) {
        return merchantService.updateProduct(userId(authentication), id, request);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(Authentication authentication, @PathVariable Long id) {
        merchantService.deleteProduct(userId(authentication), id);
    }

    @GetMapping("/news")
    public Map<String, Object> listNews(Authentication authentication,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer pageSize,
                                         @RequestParam(required = false) String keyword) {
        return merchantService.getNews(userId(authentication), page, pageSize, keyword);
    }

    @PostMapping("/news")
    public News createNews(Authentication authentication,
                           @Valid @RequestBody SaveNewsRequest request) {
        return merchantService.createNews(userId(authentication), request);
    }

    @PutMapping("/news/{id}")
    public News updateNews(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody SaveNewsRequest request) {
        return merchantService.updateNews(userId(authentication), id, request);
    }

    @DeleteMapping("/news/{id}")
    public void deleteNews(Authentication authentication, @PathVariable Long id) {
        merchantService.deleteNews(userId(authentication), id);
    }

    @GetMapping("/orders")
    public Map<String, Object> listOrders(Authentication authentication,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer pageSize) {
        return merchantService.getMerchantOrders(userId(authentication), page, pageSize);
    }

    @PatchMapping("/orders/{id}/ship")
    public Order shipOrder(Authentication authentication,
                           @PathVariable Long id,
                           @RequestBody Map<String, String> body) {
        return merchantService.shipOrder(userId(authentication), id, body.get("logistics"));
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(String.valueOf(authentication.getPrincipal()));
    }
}
