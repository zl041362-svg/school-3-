package com.zhhs.nong.controller;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        return productService.list(keyword, category, region, page, pageSize);
    }
    @GetMapping("/{id}")
    public Product detail(@PathVariable Long id) {
        return productService.detail(id);
    }
}
