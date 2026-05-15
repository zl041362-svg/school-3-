package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
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


