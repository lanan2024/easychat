package com.easychat.service;

import com.easychat.dto.LoginResponse;
import com.easychat.dto.RegisterRequest;
import com.easychat.entity.User;
import com.easychat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setPhone("13800138000");

        LoginResponse response = userService.register(request);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void testRegister_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        userService.register(request);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void testLogin_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        userService.register(request);

        LoginResponse response = userService.login("testuser", "123456");
        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    void testLogin_WrongPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        userService.register(request);

        assertThrows(IllegalArgumentException.class,
                () -> userService.login("testuser", "wrongpassword"));
    }

    @Test
    void testGetUserById() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        LoginResponse response = userService.register(request);

        User user = userService.getUserById(response.getUser().getUserId());
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }

    @Test
    void testUpdateStatus() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        LoginResponse response = userService.register(request);

        userService.updateStatus(response.getUser().getUserId(), 1);
        User user = userService.getUserById(response.getUser().getUserId());
        assertEquals(1, user.getStatus());
    }
}
