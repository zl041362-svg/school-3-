package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.trade.CreateOrderRequest;
import com.zhhs.nong.mapper.CartItemMapper;
import com.zhhs.nong.mapper.OrderItemMapper;
import com.zhhs.nong.mapper.OrderMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.model.CartItem;
import com.zhhs.nong.model.Order;
import com.zhhs.nong.model.OrderItem;
import com.zhhs.nong.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        CartItemMapper cartItemMapper,
                        ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrders(Long userId, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        List<Order> all = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getId));
        List<Order> items = slice(all, safePage, safePageSize);
        return pageResponse(items, all.size(), safePage, safePageSize);
    }

    @Transactional(readOnly = true)
    public Order getDetail(Long userId, Long id) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BizException(4045, "order not found");
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId));
        order.setItems(items);
        return order;
    }

    @Transactional
    public Order createOrder(Long userId, CreateOrderRequest request) {
        List<LineItem> lines = resolveLines(userId);
        if (lines.isEmpty()) {
            throw new BizException(4005, "cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<Product> updatedProducts = new ArrayList<>();

        for (LineItem line : lines) {
            Product product = requireProduct(line.productId());
            Integer stockObj = product.getStock();
            if (stockObj == null || stockObj <= 0) {
                throw new BizException(4006, "insufficient stock for product " + product.getName());
            }
            int qty = normalizeQty(line.qty(), stockObj);
            if (stockObj < qty) {
                throw new BizException(4006, "insufficient stock for product " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);
            updatedProducts.add(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQty(qty);
            orderItem.setSubtotal(subtotal);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setReceiver(request.receiver());
        order.setPhone(request.phone());
        order.setAddress(request.address());
        order.setStatus("pending_shipment");
        order.setPaymentStatus("paid");
        order.setLogistics(null);
        order.setAmount(total);
        // Insert requires a non-null unique order number before auto-increment ID is known.
        order.setOrderNo("TMP-" + System.currentTimeMillis() + "-" + userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        order.setOrderNo("OD-" + String.format("%06d", order.getId()));
        orderMapper.updateById(order);

        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            Product product = updatedProducts.get(i);
            product.setStock(product.getStock() - orderItem.getQty());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);

            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        order.setItems(orderItems);
        return order;
    }

    @Transactional
    public Order confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BizException(4045, "order not found");
        }
        if (!"shipped".equals(order.getStatus())) {
            throw new BizException(4007, "order is not in shipped status");
        }
        order.setStatus("completed");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }

    private List<LineItem> resolveLines(Long userId) {
        List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByAsc(CartItem::getId));
        List<LineItem> lines = new ArrayList<>();
        for (CartItem item : cartItems) {
            lines.add(new LineItem(item.getProductId(), item.getQty()));
        }
        return lines;
    }

    private Product requireProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        return product;
    }

    private int normalizeQty(int qty, int stock) {
        if (qty < 1) {
            return 1;
        }
        if (stock > 0) {
            return Math.min(qty, stock);
        }
        return qty;
    }

    private <T> List<T> slice(List<T> items, int page, int pageSize) {
        int from = Math.min((page - 1) * pageSize, items.size());
        int to = Math.min(from + pageSize, items.size());
        return new ArrayList<>(items.subList(from, to));
    }

    private Map<String, Object> pageResponse(List<Order> items, int total, int page, int pageSize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    private record LineItem(Long productId, Integer qty) {
    }
}


