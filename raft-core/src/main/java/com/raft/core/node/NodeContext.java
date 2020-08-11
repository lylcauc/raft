package com.raft.core.node;

import com.google.common.eventbus.EventBus;
import com.raft.core.log.Log;
import com.raft.core.node.config.NodeConfig;
import com.raft.core.node.store.NodeStore;
import com.raft.core.rpc.Connector;
import com.raft.core.schedule.Scheduler;
import com.raft.core.support.TaskExecutor;


// *@author liuyaolong
public class NodeContext {

    private NodeId selfId;//当前节点ID
    private NodeGroup group;//成员列表
    private Log log;//日志
    private Connector connector;//rpc组件
    private Scheduler scheduler;//定时器组件
    private EventBus eventBus;
    private TaskExecutor taskExecutor;//主线程执行器
    private NodeStore store;//部分角色状态数据存储
    private NodeConfig config;

    //获取自己的节点ID
    public NodeId selfId() {
        return selfId;
    }

    //设置自己的节点ID
    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

    public NodeGroup group() {
        return group;
    }

    public void setGroup(NodeGroup group) {
        this.group = group;
    }

    public Log log() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Connector connector() {
        return connector;
    }

    public NodeConfig config(){
        return config;
    }
    public void setConfig(NodeConfig config){
        this.config=config;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public TaskExecutor taskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public NodeStore store() {
        return store;
    }

    public void setStore(NodeStore store) {
        this.store = store;
    }
}
