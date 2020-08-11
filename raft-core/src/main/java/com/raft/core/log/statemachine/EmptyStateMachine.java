package com.raft.core.log.statemachine;

// *@author liuyaolong
public class EmptyStateMachine implements StateMachine{

    private int lastAppled;


    @Override
    public int getLastApplied() {
        return lastAppled;
    }

    @Override
    public void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex) {
        lastAppled=index;
    }

    @Override
    public void shutdown() {

    }
}
