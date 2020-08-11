package com.raft.core.rpc.message;

import com.raft.core.node.NodeId;
import com.raft.core.rpc.Channel;

// *@author liuyaolong
public  abstract class AbstractRpcMessage<T>  {

    private final T rpc;
    private final NodeId sourceNodeId;
    private final Channel channel;

    AbstractRpcMessage(T rpc,NodeId sourceNodeId,Channel channel){
        this.rpc=rpc;
        this.sourceNodeId=sourceNodeId;
        this.channel=channel;
    }

    public T get() {
        return this.rpc;
    }

    public NodeId getSourceNodeId(){
        return sourceNodeId;
    }
    public Channel getChannel(){
        return channel;
    }

}
