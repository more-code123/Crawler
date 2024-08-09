package com.wy.scheduler.core.heartbeat;

import com.alibaba.fastjson2.JSONObject;
import com.wy.common.entity.Worker;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.wy.common.constant.HeartbeatConstant.HEARTBEAT_CLIENT;
import static com.wy.common.constant.HeartbeatConstant.HEARTBEAT_SERVER;

/**
 * @Author WY
 * @Date 2024/08/06 19:42
 * @Desc 心跳包处理类
 **/
@Slf4j
@ChannelHandler.Sharable
@Component
public class HeartbeatServerHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private HeartbeatManager heartbeatManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) {
        if (HEARTBEAT_CLIENT.equals(message)) {
            // 接收到采集节点的PING信号，返回PONG信号
            log.info("收到采集节点{}的心跳：{}，返回心跳：{}", ctx.channel().remoteAddress(), message, HEARTBEAT_SERVER);
            ctx.channel().writeAndFlush(HEARTBEAT_SERVER);
            // 更新客户端生存轮次
            heartbeatManager.updateAlive(ctx.channel().id());
        } else {
            try {
                Worker worker = JSONObject.parseObject(message, Worker.class);
                log.info("收到采集节点{}的客户端信息：{}", ctx.channel().remoteAddress(), worker.toString());
                worker.setChannel(ctx.channel());
                worker.setAliveRound(1);
                heartbeatManager.addClient(ctx.channel().id(), worker);
            } catch (Exception e) {
                log.error("心跳信息解析失败：{}", message);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("采集节点{}断开连接，注销该节点", ctx.channel().remoteAddress());
        heartbeatManager.removeClient(ctx.channel().id());
    }
}
