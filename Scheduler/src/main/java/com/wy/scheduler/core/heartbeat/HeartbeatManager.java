package com.wy.scheduler.core.heartbeat;

import com.wy.common.entity.Worker;
import com.wy.common.exceptions.WorkerNotFoundException;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author WY
 * @Date 2024/08/09 14:31
 * @Desc 心跳管理器
 **/
@Component
public class HeartbeatManager {
    private static final ConcurrentHashMap<ChannelId, Worker> clientMap = new ConcurrentHashMap<>();

    public void addClient(ChannelId channelId, Worker client) {
        clientMap.put(channelId, client);
    }

    public void removeClient(ChannelId channelId) {
        clientMap.remove(channelId);
    }

    public void updateAlive(ChannelId channelId) {
        Worker client = this.getClient(channelId);
        client.setAliveRound(client.getAliveRound() + 1);
        clientMap.put(channelId, client);
    }

    public Worker getClient(ChannelId channelId) {
        Worker client = clientMap.get(channelId);
        if (Objects.isNull(client)) {
            throw new WorkerNotFoundException("client not found");
        }
        return client;
    }

    public ConcurrentMap<ChannelId, Worker> getClientMap() {
        return clientMap;
    }
}
