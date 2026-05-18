package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.merchant.SaveNewsRequest;
import com.zhhs.nong.dto.merchant.SaveProductRequest;
import com.zhhs.nong.dto.merchant.SubmitVerificationRequest;
import com.zhhs.nong.mapper.FarmerVerificationMapper;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.mapper.NewsReviewMapper;
import com.zhhs.nong.mapper.OrderItemMapper;
import com.zhhs.nong.mapper.OrderMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.mapper.ProductReviewMapper;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.FarmerVerification;
import com.zhhs.nong.model.News;
import com.zhhs.nong.model.NewsReview;
import com.zhhs.nong.model.Order;
import com.zhhs.nong.model.OrderItem;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductReview;
import com.zhhs.nong.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhhs.nong.common.PageUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MerchantService {

    private final FarmerVerificationMapper farmerVerificationMapper;
    private final ProductMapper productMapper;
    private final ProductReviewMapper productReviewMapper;
    private final NewsMapper newsMapper;
    private final NewsReviewMapper newsReviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public MerchantService(FarmerVerificationMapper farmerVerificationMapper,
                           ProductMapper productMapper,
                           ProductReviewMapper productReviewMapper,
                           NewsMapper newsMapper,
                           NewsReviewMapper newsReviewMapper,
                           OrderMapper orderMapper,
                           OrderItemMapper orderItemMapper,
                           UserMapper userMapper) {
        this.farmerVerificationMapper = farmerVerificationMapper;
        this.productMapper = productMapper;
        this.productReviewMapper = productReviewMapper;
        this.newsMapper = newsMapper;
        this.newsReviewMapper = newsReviewMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public FarmerVerification submitVerification(Long userId, SubmitVerificationRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(4040, "user not found");
        }

        FarmerVerification existing = farmerVerificationMapper.selectOne(new LambdaQueryWrapper<FarmerVerification>()
                .eq(FarmerVerification::getUserId, userId)
                .eq(FarmerVerification::getStatus, "pending"));
        if (existing != null) {
            throw new BizException(4008, "pending verification already exists");
        }

        FarmerVerification verification = new FarmerVerification();
        verification.setUserId(userId);
        verification.setFarmer(user.getName());
        verification.setContact(user.getPhone());
        verification.setRealName(request.realName());
        verification.setIdNumber(request.idNumber());
        verification.setBusinessNo(request.businessNo());
        verification.setStatus("pending");
        verification.setSubmittedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        farmerVerificationMapper.insert(verification);
        return verification;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Long userId) {
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId));
        long newsCount = newsMapper.selectCount(new LambdaQueryWrapper<News>()
                .eq(News::getUserId, userId));

        List<Long> productIds = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId).select(Product::getId)).stream().map(Product::getId).toList();
        long pendingOrderCount = 0;
        if (!productIds.isEmpty()) {
            List<Long> orderIds = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getProductId, productIds).select(OrderItem::getOrderId)).stream()
                    .map(OrderItem::getOrderId).distinct().toList();
            if (!orderIds.isEmpty()) {
                pendingOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                        .in(Order::getId, orderIds).eq(Order::getStatus, "pending_shipment"));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productCount", productCount);
        result.put("newsCount", newsCount);
        result.put("pendingOrderCount", pendingOrderCount);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProducts(Long userId, Integer page, Integer pageSize) {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .orderByDesc(Product::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public Product createProduct(Long userId, SaveProductRequest request) {
        User user = userMapper.selectById(userId);
        Product product = new Product();
        product.setName(request.name());
        product.setCategory(request.category());
        product.setRegion(request.region() != null ? request.region() : "");
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setSummary(request.summary() != null ? request.summary() : "");
        product.setDescription(request.description() != null ? request.description() : "");
        product.setSpec(request.spec() != null ? request.spec() : "");
        product.setQualification(request.qualification() != null ? request.qualification() : "");
        product.setFarmer(user != null ? user.getName() : "");
        product.setStatus("pending");
        product.setUserId(userId);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);

        ProductReview review = new ProductReview();
        review.setProductId(product.getId());
        review.setProduct(product.getName());
        review.setFarmer(product.getFarmer());
        review.setPrice(product.getPrice());
        review.setStatus("pending");
        review.setSubmittedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        productReviewMapper.insert(review);

        return product;
    }

    @Transactional
    public Product updateProduct(Long userId, Long productId, SaveProductRequest request) {
        Product product = requireProduct(userId, productId);
        product.setName(request.name());
        product.setCategory(request.category());
        product.setRegion(request.region() != null ? request.region() : "");
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setSummary(request.summary() != null ? request.summary() : "");
        product.setDescription(request.description() != null ? request.description() : "");
        product.setSpec(request.spec() != null ? request.spec() : "");
        product.setQualification(request.qualification() != null ? request.qualification() : "");
        product.setStatus("pending");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        ProductReview review = new ProductReview();
        review.setProductId(product.getId());
        review.setProduct(product.getName());
        review.setFarmer(product.getFarmer());
        review.setPrice(product.getPrice());
        review.setStatus("pending");
        review.setSubmittedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        productReviewMapper.insert(review);

        return product;
    }

    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        requireProduct(userId, productId);
        productMapper.deleteById(productId);
        productReviewMapper.delete(new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, productId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNews(Long userId, Integer page, Integer pageSize, String keyword) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<News>()
                .eq(News::getUserId, userId)
                .orderByDesc(News::getId);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(News::getTitle, keyword);
        }

        List<News> all = newsMapper.selectList(wrapper);
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public News createNews(Long userId, SaveNewsRequest request) {
        User user = userMapper.selectById(userId);
        News news = new News();
        news.setTitle(request.title());
        news.setCategory(request.category());
        news.setSummary(request.summary() != null ? request.summary() : "");
        news.setContent(request.content());
        news.setAuthor(user != null ? user.getName() : "");
        news.setStatus("pending");
        news.setUserId(userId);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.insert(news);

        NewsReview review = new NewsReview();
        review.setNewsId(news.getId());
        review.setTitle(news.getTitle());
        review.setAuthor(news.getAuthor());
        review.setStatus("pending");
        review.setSubmittedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        newsReviewMapper.insert(review);

        return news;
    }

    @Transactional
    public News updateNews(Long userId, Long newsId, SaveNewsRequest request) {
        News news = requireNews(userId, newsId);
        news.setTitle(request.title());
        news.setCategory(request.category());
        news.setSummary(request.summary() != null ? request.summary() : "");
        news.setContent(request.content());
        news.setStatus("pending");
        news.setUpdatedAt(LocalDateTime.now());
        newsMapper.updateById(news);

        NewsReview review = new NewsReview();
        review.setNewsId(news.getId());
        review.setTitle(news.getTitle());
        review.setAuthor(news.getAuthor());
        review.setStatus("pending");
        review.setSubmittedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        newsReviewMapper.insert(review);

        return news;
    }

    @Transactional
    public void deleteNews(Long userId, Long newsId) {
        requireNews(userId, newsId);
        newsMapper.deleteById(newsId);
        newsReviewMapper.delete(new LambdaQueryWrapper<NewsReview>().eq(NewsReview::getNewsId, newsId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMerchantOrders(Long userId, Integer page, Integer pageSize) {
        List<Long> productIds = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId).select(Product::getId)).stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            return PageUtils.pageResponse(List.of(), 0, page, pageSize, 20);
        }
        List<Long> orderIds = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getProductId, productIds).select(OrderItem::getOrderId)).stream()
                .map(OrderItem::getOrderId).distinct().toList();
        if (orderIds.isEmpty()) {
            return PageUtils.pageResponse(List.of(), 0, page, pageSize, 20);
        }
        List<Order> all = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .in(Order::getId, orderIds).orderByDesc(Order::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public Order shipOrder(Long userId, Long orderId, String logistics) {
        List<Long> productIds = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId).select(Product::getId)).stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            throw new BizException(4041, "no products found");
        }
        List<Long> orderProductIds = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId).select(OrderItem::getProductId)).stream()
                .map(OrderItem::getProductId).toList();
        boolean owns = orderProductIds.stream().anyMatch(productIds::contains);
        if (!owns) {
            throw new BizException(4045, "order not found");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(4045, "order not found");
        }
        if (!"pending_shipment".equals(order.getStatus())) {
            throw new BizException(4007, "order status must be pending_shipment");
        }
        order.setStatus("shipped");
        order.setLogistics(logistics);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    private Product requireProduct(Long userId, Long productId) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getUserId, userId));
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        return product;
    }

    private News requireNews(Long userId, Long newsId) {
        News news = newsMapper.selectOne(new LambdaQueryWrapper<News>()
                .eq(News::getId, newsId)
                .eq(News::getUserId, userId));
        if (news == null) {
            throw new BizException(4042, "news not found");
        }
        return news;
    }
}

