package com.raft.core.rpc.nio;

import com.google.common.eventbus.EventBus;
import com.raft.core.node.NodeId;
import io.netty.channel.ChannelHandlerContext;

// *@author liuyaolong
public class ToRemoteHandler extends AbstractHandler {

    private final NodeId selfNodeId;

    //构造函数
    ToRemoteHandler(EventBus eventBus,NodeId remoteId,NodeId selfNodeId) {
        super(eventBus);
        this.remoteId=remoteId;
        this.selfNodeId=selfNodeId;
    }

    //连接成功后发送自己的节点Id
    public void channelActive(ChannelHandlerContext ctx){
        ctx.write(selfNodeId);
        channel=new NioChannel(ctx.channel());
    }

}
