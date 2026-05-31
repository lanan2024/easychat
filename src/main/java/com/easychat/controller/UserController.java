package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.LoginRequest;
import com.easychat.dto.LoginResponse;
import com.easychat.dto.RegisterRequest;
import com.easychat.dto.UserDTO;
import com.easychat.service.UserService;
import com.easychat.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(userService.getUserDTOById(userId));
    }

    @GetMapping("/info/{userId}")
    public Result<UserDTO> getUserInfoById(@PathVariable Long userId) {
        return Result.success(userService.getUserDTOById(userId));
    }

    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestHeader("Authorization") String token,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) String avatar) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.updateUserInfo(userId, phone, avatar);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.updateStatus(userId, 0);
        return Result.success();
    }
}
