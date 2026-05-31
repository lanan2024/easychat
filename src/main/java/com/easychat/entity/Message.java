package com.easychat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long msgId;

    @Column(name = "from_id", nullable = false)
    private Long fromId;

    @Column(name = "to_id", nullable = false)
    private Long toId;

    @Column(name = "msg_type", nullable = false)
    private Integer msgType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    @Column(nullable = false)
    private Integer status = 0;

    @PrePersist
    protected void onCreate() {
        sendTime = LocalDateTime.now();
    }
}
