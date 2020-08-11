package com.raft.core.service;

import com.raft.core.node.NodeId;

// *@author liuyaolong
public class RemoveNodeCommand {

    private final NodeId nodeId;

    public RemoveNodeCommand(String nodeId){
        this.nodeId=new NodeId(nodeId);
    }

    public NodeId getNodeId(){
        return nodeId;
    }

}
