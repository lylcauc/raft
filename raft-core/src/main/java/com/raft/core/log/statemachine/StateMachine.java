package com.raft.core.log.statemachine;

import com.raft.core.log.entry.Entry;

// *@author liuyaolong
public interface StateMachine {

    //void applyEntry(Entry entry);
    //获取lastApplied
    int getLastApplied();
    //应用日志
    void applyLog(StateMachineContext context,int index,
                  byte[] commandBytes,int firstLogIndex);
    //关闭状态机
    void shutdown();

}
