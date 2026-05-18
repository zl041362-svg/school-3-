package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.admin.BatchReviewRequest;
import com.zhhs.nong.dto.admin.SaveNewsAdminRequest;
import com.zhhs.nong.dto.admin.SaveProductAdminRequest;
import com.zhhs.nong.mapper.FarmerVerificationMapper;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.mapper.NewsReviewMapper;
import com.zhhs.nong.mapper.OperationLogMapper;
import com.zhhs.nong.mapper.PermissionMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.mapper.ProductReviewMapper;
import com.zhhs.nong.mapper.RoleMapper;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.News;
import com.zhhs.nong.model.NewsReview;
import com.zhhs.nong.model.OperationLog;
import com.zhhs.nong.model.Permission;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductReview;
import com.zhhs.nong.model.Role;
import com.zhhs.nong.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final FarmerVerificationMapper farmerVerificationMapper;
    private final ProductReviewMapper productReviewMapper;
    private final NewsReviewMapper newsReviewMapper;
    private final ProductMapper productMapper;
    private final NewsMapper newsMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final OperationLogMapper operationLogMapper;

    public AdminService(FarmerVerificationMapper farmerVerificationMapper,
                        ProductReviewMapper productReviewMapper,
                        NewsReviewMapper newsReviewMapper,
                        ProductMapper productMapper,
                        NewsMapper newsMapper,
                        UserMapper userMapper,
                        RoleMapper roleMapper,
                        PermissionMapper permissionMapper,
                        OperationLogMapper operationLogMapper) {
        this.farmerVerificationMapper = farmerVerificationMapper;
        this.productReviewMapper = productReviewMapper;
        this.newsReviewMapper = newsReviewMapper;
        this.productMapper = productMapper;
        this.newsMapper = newsMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFarmerVerifications(Integer page, Integer pageSize) {
        List<FarmerVerification> all = farmerVerificationMapper.selectList(new LambdaQueryWrapper<FarmerVerification>()
                .orderByDesc(FarmerVerification::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductReviews(Integer page, Integer pageSize) {
        List<ProductReview> all = productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .orderByDesc(ProductReview::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNewsReviews(Integer page, Integer pageSize) {
        List<NewsReview> all = newsReviewMapper.selectList(new LambdaQueryWrapper<NewsReview>()
                .orderByDesc(NewsReview::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProducts(Integer page, Integer pageSize) {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNews(Integer page, Integer pageSize) {
        List<News> all = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .orderByDesc(News::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsers(Integer page, Integer pageSize) {
        List<User> all = userMapper.selectList(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoles(Integer page, Integer pageSize) {
        List<Role> all = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .orderByDesc(Role::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPermissions(Integer page, Integer pageSize) {
        List<Permission> all = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .orderByDesc(Permission::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLogs(Integer page, Integer pageSize) {
        List<OperationLog> all = operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getId));
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional
    public FarmerVerification reviewFarmerVerification(Long id, boolean approved, String reason, String operator) {
        FarmerVerification verification = requireFarmerVerification(id);
        verification.setStatus(approved ? "approved" : "rejected");
        verification.setReason(reason);
        verification.setReviewedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        farmerVerificationMapper.updateById(verification);

        if (approved && verification.getUserId() != null) {
            User user = userMapper.selectById(verification.getUserId());
            if (user != null) {
                user.setRole("farmer");
                user.setStatus("active");
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        log(operator, "review_farmer_verification", "审核农户认证 " + id + " -> " + verification.getStatus());
        return verification;
    }

    @Transactional
    public ProductReview reviewProduct(Long id, boolean approved, String reason, String operator) {
        ProductReview review = requireProductReview(id);
        review.setStatus(approved ? "approved" : "rejected");
        review.setReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        productReviewMapper.updateById(review);

        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            product.setStatus(approved ? "published" : "rejected");
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
        }

        log(operator, "review_product", "审核商品 " + id + " -> " + review.getStatus());
        return review;
    }

    @Transactional
    public NewsReview reviewNews(Long id, boolean approved, String reason, String operator) {
        NewsReview review = requireNewsReview(id);
        review.setStatus(approved ? "approved" : "rejected");
        review.setReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        newsReviewMapper.updateById(review);

        News news = newsMapper.selectById(review.getNewsId());
        if (news != null) {
            news.setStatus(approved ? "published" : "rejected");
            news.setUpdatedAt(LocalDateTime.now());
            if (approved && news.getPublishedAt() == null) {
                news.setPublishedAt(LocalDateTime.now());
            }
            newsMapper.updateById(news);
        }

        log(operator, "review_news", "审核资讯 " + id + " -> " + review.getStatus());
        return review;
    }

    @Transactional
    public User updateUserStatus(Long id, String status, String operator) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(4040, "user not found");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log(operator, "update_user_status", "用户 " + id + " 状态更新为 " + status);
        return user;
    }

    @Transactional
    public Role updateRoleMembers(Long id, Integer members, String operator) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(4046, "role not found");
        }
        role.setMembers(members);
        roleMapper.updateById(role);
        log(operator, "update_role_members", "角色 " + role.getRole() + " 成员数更新为 " + members);
        return role;
    }

    @Transactional
    public Product updateProduct(Long id, SaveProductAdminRequest request, String operator) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        product.setName(request.name());
        if (request.category() != null) product.setCategory(request.category());
        if (request.region() != null) product.setRegion(request.region());
        product.setPrice(request.price());
        product.setStock(request.stock());
        if (request.summary() != null) product.setSummary(request.summary());
        if (request.description() != null) product.setDescription(request.description());
        if (request.spec() != null) product.setSpec(request.spec());
        if (request.qualification() != null) product.setQualification(request.qualification());
        if (request.farmer() != null) product.setFarmer(request.farmer());
        if (request.status() != null) product.setStatus(request.status());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        log(operator, "update_product", "编辑商品 " + id);
        return product;
    }

    @Transactional
    public Product updateProductStatus(Long id, String status, String operator) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        log(operator, "update_product_status", "商品 " + id + " 状态更新为 " + status);
        return product;
    }

    @Transactional
    public News updateNews(Long id, SaveNewsAdminRequest request, String operator) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        news.setTitle(request.title());
        if (request.category() != null) news.setCategory(request.category());
        if (request.summary() != null) news.setSummary(request.summary());
        if (request.content() != null) news.setContent(request.content());
        if (request.author() != null) news.setAuthor(request.author());
        if (request.status() != null) news.setStatus(request.status());
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
        log(operator, "update_news", "编辑资讯 " + id);
        return news;
    }

    @Transactional
    public News updateNewsStatus(Long id, String status, String operator) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        news.setStatus(status);
        if ("published".equals(status) && news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);
        log(operator, "update_news_status", "资讯 " + id + " 状态更新为 " + status);
        return news;
    }

    @Transactional
    public Permission createPermission(String module, String action, String role, String operator) {
        Permission permission = new Permission();
        permission.setModule(module);
        permission.setAction(action);
        permission.setRole(role);
        permissionMapper.insert(permission);
        log(operator, "create_permission", "新增权限 " + module + "/" + action + " -> " + role);
        return permission;
    }

    @Transactional
    public Permission updatePermission(Long id, String module, String action, String role, String operator) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException(4050, "permission not found");
        }
        permission.setModule(module);
        permission.setAction(action);
        permission.setRole(role);
        permissionMapper.updateById(permission);
        log(operator, "update_permission", "编辑权限 " + id);
        return permission;
    }

    @Transactional
    public void deletePermission(Long id, String operator) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException(4050, "permission not found");
        }
        permissionMapper.deleteById(id);
        log(operator, "delete_permission", "删除权限 " + id);
    }

    @Transactional
    public Role createRole(String role, String description, String operator) {
        Role r = new Role();
        r.setRole(role);
        r.setDescription(description);
        r.setMembers(0);
        roleMapper.insert(r);
        log(operator, "create_role", "新增角色 " + role);
        return r;
    }

    @Transactional
    public void deleteRole(Long id, String operator) {
        Role r = roleMapper.selectById(id);
        if (r == null) {
            throw new BizException(4046, "role not found");
        }
        roleMapper.deleteById(id);
        log(operator, "delete_role", "删除角色 " + r.getRole());
    }

    @Transactional
    public void batchReviewProducts(BatchReviewRequest request, String operator) {
        for (Long id : request.ids()) {
            ProductReview review = requireProductReview(id);
            review.setStatus(request.approved() ? "approved" : "rejected");
            review.setReason(request.reason());
            review.setUpdatedAt(LocalDateTime.now());
            productReviewMapper.updateById(review);

            Product product = productMapper.selectById(review.getProductId());
            if (product != null) {
                product.setStatus(request.approved() ? "published" : "rejected");
                product.setUpdatedAt(LocalDateTime.now());
                productMapper.updateById(product);
            }
        }
        log(operator, "batch_review_products", "批量审核商品 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
    }

    @Transactional
    public void batchReviewNews(BatchReviewRequest request, String operator) {
        for (Long id : request.ids()) {
            NewsReview review = requireNewsReview(id);
            review.setStatus(request.approved() ? "approved" : "rejected");
            review.setReason(request.reason());
            review.setUpdatedAt(LocalDateTime.now());
            newsReviewMapper.updateById(review);

            News news = newsMapper.selectById(review.getNewsId());
            if (news != null) {
                news.setStatus(request.approved() ? "published" : "rejected");
                if (request.approved() && news.getPublishedAt() == null) {
                    news.setPublishedAt(LocalDateTime.now());
                }
                news.setUpdatedAt(LocalDateTime.now());
                newsMapper.updateById(news);
            }
        }
        log(operator, "batch_review_news", "批量审核资讯 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
    }

    @Transactional
    public void batchReviewFarmerVerifications(BatchReviewRequest request, String operator) {
        for (Long id : request.ids()) {
            FarmerVerification verification = requireFarmerVerification(id);
            verification.setStatus(request.approved() ? "approved" : "rejected");
            verification.setReason(request.reason());
            verification.setReviewedAt(LocalDateTime.now());
            verification.setUpdatedAt(LocalDateTime.now());
            farmerVerificationMapper.updateById(verification);

            if (request.approved() && verification.getUserId() != null) {
                User user = userMapper.selectById(verification.getUserId());
                if (user != null) {
                    user.setRole("farmer");
                    user.setStatus("active");
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateById(user);
                }
            }
        }
        log(operator, "batch_review_farmers", "批量审核认证 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLogs(Integer page, Integer pageSize, String operator, String action, String dateFrom, String dateTo) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getId);

        if (StringUtils.hasText(operator)) {
            wrapper.eq(OperationLog::getOperator, operator);
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(OperationLog::getAction, action);
        }
        if (StringUtils.hasText(dateFrom)) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(dateFrom + "T00:00:00"));
        }
        if (StringUtils.hasText(dateTo)) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(dateTo + "T23:59:59"));
        }

        List<OperationLog> all = operationLogMapper.selectList(wrapper);
        return pageResponse(slice(all, page, pageSize), all.size(), page, pageSize);
    }

    @Transactional
    public Role updateRole(Long id, String roleName, String description, String operator) {
        Role r = roleMapper.selectById(id);
        if (r == null) {
            throw new BizException(4046, "role not found");
        }
        if (description != null) r.setDescription(description);
        roleMapper.updateById(r);
        log(operator, "update_role", "编辑角色 " + r.getRole());
        return r;
    }

    private FarmerVerification requireFarmerVerification(Long id) {
        FarmerVerification verification = farmerVerificationMapper.selectById(id);
        if (verification == null) {
            throw new BizException(4047, "verification not found");
        }
        return verification;
    }

    private ProductReview requireProductReview(Long id) {
        ProductReview review = productReviewMapper.selectById(id);
        if (review == null) {
            throw new BizException(4048, "product review not found");
        }
        return review;
    }

    private NewsReview requireNewsReview(Long id) {
        NewsReview review = newsReviewMapper.selectById(id);
        if (review == null) {
            throw new BizException(4049, "news review not found");
        }
        return review;
    }

    private void log(String operator, String action, String detail) {
        OperationLog log = new OperationLog();
        log.setOperator(operator);
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private <T> List<T> slice(List<T> items, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        List<T> sorted = new ArrayList<>(items);
        int from = Math.min((safePage - 1) * safePageSize, sorted.size());
        int to = Math.min(from + safePageSize, sorted.size());
        return new ArrayList<>(sorted.subList(from, to));
    }

    private Map<String, Object> pageResponse(List<?> items, int total, Integer page, Integer pageSize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page == null || page < 1 ? 1 : page);
        result.put("pageSize", pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100));
        return result;
    }
}


