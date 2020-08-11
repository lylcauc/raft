package com.raft.core.rpc.message;

import com.raft.core.node.NodeId;
import com.raft.core.rpc.Channel;

// *@author liuyaolong
public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }
}
