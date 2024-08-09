package com.wy.scheduler.core.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author WY
 * @Date 2024/08/06 19:36
 * @Desc 心跳包服务
 **/
@Slf4j
@Component
public class HeartbeatServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {
    @Value("${heartbeat.port}")
    private int port;

    @Value("${heartbeat.interval}")
    private long interval;

    @Resource
    private HeartbeatServerHandler heartbeatServerHandler;

    private ChannelFuture future;

    private final EventLoopGroup boss = new NioEventLoopGroup();

    private final EventLoopGroup worker = new NioEventLoopGroup();

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(boss,worker)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        // 初始化管道
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast( new StringEncoder());
                            pipeline.addLast(new IdleStateHandler(interval, interval, interval, TimeUnit.MILLISECONDS));
                            pipeline.addLast(heartbeatServerHandler);
                        }
                    });
            future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        this.start();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("关闭心跳服务...");
        if (future != null) {
            future.channel().close();
        }
        boss.shutdownGracefully();
        worker.shutdownGracefully();
        log.info("心跳服务已停止");
    }
}
