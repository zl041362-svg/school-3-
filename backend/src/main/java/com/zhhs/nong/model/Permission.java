package com.zhhs.nong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String action;
    private String role;
}
