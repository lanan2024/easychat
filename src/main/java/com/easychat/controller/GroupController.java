package com.easychat.controller;

import com.easychat.common.Result;
import com.easychat.dto.GroupRequest;
import com.easychat.dto.UserDTO;
import com.easychat.entity.Group;
import com.easychat.service.GroupService;
import com.easychat.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;
    private final JwtUtil jwtUtil;

    public GroupController(GroupService groupService, JwtUtil jwtUtil) {
        this.groupService = groupService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public Result<Group> createGroup(@RequestHeader("Authorization") String token,
                                     @Valid @RequestBody GroupRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (!userId.equals(request.getOwnerId())) {
            return Result.error(403, "无权操作");
        }
        return Result.success(groupService.createGroup(request.getGroupName(), request.getOwnerId()));
    }

    @PostMapping("/join/{groupId}")
    public Result<Void> joinGroup(@RequestHeader("Authorization") String token,
                                  @PathVariable Long groupId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        groupService.joinGroup(groupId, userId);
        return Result.success();
    }

    @PostMapping("/quit/{groupId}")
    public Result<Void> quitGroup(@RequestHeader("Authorization") String token,
                                  @PathVariable Long groupId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        groupService.quitGroup(groupId, userId);
        return Result.success();
    }

    @PostMapping("/set-admin/{groupId}/{targetUserId}")
    public Result<Void> setAdmin(@RequestHeader("Authorization") String token,
                                 @PathVariable Long groupId,
                                 @PathVariable Long targetUserId) {
        Long operatorId = jwtUtil.getUserIdFromToken(token);
        groupService.setAdmin(groupId, targetUserId, operatorId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Group>> getUserGroups(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(groupService.getUserGroups(userId));
    }

    @GetMapping("/members/{groupId}")
    public Result<List<UserDTO>> getGroupMembers(@PathVariable Long groupId) {
        return Result.success(groupService.getGroupMembers(groupId));
    }
}
