package com.easychat.service.impl;

import com.easychat.common.WebSocketSessionManager;
import com.easychat.dto.MessageDTO;
import com.easychat.entity.Message;
import com.easychat.entity.User;
import com.easychat.repository.GroupMemberRepository;
import com.easychat.repository.MessageRepository;
import com.easychat.repository.UserRepository;
import com.easychat.service.MessageService;
import com.easychat.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final WebSocketSessionManager sessionManager;
    private final JsonUtil jsonUtil;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository,
                              GroupMemberRepository groupMemberRepository,
                              WebSocketSessionManager sessionManager, JsonUtil jsonUtil) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.sessionManager = sessionManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    @Transactional
    public MessageDTO sendSingleMessage(Long fromId, Long toId, String content) {
        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setMsgType(1);
        message.setContent(content);
        message.setStatus(0);
        message = messageRepository.save(message);
        return toDTO(message);
    }

    @Override
    @Transactional
    public void sendGroupMessage(Long fromId, Long groupId, String content) {
        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(groupId);
        message.setMsgType(2);
        message.setContent(content);
        message.setStatus(0);
        message = messageRepository.save(message);

        MessageDTO dto = toDTO(message);
        List<Long> memberIds = groupMemberRepository.findByGroupId(groupId).stream()
                .map(m -> m.getUserId())
                .filter(id -> !id.equals(fromId))
                .toList();

        for (Long memberId : memberIds) {
            sendMessageToUser(memberId, dto);
        }
    }

    @Override
    public void sendMessageToUser(Long userId, MessageDTO message) {
        Channel channel = sessionManager.getChannel(userId);
        if (channel != null && channel.isActive()) {
            Map<String, Object> push = new LinkedHashMap<>();
            push.put("type", "NEW_MESSAGE");
            push.put("message", message);
            channel.writeAndFlush(new TextWebSocketFrame(jsonUtil.toJson(push)));
        }
    }

    @Override
    public void pushOfflineMessages(Long userId) {
        List<Message> offline = messageRepository.findByToIdAndMsgTypeAndStatusOrderBySendTimeAsc(
                userId, 1, 0);
        for (Message msg : offline) {
            MessageDTO dto = toDTO(msg);
            sendMessageToUser(userId, dto);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long msgId) {
        messageRepository.updateStatusByMsgId(msgId, 1);
    }

    @Override
    public List<MessageDTO> getConversation(Long userId1, Long userId2, int page, int size) {
        List<Message> messages = messageRepository
                .findByFromIdAndToIdAndMsgTypeOrderBySendTimeAsc(userId1, userId2, 1);
        List<Message> reverse = messageRepository
                .findByFromIdAndToIdAndMsgTypeOrderBySendTimeAsc(userId2, userId1, 1);

        List<Message> combined = new ArrayList<>();
        combined.addAll(messages);
        combined.addAll(reverse);
        combined.sort(Comparator.comparing(Message::getSendTime));

        int start = page * size;
        int end = Math.min(start + size, combined.size());
        if (start >= combined.size()) return List.of();

        return combined.subList(start, end).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MessageDTO> getGroupMessages(Long groupId, int page, int size) {
        List<Message> messages = messageRepository
                .findByFromIdAndToIdAndMsgTypeOrderBySendTimeAsc(groupId, groupId, 2);
        int start = page * size;
        int end = Math.min(start + size, messages.size());
        if (start >= messages.size()) return List.of();
        return messages.subList(start, end).stream().map(this::toDTO).toList();
    }

    private MessageDTO toDTO(Message msg) {
        MessageDTO dto = new MessageDTO();
        dto.setMsgId(msg.getMsgId());
        dto.setFromId(msg.getFromId());
        dto.setToId(msg.getToId());
        dto.setMsgType(msg.getMsgType());
        dto.setContent(msg.getContent());
        dto.setSendTime(msg.getSendTime());
        dto.setStatus(msg.getStatus());

        userRepository.findById(msg.getFromId()).ifPresent(user -> {
            dto.setFromUsername(user.getUsername());
            dto.setFromAvatar(user.getAvatar());
        });

        return dto;
    }
}
