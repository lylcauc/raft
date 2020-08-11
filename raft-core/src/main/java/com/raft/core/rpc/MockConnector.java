package com.raft.core.rpc;

import com.raft.core.node.NodeEndPoint;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

// *@author liuyaolong
public class MockConnector extends ConnectorAdapter {

    private LinkedList<Message> messages=new LinkedList<>();
    //初始化
//    @Override
//    public void initialize() {
//
//    }
    //发送消息
    @Override
    public void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndPoint> destinationEndpoints) {
        //对于多目标节点，这里没有完全处理
        Message m = new Message();
        m.rpc = rpc;
        messages.add(m);
    }

    @Override
    public void replyRequestVote(RequestVoteResult result, RequestVoteRpcMessage rpcMessage) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = rpcMessage.getSourceNodeId();
        messages.add(m);
    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc rpc, NodeEndPoint destinationEndpoint) {
        Message m = new Message();
        m.rpc = rpc;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Override
    public void replyAppendEntries(AppendEntriesResult result, AppendEntriesRpcMessage rpcMessage) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = rpcMessage.getSourceNodeId();
        messages.add(m);
    }

    @Override
    public void resetChannels() {

    }

    //关闭
    @Override
    public void close() {

    }

    //获取最后一条消息
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }

    //获取最后一条消息或者空消息
    private Message getLastMessageOrDefault() {
        return messages.isEmpty() ? new Message() : messages.getLast();
    }
    //获取最后一条rpc消息
    public Object getRpc() {
        return getLastMessageOrDefault().rpc;
    }
    //获取最后一条result消息
    public Object getResult() {
        return getLastMessageOrDefault().result;
    }
    //获取最后一条消息的目标节点
    public NodeId getDestinationNodeId() {
        return getLastMessageOrDefault().destinationNodeId;
    }
    //获取消息数量
    public int getMessageCount() {
        return messages.size();
    }
    //获取所有消息
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    //清除消息
    public void clearMessage() {
        messages.clear();
    }

    public static class Message{
        private Object rpc; //RPC消息
        private NodeId destinationNodeId;//目标节点
        private Object result;//结果

        //获取rpc消息
        public Object getRpc() {
            return rpc;
        }
        //获取目标节点
        public NodeId getDestinationNodeId() {
            return destinationNodeId;
        }
        //获取结果
        public Object getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "destinationNodeId=" + destinationNodeId +
                    ", rpc=" + rpc +
                    ", result=" + result +
                    '}';
        }
    }
}
