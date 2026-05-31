package com.easychat.service;

import com.easychat.dto.UserDTO;
import java.util.List;

public interface FriendService {
    void addFriend(Long userId, Long friendId, String remark, Long groupId);
    void deleteFriend(Long userId, Long friendId);
    List<UserDTO> getFriends(Long userId);
}
