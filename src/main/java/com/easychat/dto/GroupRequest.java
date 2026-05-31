package com.easychat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank(message = "群名不能为空")
    private String groupName;
    @NotNull(message = "群主ID不能为空")
    private Long ownerId;
}
