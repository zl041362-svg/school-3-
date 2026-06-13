package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.admin.BatchReviewRequest;
import com.zhhs.nong.mapper.NewsMapper;
import com.zhhs.nong.mapper.NewsReviewMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.mapper.ProductReviewMapper;
import com.zhhs.nong.model.News;
import com.zhhs.nong.model.NewsReview;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductReview;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewManageService {

    private final ProductReviewMapper productReviewMapper;
    private final NewsReviewMapper newsReviewMapper;
    private final ProductMapper productMapper;
    private final NewsMapper newsMapper;
    private final OperationLogService logService;

    public ReviewManageService(ProductReviewMapper productReviewMapper,
                               NewsReviewMapper newsReviewMapper,
                               ProductMapper productMapper,
                               NewsMapper newsMapper,
                               OperationLogService logService) {
        this.productReviewMapper = productReviewMapper;
        this.newsReviewMapper = newsReviewMapper;
        this.productMapper = productMapper;
        this.newsMapper = newsMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductReviews(Integer page, Integer pageSize) {
        List<ProductReview> all = productReviewMapper.selectList(new LambdaQueryWrapper<ProductReview>()
                .ne(ProductReview::getStatus, "cancelled")
                .orderByDesc(ProductReview::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getNewsReviews(Integer page, Integer pageSize) {
        List<NewsReview> all = newsReviewMapper.selectList(new LambdaQueryWrapper<NewsReview>()
                .ne(NewsReview::getStatus, "cancelled")
                .orderByDesc(NewsReview::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
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

        logService.log(operator, "review_product", "审核商品 " + id + " -> " + review.getStatus());
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

        logService.log(operator, "review_news", "审核资讯 " + id + " -> " + review.getStatus());
        return review;
    }

    @Transactional
    public void batchReviewProducts(BatchReviewRequest request, String operator) {
        List<ProductReview> reviews = productReviewMapper.selectBatchIds(request.ids());
        List<Long> productIds = reviews.stream().map(ProductReview::getProductId).collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (ProductReview review : reviews) {
            review.setStatus(request.approved() ? "approved" : "rejected");
            review.setReason(request.reason());
            review.setUpdatedAt(LocalDateTime.now());
            productReviewMapper.updateById(review);

            Product product = productMap.get(review.getProductId());
            if (product != null) {
                product.setStatus(request.approved() ? "published" : "rejected");
                product.setUpdatedAt(LocalDateTime.now());
                productMapper.updateById(product);
            }
        }
        logService.log(operator, "batch_review_products", "批量审核商品 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
    }

    @Transactional
    public void batchReviewNews(BatchReviewRequest request, String operator) {
        List<NewsReview> reviews = newsReviewMapper.selectBatchIds(request.ids());
        List<Long> newsIds = reviews.stream().map(NewsReview::getNewsId).collect(Collectors.toList());
        Map<Long, News> newsMap = newsMapper.selectBatchIds(newsIds).stream()
                .collect(Collectors.toMap(News::getId, n -> n));

        for (NewsReview review : reviews) {
            review.setStatus(request.approved() ? "approved" : "rejected");
            review.setReason(request.reason());
            review.setUpdatedAt(LocalDateTime.now());
            newsReviewMapper.updateById(review);

            News news = newsMap.get(review.getNewsId());
            if (news != null) {
                news.setStatus(request.approved() ? "published" : "rejected");
                if (request.approved() && news.getPublishedAt() == null) {
                    news.setPublishedAt(LocalDateTime.now());
                }
                news.setUpdatedAt(LocalDateTime.now());
                newsMapper.updateById(news);
            }
        }
        logService.log(operator, "batch_review_news", "批量审核资讯 " + request.ids().size() + " 条 -> " + (request.approved() ? "通过" : "驳回"));
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
}
