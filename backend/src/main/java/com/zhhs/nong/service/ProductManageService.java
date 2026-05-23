package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.admin.SaveProductAdminRequest;
import com.zhhs.nong.mapper.ProductMapper;
import com.zhhs.nong.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProductManageService {

    private final ProductMapper productMapper;
    private final OperationLogService logService;

    public ProductManageService(ProductMapper productMapper, OperationLogService logService) {
        this.productMapper = productMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProducts(Integer page, Integer pageSize) {
        List<Product> all = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public Product update(Long id, SaveProductAdminRequest request, String operator) {
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
        logService.log(operator, "update_product", "编辑商品 " + id);
        return product;
    }

    @Transactional
    public Product updateStatus(Long id, String status, String operator) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(4041, "product not found");
        }
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        logService.log(operator, "update_product_status", "商品 " + id + " 状态更新为 " + status);
        return product;
    }
}
