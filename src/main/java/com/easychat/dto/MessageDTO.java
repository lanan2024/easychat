package com.easychat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long msgId;
    private Long fromId;
    private String fromUsername;
    private String fromAvatar;
    private Long toId;
    private Integer msgType;
    private String content;
    private LocalDateTime sendTime;
    private Integer status;
}
