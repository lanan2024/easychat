package com.easychat.service;

import com.easychat.dto.RegisterRequest;
import com.easychat.dto.UserDTO;
import com.easychat.entity.User;
import com.easychat.repository.FriendRepository;
import com.easychat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    private Long user1Id, user2Id;

    @BeforeEach
    void setUp() {
        friendRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("user1");
        req1.setPassword("123456");
        user1Id = userService.register(req1).getUser().getUserId();

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("user2");
        req2.setPassword("123456");
        user2Id = userService.register(req2).getUser().getUserId();
    }

    @Test
    void testAddFriend_Success() {
        friendService.addFriend(user1Id, user2Id, "备注", 0L);
        List<UserDTO> friends = friendService.getFriends(user1Id);
        assertEquals(1, friends.size());
        assertEquals("user2", friends.get(0).getUsername());
    }

    @Test
    void testAddFriend_ReverseExists() {
        friendService.addFriend(user1Id, user2Id, null, null);
        List<UserDTO> friends1 = friendService.getFriends(user1Id);
        List<UserDTO> friends2 = friendService.getFriends(user2Id);
        assertEquals(1, friends1.size());
        assertEquals(1, friends2.size());
    }

    @Test
    void testAddFriend_Duplicate() {
        friendService.addFriend(user1Id, user2Id, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> friendService.addFriend(user1Id, user2Id, null, null));
    }

    @Test
    void testAddFriend_Self() {
        assertThrows(IllegalArgumentException.class,
                () -> friendService.addFriend(user1Id, user1Id, null, null));
    }

    @Test
    void testDeleteFriend() {
        friendService.addFriend(user1Id, user2Id, null, null);
        friendService.deleteFriend(user1Id, user2Id);
        assertEquals(0, friendService.getFriends(user1Id).size());
    }

    @Test
    void testGetFriends_Empty() {
        List<UserDTO> friends = friendService.getFriends(user1Id);
        assertTrue(friends.isEmpty());
    }
}
