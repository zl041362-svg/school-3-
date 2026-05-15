package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("roles")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String role;
    private Integer members;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getMembers() { return members; }
    public void setMembers(Integer members) { this.members = members; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

