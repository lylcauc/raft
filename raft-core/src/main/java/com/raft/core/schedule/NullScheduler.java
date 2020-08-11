package com.raft.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// *@author liuyaolong
public class NullScheduler implements Scheduler {

    private static final Logger logger= LoggerFactory.getLogger(NullScheduler.class);

    //创建日志复制定时器
    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        logger.debug("schedule log replication task");
        return LogReplicationTask.NONE;
    }

    //创建选举超时定时器
    @Override
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        logger.debug("schedule election timeout");
        return ElectionTimeout.NONE;
    }

    //关闭定时器
    @Override
    public void stop() throws InterruptedException {

    }
}
