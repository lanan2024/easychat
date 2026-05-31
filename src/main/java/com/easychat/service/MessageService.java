package com.easychat.service;

import com.easychat.dto.MessageDTO;
import java.util.List;

public interface MessageService {
    MessageDTO sendSingleMessage(Long fromId, Long toId, String content);
    void sendGroupMessage(Long fromId, Long groupId, String content);
    void sendMessageToUser(Long userId, MessageDTO message);
    void pushOfflineMessages(Long userId);
    void markAsRead(Long msgId);
    List<MessageDTO> getConversation(Long userId1, Long userId2, int page, int size);
    List<MessageDTO> getGroupMessages(Long groupId, int page, int size);
}
