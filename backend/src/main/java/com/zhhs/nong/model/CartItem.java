package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart_items")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    @JsonIgnore
    private Long userId;
    @TableField("product_id")
    private Long productId;
    private Integer qty;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private BigDecimal price;
    @TableField(exist = false)
    private Integer stock;
}
