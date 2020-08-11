package com.raft.core.log.statemachine;

import com.raft.core.support.SingleThreadTaskExecutor;
import com.raft.core.support.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

// *@author liuyaolong
public abstract class AbstractSingleThreadStateMachine implements StateMachine{

    private static final Logger logger= LoggerFactory.getLogger(AbstractSingleThreadStateMachine.class);
    private volatile int lastApplied=0;
    private final TaskExecutor taskExecutor;
    //构造函数
    public AbstractSingleThreadStateMachine() {
        taskExecutor = new SingleThreadTaskExecutor("state-machine");
    }

    //获取lastApplied
    @Override
    public int getLastApplied() {
        return lastApplied;
    }
    //应用日志
    @Override
    public void applyLog(StateMachineContext context, int index, @Nonnull byte[] commandBytes, int firstLogIndex) {
        taskExecutor.submit(() -> doApplyLog(context, index, commandBytes, firstLogIndex));
    }
    //应用日志(TaskExecutor的线程中)
    private void doApplyLog(StateMachineContext context, int index, @Nonnull byte[] commandBytes, int firstLogIndex) {
        //忽略已经应用的日志
        if (index <= lastApplied) {
            return;
        }

        logger.debug("apply log {}", index);
        applyCommand(commandBytes);
        //更新lastApplied
        lastApplied = index;
    }
    //应用命令，抽象方法
    protected abstract void applyCommand(@Nonnull byte[] commandBytes);

    //关闭状态机
    @Override
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (InterruptedException e) {
            throw new StateMachineException(e);
        }
    }

}
