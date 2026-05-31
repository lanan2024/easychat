package com.easychat.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String phone;
    private String avatar;
    private Integer status;
    private String remark;
}
