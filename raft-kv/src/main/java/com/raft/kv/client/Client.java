package com.raft.kv.client;

import com.raft.core.service.AddNodeCommand;
import com.raft.core.service.RemoveNodeCommand;
import com.raft.core.service.ServerRouter;
import com.raft.kv.message.GetCommand;
import com.raft.kv.message.SetCommand;

// *@author liuyaolong
public class Client {

    //版本
    public static final String VERSION="0.1.0";

    //服务器路由器
    private final ServerRouter serverRouter;
    //构造函数
    public Client(ServerRouter serverRouter){
        this.serverRouter=serverRouter;
    }

    //增加节点
    public void addNode(String nodeId, String host,int port){
        serverRouter.send(new AddNodeCommand(nodeId,host,port));
    }

    //删除节点
    public void removeNode(String nodeId){
        serverRouter.send(new RemoveNodeCommand(nodeId));
    }

    //KV服务的SET命令
    public void set(String key,byte[] value){
        serverRouter.send(new SetCommand(key,value));
    }
    //KV服务的GET命令
    public byte[] get(String key){
        return (byte[]) serverRouter.send(new GetCommand(key));
    }
    //获取内部服务器路由器
    public ServerRouter getServerRouter(){
        return serverRouter;
    }


}
