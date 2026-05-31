package com.easychat.netty;

import com.easychat.netty.handler.WebSocketHandler;
import com.easychat.util.JsonUtil;
import com.easychat.util.JwtUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WebSocketHandlerTest {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JsonUtil jsonUtil;

    private EmbeddedChannel channel;
    private String validToken;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(webSocketHandler);
        validToken = jwtUtil.generateToken(1L, "testuser");
    }

    @Test
    void testAuth_Success() {
        Map<String, Object> authMsg = Map.of("type", "AUTH", "token", validToken);
        channel.writeInbound(new TextWebSocketFrame(jsonUtil.toJson(authMsg)));

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response);
        String text = response.text();
        assertTrue(text.contains("AUTH_OK"));
    }

    @Test
    void testAuth_InvalidToken() {
        Map<String, Object> authMsg = Map.of("type", "AUTH", "token", "invalid-token");
        channel.writeInbound(new TextWebSocketFrame(jsonUtil.toJson(authMsg)));

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response);
        String text = response.text();
        assertTrue(text.contains("ERROR"));
    }

    @Test
    void testPong() {
        Map<String, Object> pong = Map.of("type", "PONG");
        channel.writeInbound(new TextWebSocketFrame(jsonUtil.toJson(pong)));

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response);
        String text = response.text();
        assertTrue(text.contains("PONG_OK"));
    }

    @Test
    void testUnknownType() {
        Map<String, Object> unknown = Map.of("type", "UNKNOWN");
        channel.writeInbound(new TextWebSocketFrame(jsonUtil.toJson(unknown)));

        TextWebSocketFrame response = channel.readOutbound();
        assertNull(response);
    }

    @Test
    void testMalformedJson() {
        channel.writeInbound(new TextWebSocketFrame("not-json-at-all"));

        TextWebSocketFrame response = channel.readOutbound();
        assertNotNull(response);
        String text = response.text();
        assertTrue(text.contains("ERROR"));
    }
}
