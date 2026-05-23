package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_evaluations")
public class ProductEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("product_id")
    private Long productId;
    @TableField("user_id")
    private Long userId;
    private Integer rating;
    private String content;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private String userName;
}
