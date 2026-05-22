package com.zhhs.nong.controller;
import com.zhhs.nong.dto.auth.LoginRequest;
import com.zhhs.nong.dto.auth.RegisterRequest;
import com.zhhs.nong.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication authentication) {
        Long userId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        return authService.profile(userId);
    }
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(Authentication authentication, @RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        return authService.updateProfile(userId, body.get("name"));
    }
}
