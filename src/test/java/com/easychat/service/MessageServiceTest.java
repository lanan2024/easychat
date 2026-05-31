package com.easychat.service;

import com.easychat.dto.MessageDTO;
import com.easychat.dto.RegisterRequest;
import com.easychat.entity.Group;
import com.easychat.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private FriendRepository friendRepository;

    private Long user1Id, user2Id, user3Id;
    private Long groupId;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
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

        RegisterRequest req3 = new RegisterRequest();
        req3.setUsername("user3");
        req3.setPassword("123456");
        user3Id = userService.register(req3).getUser().getUserId();
    }

    @Test
    void testSendSingleMessage() {
        MessageDTO dto = messageService.sendSingleMessage(user1Id, user2Id, "你好");
        assertNotNull(dto);
        assertEquals(user1Id, dto.getFromId());
        assertEquals(user2Id, dto.getToId());
        assertEquals("你好", dto.getContent());
        assertEquals(1, dto.getMsgType());
    }

    @Test
    void testGetConversation() {
        messageService.sendSingleMessage(user1Id, user2Id, "消息1");
        messageService.sendSingleMessage(user2Id, user1Id, "消息2");
        messageService.sendSingleMessage(user1Id, user2Id, "消息3");

        List<MessageDTO> conversation = messageService.getConversation(user1Id, user2Id, 0, 20);
        assertEquals(3, conversation.size());
    }

    @Test
    void testGetConversation_Empty() {
        List<MessageDTO> conversation = messageService.getConversation(user1Id, user2Id, 0, 20);
        assertTrue(conversation.isEmpty());
    }

    @Test
    void testSendGroupMessage() {
        Group group = groupService.createGroup("群聊", user1Id);
        groupService.joinGroup(group.getGroupId(), user2Id);
        groupService.joinGroup(group.getGroupId(), user3Id);

        messageService.sendGroupMessage(user1Id, group.getGroupId(), "大家好");
    }

    @Test
    void testMarkAsRead() {
        MessageDTO dto = messageService.sendSingleMessage(user1Id, user2Id, "已读测试");
        messageService.markAsRead(dto.getMsgId());
    }
}
