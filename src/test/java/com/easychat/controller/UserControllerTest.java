package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.LoginRequest;
import com.easychat.dto.LoginResponse;
import com.easychat.dto.RegisterRequest;
import com.easychat.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() {
        if (token != null) return;

        RegisterRequest register = new RegisterRequest();
        register.setUsername("controllertest");
        register.setPassword("123456");
        register.setPhone("13900000001");

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/api/user/register", register, Result.class);
        if (response.getBody() != null && response.getBody().getData() != null) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) response.getBody().getData();
            token = (String) data.get("token");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> user = (java.util.Map<String, Object>) data.get("user");
            if (user != null) {
                userId = Long.valueOf(user.get("userId").toString());
            }
        }

        if (token == null) {
            LoginRequest login = new LoginRequest();
            login.setUsername("controllertest");
            login.setPassword("123456");
            ResponseEntity<Result> loginResp = restTemplate.postForEntity(
                    "/api/user/login", login, Result.class);
            if (loginResp.getBody() != null && loginResp.getBody().getData() != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> data = (java.util.Map<String, Object>) loginResp.getBody().getData();
                token = (String) data.get("token");
            }
        }
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setPhone("13900000002");

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/api/user/register", request, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    void testRegister_Duplicate() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("controllertest");
        request.setPassword("123456");

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/api/user/register", request, Result.class);
        assertEquals(400, response.getBody().getCode());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("controllertest");
        request.setPassword("123456");

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/api/user/login", request, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("controllertest");
        request.setPassword("wrong");

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/api/user/login", request, Result.class);
        assertEquals(400, response.getBody().getCode());
    }

    @Test
    void testGetUserInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Result> response = restTemplate.exchange(
                "/api/user/info", HttpMethod.GET, entity, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetUserInfo_Unauthorized() {
        ResponseEntity<Result> response = restTemplate.getForEntity(
                "/api/user/info", Result.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLogout() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Result> response = restTemplate.exchange(
                "/api/user/logout", HttpMethod.POST, entity, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
