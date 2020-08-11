package com.raft.core.log.statemachine;

// *@author liuyaolong
public interface StateMachineContext {

    void generateSnapshot(int lastIncludedIndex);
}
