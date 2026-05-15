package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String action;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

