-- EasyChat 数据库初始化脚本
-- 如果使用 spring.jpa.hibernate.ddl-auto=update 则无需手动执行

CREATE DATABASE IF NOT EXISTS easychat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE easychat;

CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(16),
    avatar VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-离线 1-在线',
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    group_id BIGINT DEFAULT 0,
    remark VARCHAR(32),
    create_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_group (
    group_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(32) NOT NULL,
    owner_id BIGINT NOT NULL,
    notice VARCHAR(255),
    create_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0-普通 1-管理员',
    join_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message (
    msg_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_id BIGINT NOT NULL,
    to_id BIGINT NOT NULL,
    msg_type TINYINT NOT NULL COMMENT '1-单聊 2-群聊',
    content TEXT,
    send_time DATETIME,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
