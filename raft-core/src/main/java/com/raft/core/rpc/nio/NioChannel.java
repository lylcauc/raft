package com.raft.core.rpc.nio;

import com.raft.core.rpc.Channel;
import com.raft.core.rpc.message.AppendEntriesResult;
import com.raft.core.rpc.message.AppendEntriesRpc;
import com.raft.core.rpc.message.RequestVoteResult;
import com.raft.core.rpc.message.RequestVoteRpc;
import io.netty.channel.ChannelException;

import javax.annotation.Nonnull;

// *@author liuyaolong
public class NioChannel implements Channel {


    private final io.netty.channel.Channel nettyChannel;

    //构造函数
    NioChannel(io.netty.channel.Channel nettyChannel){
        this.nettyChannel=nettyChannel;
    }
    //写入RequestVote消息
    @Override
    public void writeRequestVoteRpc(@Nonnull RequestVoteRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }
    //写入RequestVote响应
    @Override
    public void writeRequestVoteResult(@Nonnull RequestVoteResult result) {
        nettyChannel.writeAndFlush(result);
    }
    //写入AppendEntries消息
    @Override
    public void writeAppendEntriesRpc(@Nonnull AppendEntriesRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }
    //写入AppendEntries响应
    @Override
    public void writeAppendEntriesResult(@Nonnull AppendEntriesResult result) {
        nettyChannel.writeAndFlush(result);
    }


    //关闭
    @Override
    public void close() {
        try {
            nettyChannel.close().sync();
        } catch (InterruptedException e) {
            throw new ChannelException("failed to close", e);
        }
    }
    //获取底层Netty的Channel
     io.netty.channel.Channel getDelegate() {
        return nettyChannel;
    }
}
