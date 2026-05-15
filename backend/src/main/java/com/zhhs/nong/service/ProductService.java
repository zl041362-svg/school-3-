package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public Map<String, Object> list(String keyword, String category, String region, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

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
        int total = all.size();
        int from = Math.min((safePage - 1) * safePageSize, total);
        int to = Math.min(from + safePageSize, total);

        return Map.of(
                "items", all.subList(from, to),
                "total", total,
                "page", safePage,
                "pageSize", safePageSize
        );
    }

    public Product detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        return product;
    }
}

