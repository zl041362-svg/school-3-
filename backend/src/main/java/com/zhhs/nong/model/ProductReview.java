package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_reviews")
public class ProductReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("product_id")
    private Long productId;
    private String product;
    private String farmer;
    private BigDecimal price;
    private String status;
    private String reason;
    @TableField("created_at")
    private LocalDateTime submittedAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
