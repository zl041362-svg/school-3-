package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFarmer() { return farmer; }
    public void setFarmer(String farmer) { this.farmer = farmer; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

