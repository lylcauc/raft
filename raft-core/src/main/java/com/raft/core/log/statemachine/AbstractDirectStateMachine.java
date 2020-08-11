package com.raft.core.log.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// *@author liuyaolong
public abstract class AbstractDirectStateMachine implements StateMachine{

    private static final Logger logger= LoggerFactory.getLogger(AbstractDirectStateMachine.class);
    protected int lastApplied;
    //获取lastApplied
    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    //应用日志
    @Override
    public void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex) {
        if(index<=lastApplied){
            return;
        }
        logger.debug("apply log{}" ,index);
        applyCommand(commandBytes);
        lastApplied=index;
    }
    //应用命令
    protected abstract void applyCommand(byte[] commandBytes);


    @Override
    public void shutdown() {

    }
}
