package com.wy.node.core.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author WY
 * @Date 2024/08/09 08:38
 * @Desc 采集节点心跳服务
 **/
@Slf4j
@Component
public class HeartbeatClient implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    @Value("${heartbeat.host}")
    private String host;

    @Value("${heartbeat.port}")
    private int port;

    @Value("${heartbeat.interval}")
    private long interval;

    @Value("${worker.name}")
    public String workerName;

    private EventLoopGroup eventLoopGroup;

    private ChannelFuture future;

    public void start() throws InterruptedException {
        Bootstrap bootstrap;
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        // 初始化Netty配置
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        // 添加心跳客户端处理器
                        nioSocketChannel.pipeline()
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new HeartbeatClientHandler(HeartbeatClient.this));
                    }
                });

        future = bootstrap.connect(host, port);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("采集节点心跳服务启动成功...");
            } else {
                channelFuture.channel().eventLoop().schedule(() -> {
                    try {
                        log.warn("调度节点心跳服务连接失败，尝试重连...");
                        start();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, interval, TimeUnit.MILLISECONDS);
            }
        });
        future.channel().closeFuture().sync();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始启动采集节点心跳服务...");
        this.start();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("关闭心跳服务...");
        if (future != null) {
            future.channel().close();
        }
        eventLoopGroup.shutdownGracefully();
        log.info("心跳服务已停止");
    }
}
