package com.example.fieldmapper.controller;

import com.example.fieldmapper.model.AppUser;
import com.example.fieldmapper.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<AppUser> listUsers() {
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body, Authentication auth) {
        String username = body.get("username");
        String role = body.getOrDefault("role", "USER");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名不能为空"));
        }
        if (userService.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }
        String encodedPassword = passwordEncoder.encode("123456");
        userService.createUser(username, encodedPassword, role, auth.getName());
        return ResponseEntity.ok(Map.of("message", "用户创建成功"));
    }
}
