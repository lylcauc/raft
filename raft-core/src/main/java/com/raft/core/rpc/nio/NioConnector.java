package com.raft.core.rpc.nio;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.raft.core.log.LogException;
import com.raft.core.node.NodeEndPoint;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.Channel;
import com.raft.core.rpc.ChannelConnectException;
import com.raft.core.rpc.Connector;
import com.raft.core.rpc.message.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

// *@author liuyaolong
public class NioConnector implements Connector {

    private final Logger logger= LoggerFactory.getLogger(NioConnector.class);
    //Selector的线程池，此处为单线程
    private final NioEventLoopGroup bossNioEventLoopGroup=new NioEventLoopGroup(1);

    //IO线程池，此处为固定数量多线程
    private final NioEventLoopGroup workerNioEventLoopGroup;
    //是否和上层服务等共享线程池
    private final boolean workerGroupShared;
    private final EventBus eventBus;
    //节点间通信端口
    private final int port;
    //入口channel组
    private final InboundChannelGroup inboundChannelGroup = new InboundChannelGroup();
    //出口channel组
    private final OutboundChannelGroup outboundChannelGroup;

    //构造函数
    public NioConnector(NioEventLoopGroup workerNioEventLoopGroup, boolean workerGroupShared, NodeId selfNodeId,
                        EventBus eventBus, int port) {
        this.workerNioEventLoopGroup = workerNioEventLoopGroup;
        this.workerGroupShared = workerGroupShared;
        this.eventBus = eventBus;
        this.port = port;
        outboundChannelGroup=new OutboundChannelGroup(workerNioEventLoopGroup,eventBus,selfNodeId);
    }

    public NioConnector(NioEventLoopGroup workerNioEventLoopGroup, NodeId selfNodeId, EventBus eventBus, int port) {
        this(workerNioEventLoopGroup, true, selfNodeId, eventBus, port);
    }

    @Override
    public void initialize() {
        ServerBootstrap serverBootstrap=new ServerBootstrap()
                .group(bossNioEventLoopGroup,workerNioEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //pipeline和处理器
                        ChannelPipeline pipeline=ch.pipeline();
                        pipeline.addLast(new Decoder());
                        pipeline.addLast(new Encoder());
                        pipeline.addLast(
                                new FromRemoteHandler(eventBus,inboundChannelGroup));
                    }
                });
        logger.debug("node listen on port {}",port);
        try {
            //返回一个ChannelFuture,允许异步获取结果
            serverBootstrap.bind(port).sync();
        }catch (InterruptedException e){
            throw new ConnectorException("failed to bind port",e);
        }
    }

    @Override
    public void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndPoint> destinationEndpoints) {
        Preconditions.checkNotNull(rpc);
        Preconditions.checkNotNull(destinationEndpoints);

        for(NodeEndPoint endPoint:destinationEndpoints){
            logger.debug("send {} to {}",rpc,endPoint.getId());
            try {
                getChannel(endPoint).writeRequestVoteRpc(rpc);
            }catch (Exception e){
                logger.warn("failed to send RequestVoteRpc",e);
            }

        }
    }

    @Override
    public void replyRequestVote(RequestVoteResult result, RequestVoteRpcMessage rpcMessage) {
        Preconditions.checkNotNull(result);
        Preconditions.checkNotNull(rpcMessage);
        logger.debug("reply {} to node {}", result, rpcMessage.getSourceNodeId());
        try {
            rpcMessage.getChannel().writeRequestVoteResult(result);
        } catch (Exception e) {
            logException(e);
        }
    }

    private void logException(Exception e) {
        if (e instanceof ChannelConnectException) {
            logger.warn(e.getMessage());
        } else {
            logger.warn("failed to process channel", e);
        }
    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc rpc, NodeEndPoint destinationEndpoint) {
        Preconditions.checkNotNull(rpc);
        Preconditions.checkNotNull(destinationEndpoint);
        logger.debug("send {} to node {}", rpc, destinationEndpoint.getId());
        try {
            getChannel(destinationEndpoint).writeAppendEntriesRpc(rpc);
        } catch (Exception e) {
            logException(e);
        }
    }

    @Override
    public void replyAppendEntries(AppendEntriesResult result, AppendEntriesRpcMessage rpcMessage) {
        Preconditions.checkNotNull(result);
        Preconditions.checkNotNull(rpcMessage);
        logger.debug("reply {} to node {}", result, rpcMessage.getSourceNodeId());
        try {
            rpcMessage.getChannel().writeAppendEntriesResult(result);
        } catch (Exception e) {
            logException(e);
        }
    }

    @Override
    public void resetChannels() {
        inboundChannelGroup.closeAll();
    }

    @Override
    public void close() {
        logger.debug("close connection");
        inboundChannelGroup.closeAll();
        outboundChannelGroup.closeAll();
        bossNioEventLoopGroup.shutdownGracefully();
        if(!workerGroupShared){
            workerNioEventLoopGroup.shutdownGracefully();
        }
    }

    private Channel getChannel(NodeEndPoint endPoint){
        return outboundChannelGroup.getOrConnect(endPoint.getId(),endPoint.getAddress());
    }
}
