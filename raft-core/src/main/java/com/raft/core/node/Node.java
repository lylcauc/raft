package com.raft.core.node;

import com.raft.core.log.statemachine.StateMachine;
import com.raft.core.node.role.RoleNameAndLeaderId;

// *@author liuyaolong
public interface Node {

    //启动
    void start();
    //关闭
    void stop() throws InterruptedException;

    //注册状态机
    void registerStateMachine(StateMachine stateMachine);

    //追加日志
    void appendLog(byte[] commandBytes);

    RoleNameAndLeaderId getRoleNameAndLeaderId();


}
