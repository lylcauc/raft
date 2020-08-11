package com.raft.core.service;

import com.raft.core.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// *@author liuyaolong

//客户端内部负责处理KV服务端重定向和选择Leader节点的路由器类
public class ServerRouter {

    private static Logger logger= LoggerFactory.getLogger(ServerRouter.class);
    private final Map<NodeId,Channel> availableServers=new HashMap<>();
    private NodeId leaderId;
    //发送消息
    public Object send(Object payload){
        //尝试所有的可能
        for(NodeId nodeId:getCandidateNodeIds()){
            try {
                Object result=doSend(nodeId,payload);
                this.leaderId=nodeId;
                return result;
            }catch (RedirectException e){
                //收到重定向请求，修改Leader节点id
                logger.debug("not a leader server, redirect to server {}",e.getLeaderId());
                this.leaderId=e.getLeaderId();
                return doSend(e.getLeaderId(),payload);
            } catch (Exception e){
                //连接失败，尝试下一个节点
                logger.debug("failed to process with server "+nodeId+", cause"+e.getMessage());
            }
        }
        throw new NoAvailableServerException("no available server");
    }
    //获取候选节点id列表
    private Collection<NodeId> getCandidateNodeIds(){
        //候选为空
        if(availableServers.isEmpty()){
            throw new NoAvailableServerException("no available server");
        }
        //已设置
        if(leaderId!=null){
            List<NodeId> nodeIds=new ArrayList<>();
            nodeIds.add(leaderId);
            for(NodeId nodeId:availableServers.keySet()){
                if(!nodeId.equals(leaderId)){
                    nodeIds.add(nodeId);
                }
            }
            return nodeIds;
        }
        //没有设置的话，任意返回
        return availableServers.keySet();
    }

    private Object doSend(NodeId id,Object payload){
        Channel channel = this.availableServers.get(id);
        if(channel==null){
            throw new IllegalStateException("no such channel to server"+id);
        }
        logger.debug("send request to server {}",id);
        return channel.send(payload);
    }

    public NodeId getLeaderId(){
        return leaderId;
    }

    public void add(NodeId id,Channel channel){
        this.availableServers.put(id,channel);
    }
    public void setLeaderId(NodeId leaderId){
        if(!availableServers.containsKey(leaderId)){
            throw new IllegalStateException("no such server ["+leaderId+"] in list");
        }
        this.leaderId=leaderId;
    }
}
