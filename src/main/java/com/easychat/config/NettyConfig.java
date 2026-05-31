package com.easychat.config;

import com.easychat.netty.NettyServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyConfig {

    private final NettyServer nettyServer;

    public NettyConfig(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @PostConstruct
    public void startNetty() {
        if (nettyServer.getPort() > 0) {
            nettyServer.start();
        }
    }

    @PreDestroy
    public void stopNetty() {
        nettyServer.stop();
    }
}
