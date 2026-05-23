package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("farmer_verifications")
public class FarmerVerification {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    private String farmer;
    private String contact;
    @TableField("real_name")
    private String realName;
    @TableField("id_number")
    private String idNumber;
    @TableField("business_no")
    private String businessNo;
    private String status;
    private String reason;
    @TableField("created_at")
    private LocalDateTime submittedAt;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
