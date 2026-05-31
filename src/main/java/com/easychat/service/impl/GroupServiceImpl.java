package com.easychat.service.impl;

import com.easychat.dto.UserDTO;
import com.easychat.entity.Group;
import com.easychat.entity.GroupMember;
import com.easychat.entity.User;
import com.easychat.repository.GroupMemberRepository;
import com.easychat.repository.GroupRepository;
import com.easychat.repository.UserRepository;
import com.easychat.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Group createGroup(String groupName, Long ownerId) {
        Group group = new Group();
        group.setGroupName(groupName);
        group.setOwnerId(ownerId);
        group = groupRepository.save(group);

        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getGroupId());
        owner.setUserId(ownerId);
        owner.setRole(1);
        groupMemberRepository.save(owner);

        return group;
    }

    @Override
    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new IllegalArgumentException("已是群成员");
        }
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(0);
        groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void quitGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("群组不存在"));
        if (group.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("群主不能退出群聊，请先转让群主");
        }
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    @Transactional
    public void setAdmin(Long groupId, Long targetUserId, Long operatorId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("群组不存在"));
        if (!group.getOwnerId().equals(operatorId)) {
            throw new IllegalArgumentException("只有群主可以设置管理员");
        }
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("该用户不在群中"));
        member.setRole(1);
        groupMemberRepository.save(member);
    }

    @Override
    public List<Group> getUserGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> groupRepository.findById(m.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .toList();
    }

    @Override
    public List<UserDTO> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream().map(m -> {
            User user = userRepository.findById(m.getUserId()).orElse(null);
            if (user == null) return null;
            UserDTO dto = new UserDTO();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setAvatar(user.getAvatar());
            dto.setStatus(user.getStatus());
            return dto;
        }).filter(dto -> dto != null).toList();
    }
}
