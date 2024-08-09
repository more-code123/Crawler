package com.wy.node.core.heartbeat;

import com.alibaba.fastjson2.JSONObject;
import com.wy.common.constant.HeartbeatConstant;
import com.wy.common.entity.Worker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author WY
 * @Date 2024/08/09 08:58
 * @Desc
 **/
@Slf4j
@Component
public class HeartbeatClientHandler extends ChannelInboundHandlerAdapter {

    private final HeartbeatClient heartbeatClient;

    private ScheduledExecutorService scheduledExecutorService;

    public HeartbeatClientHandler(HeartbeatClient heartbeatClient) {
        this.heartbeatClient = heartbeatClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 发送一个工作节点信息
        Worker worker = new Worker();
        worker.setName(heartbeatClient.workerName);
        worker.setAddress(String.valueOf(ctx.channel().localAddress()));
        worker.setPort(String.valueOf(ctx.channel().localAddress()));
        worker.setStartTime(new Date());
        log.info("管道注册成功，发送采集节点信息到调度节点");
        ctx.writeAndFlush(JSONObject.toJSONString(worker));

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (ctx.channel().isActive()) {
                log.info("向调度节点的{}心跳：{}", ctx.channel().remoteAddress(), HeartbeatConstant.HEARTBEAT_CLIENT);
                ctx.channel().writeAndFlush(HeartbeatConstant.HEARTBEAT_CLIENT);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("收到调度节点的心跳：{}", msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error("调度节点连接失败，尝试重连...");
        if (scheduledExecutorService != null) {
            // 关闭发送心跳的单线程实例
            scheduledExecutorService.shutdown();
        }
        // 尝试重启
        heartbeatClient.start();
    }
}
