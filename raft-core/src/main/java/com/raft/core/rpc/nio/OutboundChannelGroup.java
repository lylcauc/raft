package com.raft.core.rpc.nio;

import com.google.common.eventbus.EventBus;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.Address;
import com.raft.core.rpc.ChannelConnectException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

// *@author liuyaolong
class OutboundChannelGroup {
    private static final Logger logger= LoggerFactory.getLogger(OutboundChannelGroup.class);

    private final EventLoopGroup workerGroup;
    private final EventBus eventBus;
    private final NodeId selfNodeId;
    private final ConcurrentHashMap<NodeId, Future<NioChannel>> channelMap=new ConcurrentHashMap<>();

    //构造函数
    public OutboundChannelGroup(EventLoopGroup workerGroup, EventBus eventBus, NodeId selfNodeId) {
        this.workerGroup = workerGroup;
        this.eventBus = eventBus;
        this.selfNodeId = selfNodeId;
    }


    private NioChannel connect(NodeId nodeId, Address address) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new Decoder());
                        pipeline.addLast(new Encoder());
                        pipeline.addLast(new ToRemoteHandler(eventBus, nodeId, selfNodeId));
                    }
                });
        ChannelFuture future = bootstrap.connect(address.getHost(), address.getPort()).sync();
        //同步等待连接完成
        if (!future.isSuccess()) {
            throw new ChannelException("failed to connect", future.cause());
        }
        logger.debug("channel OUTBOUND-{} connected", nodeId);
        Channel nettyChannel = future.channel();
        //连接关闭后从组里删除
        nettyChannel.closeFuture().addListener((ChannelFutureListener) cf -> {
            logger.debug("channel OUTBOUND-{} disconnected", nodeId);
            channelMap.remove(nodeId);
        });
        return new NioChannel(nettyChannel);
    }


    //获取已有的连接或者新建连接
    NioChannel getOrConnect(NodeId nodeId, Address address) {
        Future<NioChannel> future = channelMap.get(nodeId);
        if (future == null) {
            FutureTask<NioChannel> newFuture = new FutureTask<>(() -> connect(nodeId, address));
            future = channelMap.putIfAbsent(nodeId, newFuture);
            if (future == null) {
                future = newFuture;
                //在当前线程执行connect方法
                newFuture.run();
            }
        }
        //等待其他线程执行完毕或者自己执行到这一步
        try {
            return future.get();
        } catch (Exception e) {
            channelMap.remove(nodeId);
            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof ConnectException) {
                    throw new ChannelConnectException("failed to get channel to node " + nodeId +
                            ", cause " + cause.getMessage(), cause);
                }
            }
            throw new ChannelException("failed to get channel to node " + nodeId, e);
        }
    }

    void closeAll() {
        logger.debug("close all outbound channels");
        channelMap.forEach((nodeId, nioChannelFuture) -> {
            try {
                nioChannelFuture.get().close();
            } catch (Exception e) {
                logger.warn("failed to close", e);
            }
        });
    }



}
