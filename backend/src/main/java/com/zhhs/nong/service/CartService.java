package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.trade.AddCartItemRequest;
import com.zhhs.nong.dto.trade.UpdateCartQtyRequest;
import com.zhhs.nong.mapper.CartItemMapper;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.model.CartItem;
import com.zhhs.nong.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartService(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCart(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getId));
        return listResponse(enrich(items));
    }

    @Transactional
    public CartItem addItem(Long userId, AddCartItemRequest request) {
        Product product = requireProduct(request.productId());
        Integer stockObj = product.getStock();
        if (stockObj == null || stockObj <= 0) {
            throw new BizException(4002, "product is sold out");
        }

        int qty = normalizeQty(request.qty(), stockObj);
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.productId()));

        CartItem target;
        if (existing != null) {
            target = existing;
            target.setQty(normalizeQty(existing.getQty() + qty, product.getStock()));
            target.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(target);
        } else {
            target = new CartItem();
            target.setUserId(userId);
            target.setProductId(request.productId());
            target.setQty(qty);
            target.setCreatedAt(LocalDateTime.now());
            target.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.insert(target);
        }
        enrich(target, product);
        return target;
    }

    @Transactional
    public CartItem updateQty(Long userId, Long itemId, UpdateCartQtyRequest request) {
        CartItem item = requireCartItem(userId, itemId);
        Product product = requireProduct(item.getProductId());
        Integer stockObj = product.getStock();
        if (stockObj == null || stockObj <= 0) {
            throw new BizException(4002, "product is sold out");
        }
        item.setQty(normalizeQty(request.qty(), stockObj));
        item.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(item);
        enrich(item, product);
        return item;
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        int deleted = cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, itemId)
                .eq(CartItem::getUserId, userId));
        if (deleted == 0) {
            throw new BizException(4043, "cart item not found");
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    private List<CartItem> enrich(List<CartItem> items) {
        if (items.isEmpty()) return items;
        List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (CartItem item : items) {
            enrich(item, productMap.get(item.getProductId()));
        }
        return items;
    }

    private void enrich(CartItem item, Product product) {
        if (product == null) {
            item.setName("商品已失效");
            item.setPrice(BigDecimal.ZERO);
            item.setStock(0);
            return;
        }
        item.setName(product.getName());
        item.setPrice(product.getPrice());
        item.setStock(product.getStock());
    }

    private Product requireProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        return product;
    }

    private CartItem requireCartItem(Long userId, Long itemId) {
        CartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, itemId)
                .eq(CartItem::getUserId, userId));
        if (item == null) {
            throw new BizException(4043, "cart item not found");
        }
        return item;
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

    private Map<String, Object> listResponse(List<CartItem> items) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", items.size());
        result.put("page", 1);
        result.put("pageSize", items.size());
        return result;
    }
}


