package com.easychat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendRequest {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
    private String remark;
    private Long groupId;
}
