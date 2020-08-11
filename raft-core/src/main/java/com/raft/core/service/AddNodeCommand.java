package com.raft.core.service;

import com.raft.core.node.NodeEndPoint;

// *@author liuyaolong
public class AddNodeCommand {

    private final String nodeId;
    private final String host;
    private final int port;

    public AddNodeCommand(String nodeId,String host,int port){
        this.nodeId=nodeId;
        this.host=host;
        this.port=port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public NodeEndPoint toNodeEndPoint(){
        return new NodeEndPoint(nodeId,host,port);
    }
}
