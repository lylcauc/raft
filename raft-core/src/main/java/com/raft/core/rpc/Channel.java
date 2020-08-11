package com.raft.core.rpc;

import com.raft.core.rpc.message.AppendEntriesResult;
import com.raft.core.rpc.message.AppendEntriesRpc;
import com.raft.core.rpc.message.RequestVoteResult;
import com.raft.core.rpc.message.RequestVoteRpc;

import javax.annotation.Nonnull;

// *@author liuyaolong
public interface Channel {


    //发送RequestVote消息
    void writeRequestVoteRpc(@Nonnull RequestVoteRpc rpc);
    //发送RequestVote响应
    void writeRequestVoteResult(@Nonnull RequestVoteResult result);
    //发送AppendEntries消息
    void writeAppendEntriesRpc(@Nonnull AppendEntriesRpc rpc);
    //发送AppendEntries响应
    void writeAppendEntriesResult(@Nonnull AppendEntriesResult result);
    //关闭
    void close();

}
