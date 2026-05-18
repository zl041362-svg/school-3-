package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zhhs.nong.common.PageUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
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
}

