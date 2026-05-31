package com.easychat.service;

import com.easychat.dto.LoginResponse;
import com.easychat.dto.RegisterRequest;
import com.easychat.dto.UserDTO;
import com.easychat.entity.User;

public interface UserService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(String username, String password);
    User getUserById(Long userId);
    UserDTO getUserDTOById(Long userId);
    void updateStatus(Long userId, int status);
    void updateUserInfo(Long userId, String phone, String avatar);
}
