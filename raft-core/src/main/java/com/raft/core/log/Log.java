package com.raft.core.log;

// *@author liuyaolong


import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryMeta;
import com.raft.core.log.entry.GeneralEntry;
import com.raft.core.log.entry.NoOpEntry;
import com.raft.core.log.statemachine.StateMachine;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.AppendEntriesRpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Log {

    int ALL_ENTRIES=-1;

     //获取最后一条日志的元信息
    @Nonnull
    EntryMeta getLastEntryMeta();

    //创建AppendEntries消息
    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);


    //获取下一条日志的索引
    int getNextIndex();

    //获取当前的commitIndex
    int getCommitIndex();


     //判断对象的lastLogIndex和lastLogTerm是否比自己新
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);

     //增加一个NO-OP日志
    NoOpEntry appendEntry(int term);


    //增加一条普通日志
    GeneralEntry appendEntry(int term, byte[] command);

    //追加来自Leader的日志条目
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    //推进commitIndex
    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    void setStateMachine(StateMachine stateMachine);

    //关闭日志组件
    void close();
}
