package com.zhhs.nong.controller.admin;

import com.zhhs.nong.common.CurrentUser;
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
import com.zhhs.nong.service.AccessControlService;
import com.zhhs.nong.service.FarmerManageService;
import com.zhhs.nong.service.NewsManageService;
import com.zhhs.nong.service.OperationLogService;
import com.zhhs.nong.service.ProductManageService;
import com.zhhs.nong.service.ReviewManageService;
import com.zhhs.nong.service.UserManageService;
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

    private final FarmerManageService farmerManageService;
    private final ReviewManageService reviewManageService;
    private final ProductManageService productManageService;
    private final NewsManageService newsManageService;
    private final UserManageService userManageService;
    private final AccessControlService accessControlService;
    private final OperationLogService operationLogService;

    public AdminController(FarmerManageService farmerManageService,
                           ReviewManageService reviewManageService,
                           ProductManageService productManageService,
                           NewsManageService newsManageService,
                           UserManageService userManageService,
                           AccessControlService accessControlService,
                           OperationLogService operationLogService) {
        this.farmerManageService = farmerManageService;
        this.reviewManageService = reviewManageService;
        this.productManageService = productManageService;
        this.newsManageService = newsManageService;
        this.userManageService = userManageService;
        this.accessControlService = accessControlService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/farmer-verifications")
    public Map<String, Object> farmerVerifications(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize) {
        return farmerManageService.getVerifications(page, pageSize);
    }

    @PostMapping("/farmer-verifications/{id}/review")
    public FarmerVerification reviewFarmerVerification(Authentication authentication,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody ReviewRequest request) {
        return farmerManageService.review(id, request.approved(), request.reason(), CurrentUser.operator(authentication));
    }

    @PostMapping("/farmer-verifications/batch")
    public void batchReviewFarmerVerifications(Authentication authentication,
                                               @Valid @RequestBody BatchReviewRequest request) {
        farmerManageService.batchReview(request, CurrentUser.operator(authentication));
    }

    @GetMapping("/product-reviews")
    public Map<String, Object> productReviews(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize) {
        return reviewManageService.getProductReviews(page, pageSize);
    }

    @PostMapping("/product-reviews/{id}/review")
    public ProductReview reviewProduct(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ReviewRequest request) {
        return reviewManageService.reviewProduct(id, request.approved(), request.reason(), CurrentUser.operator(authentication));
    }

    @PostMapping("/product-reviews/batch")
    public void batchReviewProducts(Authentication authentication,
                                    @Valid @RequestBody BatchReviewRequest request) {
        reviewManageService.batchReviewProducts(request, CurrentUser.operator(authentication));
    }

    @GetMapping("/news-reviews")
    public Map<String, Object> newsReviews(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize) {
        return reviewManageService.getNewsReviews(page, pageSize);
    }

    @PostMapping("/news-reviews/{id}/review")
    public NewsReview reviewNews(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewManageService.reviewNews(id, request.approved(), request.reason(), CurrentUser.operator(authentication));
    }

    @PostMapping("/news-reviews/batch")
    public void batchReviewNews(Authentication authentication,
                                @Valid @RequestBody BatchReviewRequest request) {
        reviewManageService.batchReviewNews(request, CurrentUser.operator(authentication));
    }

    @GetMapping("/products")
    public Map<String, Object> products(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer pageSize) {
        return productManageService.getProducts(page, pageSize);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody SaveProductAdminRequest request) {
        return productManageService.update(id, request, CurrentUser.operator(authentication));
    }

    @PatchMapping("/products/{id}/status")
    public Product updateProductStatus(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody StatusRequest request) {
        return productManageService.updateStatus(id, request.status(), CurrentUser.operator(authentication));
    }

    @GetMapping("/news")
    public Map<String, Object> news(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer pageSize) {
        return newsManageService.getNews(page, pageSize);
    }

    @PutMapping("/news/{id}")
    public News updateNews(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody SaveNewsAdminRequest request) {
        return newsManageService.update(id, request, CurrentUser.operator(authentication));
    }

    @PatchMapping("/news/{id}/status")
    public News updateNewsStatus(Authentication authentication,
                                 @PathVariable Long id,
                                 @Valid @RequestBody StatusRequest request) {
        return newsManageService.updateStatus(id, request.status(), CurrentUser.operator(authentication));
    }

    @GetMapping("/users")
    public Map<String, Object> users(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize) {
        return userManageService.getUsers(page, pageSize);
    }

    @PatchMapping("/users/{id}")
    public User updateUser(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateUserStatusRequest request) {
        return userManageService.updateStatus(id, request.status(), CurrentUser.operator(authentication));
    }

    @GetMapping("/roles")
    public Map<String, Object> roles(@RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize) {
        return accessControlService.getRoles(page, pageSize);
    }

    @PatchMapping("/roles/{id}")
    public Role updateRole(Authentication authentication,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateRoleMembersRequest request) {
        return accessControlService.updateRoleMembers(id, request.members(), CurrentUser.operator(authentication));
    }

    @PostMapping("/roles")
    public Role createRole(Authentication authentication,
                           @Valid @RequestBody RoleRequest request) {
        return accessControlService.createRole(request.role(), request.description(), CurrentUser.operator(authentication));
    }

    @PutMapping("/roles/{id}")
    public Role editRole(Authentication authentication,
                         @PathVariable Long id,
                         @Valid @RequestBody RoleRequest request) {
        return accessControlService.updateRole(id, request.role(), request.description(), CurrentUser.operator(authentication));
    }

    @DeleteMapping("/roles/{id}")
    public void deleteRole(Authentication authentication, @PathVariable Long id) {
        accessControlService.deleteRole(id, CurrentUser.operator(authentication));
    }

    @GetMapping("/permissions")
    public Map<String, Object> permissions(@RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer pageSize) {
        return accessControlService.getPermissions(page, pageSize);
    }

    @PostMapping("/permissions")
    public Permission createPermission(Authentication authentication,
                                       @Valid @RequestBody PermissionRequest request) {
        return accessControlService.createPermission(request.module(), request.action(), request.role(), CurrentUser.operator(authentication));
    }

    @PutMapping("/permissions/{id}")
    public Permission updatePermission(Authentication authentication,
                                       @PathVariable Long id,
                                       @Valid @RequestBody PermissionRequest request) {
        return accessControlService.updatePermission(id, request.module(), request.action(), request.role(), CurrentUser.operator(authentication));
    }

    @DeleteMapping("/permissions/{id}")
    public void deletePermission(Authentication authentication, @PathVariable Long id) {
        accessControlService.deletePermission(id, CurrentUser.operator(authentication));
    }

    @GetMapping("/logs")
    public Map<String, Object> logs(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer pageSize,
                                    @RequestParam(required = false) String operator,
                                    @RequestParam(required = false) String action,
                                    @RequestParam(required = false) String dateFrom,
                                    @RequestParam(required = false) String dateTo) {
        return operationLogService.getLogs(page, pageSize, operator, action, dateFrom, dateTo);
    }
}
