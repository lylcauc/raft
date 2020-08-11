package com.raft.core.rpc.nio;

// *@author liuyaolong

import com.google.common.eventbus.EventBus;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.Channel;
import com.raft.core.rpc.message.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

abstract class AbstractHandler extends ChannelDuplexHandler {

    private static final Logger logger= LoggerFactory.getLogger(AbstractHandler.class);

    protected final EventBus eventBus;
    NodeId remoteId;// 远程节点ID
    //RPC组件中的Channel,非Netty的Channel
    protected Channel channel;
    //最后发送的AppendEntriesRpc消息
    private AppendEntriesRpc lastAppendEntriesRpc;

    //构造函数
    AbstractHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        assert remoteId != null;
        assert channel != null;
        //判断类型后转发消息
        if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc) msg;
            eventBus.post(new RequestVoteRpcMessage(rpc, remoteId, channel));
        }
        else if (msg instanceof RequestVoteResult) {
            eventBus.post(msg);
        }
        else if (msg instanceof AppendEntriesRpc) {
            AppendEntriesRpc rpc = (AppendEntriesRpc) msg;
            eventBus.post(new AppendEntriesRpcMessage(rpc, remoteId, channel));
        }
        else if (msg instanceof AppendEntriesResult) {
            AppendEntriesResult result = (AppendEntriesResult) msg;
            if (lastAppendEntriesRpc == null) {
                logger.warn("no last append entries rpc");
            } else {
                eventBus.post(new AppendEntriesResultMessage(
                        result,remoteId,lastAppendEntriesRpc));
                lastAppendEntriesRpc=null;
            }
        }
//        else if (msg instanceof InstallSnapshotRpc) {
//            InstallSnapshotRpc rpc = (InstallSnapshotRpc) msg;
//            eventBus.post(new InstallSnapshotRpcMessage(rpc, remoteId, channel));
//        } else if (msg instanceof InstallSnapshotResult) {
//            InstallSnapshotResult result = (InstallSnapshotResult) msg;
//            assert lastInstallSnapshotRpc != null;
//            eventBus.post(new InstallSnapshotResultMessage(result, remoteId, lastInstallSnapshotRpc));
//            lastInstallSnapshotRpc = null;
//        }
    }
    //发送前记录最后一个AppendEntries消息
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof AppendEntriesRpc) {
            lastAppendEntriesRpc = (AppendEntriesRpc) msg;
        }
//        else if (msg instanceof InstallSnapshotRpc) {
//            lastInstallSnapshotRpc = (InstallSnapshotRpc) msg;
//        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn(cause.getMessage(), cause);
        ctx.close();
    }


}
