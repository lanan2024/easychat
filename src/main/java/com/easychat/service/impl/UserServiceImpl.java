package com.easychat.service.impl;

import com.easychat.dto.LoginResponse;
import com.easychat.dto.RegisterRequest;
import com.easychat.dto.UserDTO;
import com.easychat.entity.User;
import com.easychat.repository.UserRepository;
import com.easychat.service.UserService;
import com.easychat.util.EncryptUtil;
import com.easychat.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EncryptUtil encryptUtil;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, EncryptUtil encryptUtil, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.encryptUtil = encryptUtil;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encryptUtil.md5(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setStatus(0);
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        return buildLoginResponse(user, token);
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!user.getPassword().equals(encryptUtil.md5(password))) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        user.setStatus(1);
        userRepository.save(user);

        return buildLoginResponse(user, token);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Override
    public UserDTO getUserDTOById(Long userId) {
        User user = getUserById(userId);
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        return dto;
    }

    @Override
    public void updateStatus(Long userId, int status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            userRepository.save(user);
        });
    }

    @Override
    public void updateUserInfo(Long userId, String phone, String avatar) {
        User user = getUserById(userId);
        if (phone != null) user.setPhone(phone);
        if (avatar != null) user.setAvatar(avatar);
        userRepository.save(user);
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUserId(), user.getUsername(), user.getPhone(),
                user.getAvatar(), user.getStatus());
        return new LoginResponse(token, userInfo);
    }
}
