package com.easychat.service.impl;

import com.easychat.dto.UserDTO;
import com.easychat.entity.Friend;
import com.easychat.entity.User;
import com.easychat.repository.FriendRepository;
import com.easychat.repository.UserRepository;
import com.easychat.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public FriendServiceImpl(FriendRepository friendRepository, UserRepository userRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addFriend(Long userId, Long friendId, String remark, Long groupId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("不能添加自己为好友");
        }
        if (!userRepository.existsById(friendId)) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent()) {
            throw new IllegalArgumentException("已是好友关系");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setRemark(remark);
        friend.setGroupId(groupId);
        friendRepository.save(friend);

        Friend reverse = new Friend();
        reverse.setUserId(friendId);
        reverse.setFriendId(userId);
        reverse.setRemark(null);
        reverse.setGroupId(0L);
        friendRepository.save(reverse);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);
    }

    @Override
    public List<UserDTO> getFriends(Long userId) {
        List<Friend> friends = friendRepository.findByUserId(userId);
        return friends.stream().map(f -> {
            User friendUser = userRepository.findById(f.getFriendId()).orElse(null);
            if (friendUser == null) return null;
            UserDTO dto = new UserDTO();
            dto.setUserId(friendUser.getUserId());
            dto.setUsername(friendUser.getUsername());
            dto.setPhone(friendUser.getPhone());
            dto.setAvatar(friendUser.getAvatar());
            dto.setStatus(friendUser.getStatus());
            dto.setRemark(f.getRemark());
            return dto;
        }).filter(dto -> dto != null).toList();
    }
}
