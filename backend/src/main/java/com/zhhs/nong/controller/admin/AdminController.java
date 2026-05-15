package com.zhhs.nong.controller.admin;

import com.zhhs.nong.dto.admin.ReviewRequest;
import com.zhhs.nong.dto.admin.UpdateRoleMembersRequest;
import com.zhhs.nong.dto.admin.UpdateUserStatusRequest;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.NewsReview;
import com.zhhs.nong.model.ProductReview;
import com.zhhs.nong.model.Role;
import com.zhhs.nong.model.User;
import com.zhhs.nong.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/farmer-verifications")
    public Map<String, Object> farmerVerifications(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize) {
        return adminService.getFarmerVerifications(page, pageSize);
    }

    @PostMapping("/farmer-verifications/{id}/review")
    public FarmerVerification reviewFarmerVerification(Authentication authentication,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody ReviewRequest request) {
        return adminService.reviewFarmerVerification(id, request.approved(), request.reason(), operator(authentication));
    }

    @GetMapping("/product-reviews")
    public Map<String, Object> productReviews(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
        return adminService.getProductReviews(page, pageSize);
    }

    @PostMapping("/product-reviews/{id}/review")
    public ProductReview reviewProduct(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ReviewRequest request) {
        return adminService.reviewProduct(id, request.approved(), request.reason(), operator(authentication));
    }

    @GetMapping("/news-reviews")
    public Map<String, Object> newsReviews(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize) {
        return adminService.getNewsReviews(page, pageSize);
    }

    @PostMapping("/news-reviews/{id}/review")
    public NewsReview reviewNews(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request) {
        return adminService.reviewNews(id, request.approved(), request.reason(), operator(authentication));
    }

    @GetMapping("/products")
    public Map<String, Object> products(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer pageSize) {
        return adminService.getProducts(page, pageSize);
    }

    @GetMapping("/news")
    public Map<String, Object> news(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer pageSize) {
        return adminService.getNews(page, pageSize);
    }

    @GetMapping("/users")
    public Map<String, Object> users(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize) {
        return adminService.getUsers(page, pageSize);
    }

    @PatchMapping("/users/{id}")
    public User updateUser(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminService.updateUserStatus(id, request.status(), operator(authentication));
    }

    @GetMapping("/roles")
    public Map<String, Object> roles(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize) {
        return adminService.getRoles(page, pageSize);
    }

    @PatchMapping("/roles/{id}")
    public Role updateRole(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateRoleMembersRequest request) {
        return adminService.updateRoleMembers(id, request.members(), operator(authentication));
    }

    @GetMapping("/permissions")
    public Map<String, Object> permissions(@RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer pageSize) {
        return adminService.getPermissions(page, pageSize);
    }

    @GetMapping("/logs")
    public Map<String, Object> logs(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer pageSize) {
        return adminService.getLogs(page, pageSize);
    }

    private String operator(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "admin";
        }
        return authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "").toLowerCase();
    }
}


