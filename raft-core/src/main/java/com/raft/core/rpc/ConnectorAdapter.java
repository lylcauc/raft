package com.raft.core.rpc;

import com.raft.core.node.NodeEndPoint;
import com.raft.core.rpc.message.*;

import javax.annotation.Nonnull;
import java.util.Collection;

// *@author liuyaolong
public abstract class ConnectorAdapter implements Connector{
    @Override
    public void initialize() {
    }

    @Override
    public void sendRequestVote(@Nonnull RequestVoteRpc rpc, @Nonnull Collection<NodeEndPoint> destinationEndpoints) {
    }

    @Override
    public void replyRequestVote(@Nonnull RequestVoteResult result, @Nonnull RequestVoteRpcMessage rpcMessage) {

    }

    @Override
    public void sendAppendEntries(@Nonnull AppendEntriesRpc rpc, @Nonnull NodeEndPoint destinationEndpoint) {

    }

    @Override
    public void replyAppendEntries(@Nonnull AppendEntriesResult result, @Nonnull AppendEntriesRpcMessage rpcMessage) {

    }

    @Override
    public void resetChannels() {
    }

    @Override
    public void close() {
    }
}
