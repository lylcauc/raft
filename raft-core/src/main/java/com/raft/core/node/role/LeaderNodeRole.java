package com.raft.core.node.role;

import com.raft.core.node.NodeId;
import com.raft.core.schedule.LogReplicationTask;

// *@author liuyaolong
public class LeaderNodeRole extends AbstractNodeRole {

    private final LogReplicationTask logReplicationTask;//日志复制定时器



    public LeaderNodeRole(int term,LogReplicationTask logReplicationTask){
        super(RoleName.LEADER,term);
        this.logReplicationTask=logReplicationTask;
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return selfId;
    }

    //取消日志复制定时任务
    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

    @Override
    public String toString() {
        return "LeaderNodeRole{" +
                "logReplicationTask=" + logReplicationTask +
                ", term=" + term +
                '}';
    }
}
