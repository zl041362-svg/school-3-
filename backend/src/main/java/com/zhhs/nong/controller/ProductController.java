package com.zhhs.nong.controller;
import com.zhhs.nong.common.CurrentUser;
import com.zhhs.nong.dto.trade.CreateEvaluationRequest;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductEvaluation;
import com.zhhs.nong.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping("/{id}/evaluations")
    public Map<String, Object> evaluations(@PathVariable Long id,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize) {
        return productService.getEvaluations(id, page, pageSize);
    }
    @GetMapping("/{id}/can-review")
    public Map<String, Object> canReview(Authentication authentication, @PathVariable Long id) {
        return Map.of("canReview", productService.canReview(CurrentUser.id(authentication), id));
    }
    @PostMapping("/{id}/evaluations")
    public ProductEvaluation createEvaluation(Authentication authentication,
                                               @PathVariable Long id,
                                               @Valid @RequestBody CreateEvaluationRequest request) {
        return productService.createEvaluation(CurrentUser.id(authentication), id, request);
    }
    @DeleteMapping("/evaluations/{id}")
    public void deleteEvaluation(Authentication authentication, @PathVariable Long id) {
        productService.deleteEvaluation(CurrentUser.id(authentication), id);
    }

    @GetMapping("/evaluations/my")
    public Map<String, Object> myEvaluations(Authentication authentication,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
        return productService.getMyEvaluations(CurrentUser.id(authentication), page, pageSize);
    }
}
