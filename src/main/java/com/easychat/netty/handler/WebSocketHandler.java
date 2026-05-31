package com.easychat.netty.handler;

import com.easychat.common.WebSocketSessionManager;
import com.easychat.dto.MessageDTO;
import com.easychat.service.MessageService;
import com.easychat.util.JsonUtil;
import com.easychat.util.JwtUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    private final WebSocketSessionManager sessionManager;
    private final JsonUtil jsonUtil;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    public WebSocketHandler(WebSocketSessionManager sessionManager, JsonUtil jsonUtil,
                            JwtUtil jwtUtil, MessageService messageService) {
        this.sessionManager = sessionManager;
        this.jsonUtil = jsonUtil;
        this.jwtUtil = jwtUtil;
        this.messageService = messageService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("New WebSocket connection: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        sessionManager.removeChannel(channel);
        log.info("WebSocket disconnected: {}", channel.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame textFrame) {
            String text = textFrame.text();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = jsonUtil.fromJson(text, Map.class);
                String type = (String) msg.get("type");
                if (type == null) return;

                switch (type) {
                    case "AUTH" -> handleAuth(ctx, msg);
                    case "PING" -> handlePing(ctx);
                    case "PONG" -> handlePong(ctx);
                    case "SINGLE_SEND" -> handleSingleSend(ctx, msg);
                    case "GROUP_SEND" -> handleGroupSend(ctx, msg);
                    case "READ_ACK" -> handleReadAck(msg);
                    default -> log.warn("Unknown message type: {}", type);
                }
            } catch (Exception e) {
                log.error("Error processing message", e);
                sendError(ctx, "消息处理失败");
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Heartbeat timeout, closing channel: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket error", cause);
        ctx.close();
    }

    private void handleAuth(ChannelHandlerContext ctx, Map<String, Object> msg) {
        String token = (String) msg.get("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            sendError(ctx, "认证失败");
            return;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        sessionManager.addChannel(userId, ctx.channel());
        sendJson(ctx, Map.of("type", "AUTH_OK", "userId", userId));
        messageService.pushOfflineMessages(userId);
        log.info("User {} authenticated via WebSocket", userId);
    }

    private void handlePing(ChannelHandlerContext ctx) {
        sendJson(ctx, Map.of("type", "PONG"));
    }

    private void handlePong(ChannelHandlerContext ctx) {
        sendJson(ctx, Map.of("type", "PONG_OK"));
    }

    private void handleSingleSend(ChannelHandlerContext ctx, Map<String, Object> msg) {
        Long fromId = getUserIdFromChannel(ctx.channel());
        if (fromId == null) return;

        Long toId = Long.valueOf(msg.get("toId").toString());
        String content = (String) msg.get("content");

        MessageDTO saved = messageService.sendSingleMessage(fromId, toId, content);
        messageService.sendMessageToUser(toId, saved);
        sendJson(ctx, Map.of("type", "SEND_OK", "msgId", saved.getMsgId()));
    }

    private void handleGroupSend(ChannelHandlerContext ctx, Map<String, Object> msg) {
        Long fromId = getUserIdFromChannel(ctx.channel());
        if (fromId == null) return;

        Long groupId = Long.valueOf(msg.get("groupId").toString());
        String content = (String) msg.get("content");

        messageService.sendGroupMessage(fromId, groupId, content);
        sendJson(ctx, Map.of("type", "SEND_OK"));
    }

    private void handleReadAck(Map<String, Object> msg) {
        Long msgId = Long.valueOf(msg.get("msgId").toString());
        messageService.markAsRead(msgId);
    }

    private Long getUserIdFromChannel(Channel channel) {
        return sessionManager.getAllChannels().entrySet().stream()
                .filter(e -> e.getValue().equals(channel))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void sendJson(ChannelHandlerContext ctx, Object obj) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(new TextWebSocketFrame(jsonUtil.toJson(obj)));
        }
    }

    private void sendError(ChannelHandlerContext ctx, String message) {
        sendJson(ctx, Map.of("type", "ERROR", "message", message));
    }
}
