package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.MessageDTO;
import com.easychat.service.MessageService;
import com.easychat.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/conversation/{targetId}")
    public Result<List<MessageDTO>> getConversation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(messageService.getConversation(userId, targetId, page, size));
    }

    @GetMapping("/group/{groupId}")
    public Result<List<MessageDTO>> getGroupMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getGroupMessages(groupId, page, size));
    }
}
