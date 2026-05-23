package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news_reviews")
public class NewsReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("news_id")
    private Long newsId;
    private String title;
    private String author;
    private String status;
    private String reason;
    @TableField("created_at")
    private LocalDateTime submittedAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
