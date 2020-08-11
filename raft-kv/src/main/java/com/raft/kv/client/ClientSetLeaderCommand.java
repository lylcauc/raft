package com.raft.kv.client;

import com.raft.core.node.NodeId;

// *@author liuyaolong
public class ClientSetLeaderCommand implements Command{

    @Override
    public String getName() {
        return "client-set-leader";
    }

    //执行
    @Override
    public void execute(String arguments, CommandContext context) {
        //判断后续参数是否为空
        if(arguments.isEmpty()){
            throw new IllegalArgumentException("usage: "+getName()+"<node-id>");
        }
        //设置新的leader节点ID
        NodeId nodeId=new NodeId(arguments);
        try {
            context.setClientLeader(nodeId);
            System.out.println(nodeId);
        }catch (IllegalStateException e){
            System.err.println(e.getMessage());
        }

    }
}
