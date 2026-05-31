package com.easychat.service;

import com.easychat.dto.UserDTO;
import com.easychat.entity.Group;
import java.util.List;

public interface GroupService {
    Group createGroup(String groupName, Long ownerId);
    void joinGroup(Long groupId, Long userId);
    void quitGroup(Long groupId, Long userId);
    void setAdmin(Long groupId, Long targetUserId, Long operatorId);
    List<Group> getUserGroups(Long userId);
    List<UserDTO> getGroupMembers(Long groupId);
}
