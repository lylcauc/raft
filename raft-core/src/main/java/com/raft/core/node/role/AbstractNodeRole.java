package com.raft.core.node.role;

import com.raft.core.node.NodeId;

// *@author liuyaolong
public abstract class AbstractNodeRole {


    private final RoleName name;
    protected final int term;

    //构造函数
    AbstractNodeRole(RoleName name,int term){
        this.name=name;
        this.term=term;
    }

    //获取当前得角色名
    public RoleName getName(){
        return name;
    }

    public abstract NodeId getLeaderId(NodeId selfId);

    //取消超时或者定时任务
    public abstract void cancelTimeoutOrTask();

    //获取当前得term
    public int getTerm(){
        return term;
    }

    public RoleNameAndLeaderId getNameAndLeaderId(NodeId selfId){
        return new RoleNameAndLeaderId(name,getLeaderId(selfId));
    }




}
