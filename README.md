# EasyChat

仿微信即时通讯系统 — 基于 Spring Boot + Netty + Vue 3 + Electron 的桌面聊天应用。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.4.4 + Java 21 |
| 数据库 | MySQL + H2（测试） |
| 缓存 | Redis |
| 即时通讯 | Netty 4（WebSocket） |
| 认证 | JWT |
| 前端 | Vue 3 + Vite + Pinia |
| 桌面端 | Electron 33 |
| 打包 | electron-builder（NSIS / AppImage） |

## 快速开始

### 前置要求

- JDK 21+
- MySQL 8.0+
- Redis（可选，可注释）
- Node.js 18+ / pnpm

### 启动后端

```powershell
cd easychat
.\mvnw.cmd spring-boot:run
```

REST API → `http://localhost:8080/api`
WebSocket → `ws://localhost:9090/ws`

### 启动前端（开发模式）

```powershell
cd easychat\frontend
pnpm install
pnpm dev
```

浏览器打开 `http://localhost:5173`

### 打包桌面客户端

```powershell
cd easychat\frontend
pnpm electron:build
```

输出在 `frontend\dist-electron\EasyChat Setup <version>.exe`

## 局域网通信

### 服务端

在 A 机器启动后端，查看本机 LAN IP：

```powershell
ipconfig
# 找到 WLAN 的 IPv4 地址，如 192.168.1.100
```

### 客户端

其他机器（或本机）安装客户端后，通过 `--server` 参数指定后端 IP：

```powershell
EasyChat.exe --server 192.168.1.100
```

不传 `--server` 则默认连接 `localhost`。

浏览器开发模式可通过 `localStorage` 指定：

```js
localStorage.setItem('serverHost', '192.168.1.100')
```

## 项目结构

```
easychat/
├── src/                          # 后端 Spring Boot
│   ├── main/java/com/easychat/
│   │   ├── common/               # 全局异常、统一返回、Session 管理
│   │   ├── config/               # CORS、JWT 拦截器、Netty、Redis
│   │   ├── controller/           # REST 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # JPA 实体（User/Friend/Group/GroupMember/Message）
│   │   ├── netty/handler/        # Netty WebSocket 消息分发
│   │   ├── repository/           # JPA Repository
│   │   ├── service/              # 业务逻辑
│   │   └── util/                 # JWT、JSON、加密工具
│   └── test/
├── frontend/                     # 前端 Vue 3 + Electron
│   ├── electron/                 # Electron 主进程 + preload
│   ├── src/
│   │   ├── api/                  # Axios HTTP 封装
│   │   ├── store/                # Pinia 状态管理
│   │   ├── utils/                # WebSocket 客户端
│   │   ├── views/                # 页面组件
│   │   └── config.js             # 服务器地址配置
│   └── package.json
├── docs/                         # 设计文档
└── pom.xml
```

## WebSocket 消息协议

| type | 方向 | 说明 |
|---|---|---|
| AUTH | C→S | 认证（携带 JWT token） |
| AUTH_OK | S→C | 认证成功 |
| PING | C→S | 心跳 |
| PONG | S→C | 心跳回复 |
| SINGLE_SEND | C→S | 发送单聊消息 |
| GROUP_SEND | C→S | 发送群聊消息 |
| NEW_MESSAGE | S→C | 新消息推送 |
| READ_ACK | C→S | 消息已读确认 |
| SEND_OK | S→C | 消息发送成功 |
| ERROR | S→C | 错误通知 |

## 测试

```powershell
.\mvnw.cmd test
# 40 项测试，覆盖 Service、Controller、WebSocket Handler
```

## 版本

当前版本：1.3.0

由 [opencode](https://opencode.ai) 辅助开发
