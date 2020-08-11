package com.raft.kv.message;

// *@author liuyaolong

import com.raft.core.node.NodeId;

//GET重定向
public class Redirect {

    private final String leaderId;

    //构造函数，类型为NodeId
    public Redirect(NodeId leaderId){
        this(leaderId!=null?leaderId.getValue():null);
    }
    //构造函数，类型为字符串
    public Redirect(String leaderId){
        this.leaderId=leaderId;
    }

    //获取leader的Id
    public String getLeaderId(){
        return leaderId;
    }

    @Override
    public String toString() {
        return "Redirect{" +
                "leaderId='" + leaderId + '\'' +
                '}';
    }
}
