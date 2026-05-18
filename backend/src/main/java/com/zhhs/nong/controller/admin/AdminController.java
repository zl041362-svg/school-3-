package com.zhhs.nong.controller.admin;

import com.zhhs.nong.dto.admin.BatchReviewRequest;
import com.zhhs.nong.dto.admin.PermissionRequest;
import com.zhhs.nong.dto.admin.ReviewRequest;
import com.zhhs.nong.dto.admin.RoleRequest;
import com.zhhs.nong.dto.admin.SaveNewsAdminRequest;
import com.zhhs.nong.dto.admin.SaveProductAdminRequest;
import com.zhhs.nong.dto.admin.StatusRequest;
import com.zhhs.nong.dto.admin.UpdateRoleMembersRequest;
import com.zhhs.nong.dto.admin.UpdateUserStatusRequest;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.News;
import com.zhhs.nong.model.NewsReview;
import com.zhhs.nong.model.Permission;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductReview;
import com.zhhs.nong.model.Role;
import com.zhhs.nong.model.User;
import com.zhhs.nong.service.AdminService;
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
                                    @RequestParam(required = false) Integer pageSize,
                                    @RequestParam(required = false) String operator,
                                    @RequestParam(required = false) String action,
                                    @RequestParam(required = false) String dateFrom,
                                    @RequestParam(required = false) String dateTo) {
        return adminService.getLogs(page, pageSize, operator, action, dateFrom, dateTo);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody SaveProductAdminRequest request) {
        return adminService.updateProduct(id, request, operator(authentication));
    }

    @PatchMapping("/products/{id}/status")
    public Product updateProductStatus(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody StatusRequest request) {
        return adminService.updateProductStatus(id, request.status(), operator(authentication));
    }

    @PutMapping("/news/{id}")
    public News updateNews(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody SaveNewsAdminRequest request) {
        return adminService.updateNews(id, request, operator(authentication));
    }

    @PatchMapping("/news/{id}/status")
    public News updateNewsStatus(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody StatusRequest request) {
        return adminService.updateNewsStatus(id, request.status(), operator(authentication));
    }

    @PostMapping("/permissions")
    public Permission createPermission(Authentication authentication,
                                       @Valid @RequestBody PermissionRequest request) {
        return adminService.createPermission(request.module(), request.action(), request.role(), operator(authentication));
    }

    @PutMapping("/permissions/{id}")
    public Permission updatePermission(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody PermissionRequest request) {
        return adminService.updatePermission(id, request.module(), request.action(), request.role(), operator(authentication));
    }

    @DeleteMapping("/permissions/{id}")
    public void deletePermission(Authentication authentication, @PathVariable Long id) {
        adminService.deletePermission(id, operator(authentication));
    }

    @PostMapping("/roles")
    public Role createRole(Authentication authentication,
                           @Valid @RequestBody RoleRequest request) {
        return adminService.createRole(request.role(), request.description(), operator(authentication));
    }

    @DeleteMapping("/roles/{id}")
    public void deleteRole(Authentication authentication, @PathVariable Long id) {
        adminService.deleteRole(id, operator(authentication));
    }

    @PutMapping("/roles/{id}")
    public Role editRole(Authentication authentication,
                         @PathVariable Long id,
                         @Valid @RequestBody RoleRequest request) {
        return adminService.updateRole(id, request.role(), request.description(), operator(authentication));
    }

    @PostMapping("/product-reviews/batch")
    public void batchReviewProducts(Authentication authentication,
                                    @Valid @RequestBody BatchReviewRequest request) {
        adminService.batchReviewProducts(request, operator(authentication));
    }

    @PostMapping("/news-reviews/batch")
    public void batchReviewNews(Authentication authentication,
                                @Valid @RequestBody BatchReviewRequest request) {
        adminService.batchReviewNews(request, operator(authentication));
    }

    @PostMapping("/farmer-verifications/batch")
    public void batchReviewFarmerVerifications(Authentication authentication,
                                               @Valid @RequestBody BatchReviewRequest request) {
        adminService.batchReviewFarmerVerifications(request, operator(authentication));
    }

    private String operator(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "system";
        }
        return authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "").toLowerCase();
    }
}


