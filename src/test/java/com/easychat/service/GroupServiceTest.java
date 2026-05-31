package com.easychat.service;

import com.easychat.dto.RegisterRequest;
import com.easychat.dto.UserDTO;
import com.easychat.entity.Group;
import com.easychat.repository.GroupMemberRepository;
import com.easychat.repository.GroupRepository;
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
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    private Long ownerId, memberId;

    @BeforeEach
    void setUp() {
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("owner");
        req1.setPassword("123456");
        ownerId = userService.register(req1).getUser().getUserId();

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("member");
        req2.setPassword("123456");
        memberId = userService.register(req2).getUser().getUserId();
    }

    @Test
    void testCreateGroup_Success() {
        Group group = groupService.createGroup("测试群", ownerId);
        assertNotNull(group);
        assertEquals("测试群", group.getGroupName());
        assertEquals(ownerId, group.getOwnerId());
    }

    @Test
    void testJoinGroup_Success() {
        Group group = groupService.createGroup("测试群", ownerId);
        groupService.joinGroup(group.getGroupId(), memberId);

        List<Group> userGroups = groupService.getUserGroups(memberId);
        assertEquals(1, userGroups.size());
    }

    @Test
    void testJoinGroup_Duplicate() {
        Group group = groupService.createGroup("测试群", ownerId);
        groupService.joinGroup(group.getGroupId(), memberId);
        assertThrows(IllegalArgumentException.class,
                () -> groupService.joinGroup(group.getGroupId(), memberId));
    }

    @Test
    void testGetGroupMembers() {
        Group group = groupService.createGroup("测试群", ownerId);
        groupService.joinGroup(group.getGroupId(), memberId);

        List<UserDTO> members = groupService.getGroupMembers(group.getGroupId());
        assertEquals(2, members.size());
    }

    @Test
    void testQuitGroup() {
        Group group = groupService.createGroup("测试群", ownerId);
        groupService.joinGroup(group.getGroupId(), memberId);
        groupService.quitGroup(group.getGroupId(), memberId);

        assertEquals(1, groupService.getGroupMembers(group.getGroupId()).size());
    }

    @Test
    void testOwnerCannotQuit() {
        Group group = groupService.createGroup("测试群", ownerId);
        assertThrows(IllegalArgumentException.class,
                () -> groupService.quitGroup(group.getGroupId(), ownerId));
    }

    @Test
    void testSetAdmin() {
        Group group = groupService.createGroup("测试群", ownerId);
        groupService.joinGroup(group.getGroupId(), memberId);
        groupService.setAdmin(group.getGroupId(), memberId, ownerId);
    }
}
