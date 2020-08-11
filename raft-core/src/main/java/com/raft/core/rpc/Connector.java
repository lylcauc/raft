package com.raft.core.rpc;

import com.raft.core.node.NodeEndPoint;
import com.raft.core.rpc.message.*;

import java.util.Collection;

// *@author liuyaolong
public interface Connector {

    //初始化
    void initialize();

    //发送RequestVote消息给多个节点
    void sendRequestVote(RequestVoteRpc rpc,
                         Collection<NodeEndPoint> destinationEndpoints);

    //回复RequestVote结果给单个节点
//    void replyRequestVote(RequestVoteResult result,
//                          NodeEndPoint destinationEndpoint);
    void replyRequestVote(RequestVoteResult result,
                          RequestVoteRpcMessage rpcMessage);

    //发送AppendEntries消息给单个节点
    void sendAppendEntries(AppendEntriesRpc rpc,
                           NodeEndPoint destinationEndpoint);

    //回复AppendEntries结果给单个节点
//    void replyAppendEntries(AppendEntriesResult result,
//                            NodeEndPoint destinationEndPoint);

    void replyAppendEntries(AppendEntriesResult result,
                            AppendEntriesRpcMessage rpcMessage);


    //重置连接
    void resetChannels();
    //关闭通信组件
    void close();
}
