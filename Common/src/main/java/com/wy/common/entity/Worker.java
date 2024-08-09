package com.wy.common.entity;

import io.netty.channel.Channel;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author WY
 * @Date 2024/08/07 16:06
 * @Desc 节点信息
 **/
public class Worker implements Serializable {
    private static final long serialVersionUID = -2233376381275353085L;

    // 节点名称
    private String name;
    // 节点地址
    private String address;
    // 节点端口
    private String port;
    // 节点启动时间
    private Date startTime;
    // 节点管道
    private transient Channel channel;
    // 节点存活轮次
    private long aliveRound;

    public long getAliveRound() {
        return aliveRound;
    }

    public void setAliveRound(long aliveRound) {
        this.aliveRound = aliveRound;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port='" + port + '\'' +
                ", startTime=" + startTime +
                "}";
    }
}
