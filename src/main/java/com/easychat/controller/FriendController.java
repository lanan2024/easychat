package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.FriendRequest;
import com.easychat.dto.UserDTO;
import com.easychat.service.FriendService;
import com.easychat.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    private final FriendService friendService;
    private final JwtUtil jwtUtil;

    public FriendController(FriendService friendService, JwtUtil jwtUtil) {
        this.friendService = friendService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/add")
    public Result<Void> addFriend(@RequestHeader("Authorization") String token,
                                  @Valid @RequestBody FriendRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        friendService.addFriend(userId, request.getFriendId(), request.getRemark(), request.getGroupId());
        return Result.success();
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@RequestHeader("Authorization") String token,
                                     @PathVariable Long friendId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<UserDTO>> getFriends(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(friendService.getFriends(userId));
    }
}
