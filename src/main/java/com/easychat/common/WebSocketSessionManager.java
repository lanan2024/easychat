package com.easychat.common;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Map<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    public void addChannel(Long userId, Channel channel) {
        userChannelMap.put(userId, channel);
        allChannels.add(channel);
    }

    public void removeChannel(Channel channel) {
        allChannels.remove(channel);
        userChannelMap.entrySet().removeIf(entry -> entry.getValue().equals(channel));
    }

    public Channel getChannel(Long userId) {
        return userChannelMap.get(userId);
    }

    public boolean isOnline(Long userId) {
        Channel channel = userChannelMap.get(userId);
        return channel != null && channel.isActive();
    }

    public void removeUser(Long userId) {
        userChannelMap.remove(userId);
    }

    public int onlineCount() {
        return (int) userChannelMap.values().stream().filter(Channel::isActive).count();
    }

    public Map<Long, Channel> getAllChannels() {
        return userChannelMap;
    }
}
