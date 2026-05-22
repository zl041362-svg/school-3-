package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.trade.CreateEvaluationRequest;
import com.zhhs.nong.mapper.OrderItemMapper;
import com.zhhs.nong.mapper.OrderMapper;
import com.zhhs.nong.mapper.ProductEvaluationMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.Order;
import com.zhhs.nong.model.OrderItem;
import com.zhhs.nong.model.Product;
import com.zhhs.nong.model.ProductEvaluation;
import com.zhhs.nong.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhhs.nong.common.PageUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductEvaluationMapper evaluationMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public ProductService(ProductMapper productMapper,
                          ProductEvaluationMapper evaluationMapper,
                          OrderMapper orderMapper,
                          OrderItemMapper orderItemMapper,
                          UserMapper userMapper) {
        this.productMapper = productMapper;
        this.evaluationMapper = evaluationMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userMapper = userMapper;
    }

    public Map<String, Object> list(String keyword, String category, String region, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "published")
                .orderByDesc(Product::getId);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.like(Product::getCategory, category);
        }
        if (StringUtils.hasText(region)) {
            wrapper.like(Product::getRegion, region);
        }

        List<Product> all = productMapper.selectList(wrapper);
        List<Product> items = PageUtils.slice(all, page, pageSize, 20);
        return PageUtils.pageResponse(items, all.size(), page, pageSize, 20);
    }

    public Product detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        return product;
    }

    @Transactional(readOnly = true)
    public boolean canReview(Long userId, Long productId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .in(Order::getStatus, "pending_shipment", "shipped", "completed"));
        if (orders.isEmpty()) return false;
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        return orderItemMapper.exists(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getProductId, productId));
    }

    @Transactional
    public ProductEvaluation createEvaluation(Long userId, Long productId, CreateEvaluationRequest request) {
        if (!canReview(userId, productId)) {
            throw new BizException(4009, "只有购买过该商品才能评价");
        }
        ProductEvaluation existing = evaluationMapper.selectOne(new LambdaQueryWrapper<ProductEvaluation>()
                .eq(ProductEvaluation::getUserId, userId)
                .eq(ProductEvaluation::getProductId, productId));
        ProductEvaluation eval;
        if (existing != null) {
            eval = existing;
            eval.setRating(request.rating());
            eval.setContent(request.content());
            eval.setUpdatedAt(LocalDateTime.now());
            evaluationMapper.updateById(eval);
        } else {
            eval = new ProductEvaluation();
            eval.setUserId(userId);
            eval.setProductId(productId);
            eval.setRating(request.rating());
            eval.setContent(request.content());
            eval.setCreatedAt(LocalDateTime.now());
            eval.setUpdatedAt(LocalDateTime.now());
            evaluationMapper.insert(eval);
        }
        User user = userMapper.selectById(userId);
        if (user != null) eval.setUserName(user.getName());
        return eval;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEvaluations(Long productId, Integer page, Integer pageSize) {
        List<ProductEvaluation> all = evaluationMapper.selectList(new LambdaQueryWrapper<ProductEvaluation>()
                .eq(ProductEvaluation::getProductId, productId)
                .orderByDesc(ProductEvaluation::getId));
        List<ProductEvaluation> items = PageUtils.slice(new ArrayList<>(all), page, pageSize, 20);

        if (!items.isEmpty()) {
            List<Long> userIds = items.stream().map(ProductEvaluation::getUserId).distinct().toList();
            Map<Long, String> nameMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : "匿名用户"));
            items.forEach(e -> e.setUserName(nameMap.getOrDefault(e.getUserId(), "匿名用户")));
        }

        double avg = all.isEmpty() ? 0 : all.stream().mapToInt(ProductEvaluation::getRating).average().orElse(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", all.size());
        result.put("page", page == null || page < 1 ? 1 : page);
        result.put("pageSize", pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100));
        result.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
        result.put("count", all.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyEvaluations(Long userId, Integer page, Integer pageSize) {
        List<ProductEvaluation> all = evaluationMapper.selectList(new LambdaQueryWrapper<ProductEvaluation>()
                .eq(ProductEvaluation::getUserId, userId)
                .orderByDesc(ProductEvaluation::getId));
        List<ProductEvaluation> items = PageUtils.slice(all, page, pageSize, 20);
        if (!items.isEmpty()) {
            List<Long> productIds = items.stream().map(ProductEvaluation::getProductId).distinct().toList();
            Map<Long, String> nameMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p.getName()));
            items.forEach(e -> e.setUserName(nameMap.getOrDefault(e.getProductId(), "已下架")));
        }
        return PageUtils.pageResponse(items, all.size(), page, pageSize, 20);
    }

    @Transactional
    public void deleteEvaluation(Long userId, Long evaluationId) {
        ProductEvaluation eval = evaluationMapper.selectById(evaluationId);
        if (eval == null) {
            throw new BizException(4051, "评价不存在");
        }
        if (!eval.getUserId().equals(userId)) {
            throw new BizException(4031, "只能删除自己的评价");
        }
        evaluationMapper.deleteById(evaluationId);
    }
}
