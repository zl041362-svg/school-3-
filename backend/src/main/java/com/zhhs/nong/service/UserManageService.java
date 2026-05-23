package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserManageService {

    private final UserMapper userMapper;
    private final OperationLogService logService;

    public UserManageService(UserMapper userMapper, OperationLogService logService) {
        this.userMapper = userMapper;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsers(Integer page, Integer pageSize) {
        List<User> all = userMapper.selectList(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getId));
        return PageUtils.pageResponse(PageUtils.slice(all, page, pageSize, 20), all.size(), page, pageSize, 20);
    }

    @Transactional
    public User updateStatus(Long id, String status, String operator) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(4040, "user not found");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        logService.log(operator, "update_user_status", "用户 " + id + " 状态更新为 " + status);
        return user;
    }
}
