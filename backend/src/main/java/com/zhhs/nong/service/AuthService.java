package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.auth.LoginRequest;
import com.zhhs.nong.dto.auth.RegisterRequest;
import com.zhhs.nong.mapper.UserMapper;
import com.zhhs.nong.model.User;
import com.zhhs.nong.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.phone()));
        if (exists != null) {
            throw new BizException(4001, "phone already exists");
        }

        User user = new User();
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("customer");
        user.setStatus("active");
        user.setName("User" + request.phone().substring(Math.max(0, request.phone().length() - 4)));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return Map.of("id", user.getId(), "phone", user.getPhone(), "role", user.getRole());
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.phone()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(4010, "invalid phone or password");
        }

        if (!"active".equals(user.getStatus())) {
            throw new BizException(4030, "user is disabled");
        }

        String token = jwtService.createToken(user.getId(), user.getRole(), user.getPhone());
        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "phone", user.getPhone(),
                        "role", user.getRole()
                )
        );
    }

    public Map<String, Object> profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(4040, "user not found");
        }

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "phone", user.getPhone(),
                        "role", user.getRole()
                )
        );
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, String name) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(4040, "user not found");
        }
        user.setName(name);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "phone", user.getPhone(),
                        "role", user.getRole()
                )
        );
    }
}

