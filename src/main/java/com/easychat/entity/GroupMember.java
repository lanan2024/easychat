package com.easychat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_member")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer role = 0;

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @PrePersist
    protected void onCreate() {
        joinTime = LocalDateTime.now();
    }
}
