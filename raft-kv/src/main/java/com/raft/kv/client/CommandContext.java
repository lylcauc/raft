package com.raft.kv.client;

import com.raft.core.node.NodeId;
import com.raft.core.rpc.Address;
import com.raft.core.service.ServerRouter;

import java.util.Map;

// *@author liuyaolong


//命令上下文，包含集群成员列表和KV客户端实际命令的实现
public class CommandContext {

    //服务器列表
    private final Map<NodeId, Address> serverMap;
    //客户端
    private Client client;
    //是否在运行
    private boolean running=false;
    //构造函数
    public CommandContext(Map<NodeId,Address> serverMap){
        this.serverMap=serverMap;
        this.client=new Client(buildServerRouter(serverMap));
    }
    //构建ServerRouter(服务器路由)
    private ServerRouter buildServerRouter(Map<NodeId, Address> serverMap) {
        ServerRouter router = new ServerRouter();
        for (NodeId nodeId : serverMap.keySet()) {
            Address address = serverMap.get(nodeId);
            router.add(nodeId, new SocketChannel(address.getHost(), address.getPort()));
        }
        return router;
    }

    Client getClient(){
        return client;
    }

    void setClientLeader(NodeId nodeId){
        client.getServerRouter().setLeaderId(nodeId);
    }

    NodeId getClientLeader() {
        return client.getServerRouter().getLeaderId();
    }
    void clientAddServer(String nodeId,String host,int portService){
        serverMap.put(new NodeId(nodeId),new Address(host,portService));
        client=new Client(buildServerRouter(serverMap));
    }

    boolean clientRemoveServer(String nodeId){
        Address address=serverMap.remove(new NodeId(nodeId));
        if(address !=null){
            client=new Client(buildServerRouter(serverMap));
            return true;
        }
        return false;
    }

    void setRunning(boolean running){
        this.running=running;
    }

    boolean isRunning(){
        return running;
    }

    void printSeverList() {
        for (NodeId nodeId : serverMap.keySet()) {
            Address address = serverMap.get(nodeId);
            System.out.println(nodeId + "," + address.getHost() + "," + address.getPort());
        }
    }

}
