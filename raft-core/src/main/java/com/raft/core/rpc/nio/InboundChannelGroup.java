package com.raft.core.rpc.nio;

import com.raft.core.node.NodeId;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// *@author liuyaolong
public class InboundChannelGroup {

    private static final Logger logger= LoggerFactory.getLogger(InboundChannelGroup.class);

    private final List<NioChannel> channels=new CopyOnWriteArrayList<>();

    //增加入口连接
    public void add(NodeId remoteId, NioChannel channel) {
        logger.debug("channel INBOUND-{} connected", remoteId);
        channel.getDelegate().closeFuture().addListener((ChannelFutureListener) future -> {
            //连接关闭时移除
            logger.debug("channel INBOUND-{} disconnected", remoteId);
            remove(channel);
        });
    }
    //移除连接
    private void remove(NioChannel channel) {
        channels.remove(channel);
    }
    //关闭所有连接
    void closeAll() {
        logger.debug("close all inbound channels");
        for (NioChannel channel : channels) {
            channel.close();
        }
    }

}
