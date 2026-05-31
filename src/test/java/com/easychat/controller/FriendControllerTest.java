package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.FriendRequest;
import com.easychat.dto.RegisterRequest;
import com.easychat.repository.FriendRepository;
import com.easychat.repository.UserRepository;
import com.easychat.service.UserService;
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
class FriendControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    private String token1;
    private Long userId2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        friendRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("fuser1");
        req1.setPassword("123456");
        var r1 = userService.register(req1);
        token1 = r1.getToken();

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("fuser2");
        req2.setPassword("123456");
        userId2 = userService.register(req2).getUser().getUserId();
    }

    @Test
    void testAddFriend() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token1);
        FriendRequest request = new FriendRequest();
        request.setFriendId(userId2);

        HttpEntity<FriendRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Result> response = restTemplate.exchange(
                "/api/friend/add", HttpMethod.POST, entity, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetFriends() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token1);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Result> response = restTemplate.exchange(
                "/api/friend/list", HttpMethod.GET, entity, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
