package com.raft.core.node;

// *@author liuyaolong

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.raft.core.log.FileLog;
import com.raft.core.log.Log;
import com.raft.core.log.MemoryLog;
import com.raft.core.node.config.NodeConfig;
import com.raft.core.node.store.FileNodeStore;
import com.raft.core.node.store.MemoryNodeStore;
import com.raft.core.node.store.NodeStore;
import com.raft.core.rpc.Connector;
import com.raft.core.rpc.nio.NioConnector;
import com.raft.core.schedule.DefaultScheduler;
import com.raft.core.schedule.Scheduler;
import com.raft.core.support.SingleThreadTaskExecutor;
import com.raft.core.support.TaskExecutor;
import io.netty.channel.nio.NioEventLoopGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;

/*
    用于快速构建NodeImpl实例
 */
public class NodeBuilder {



    private final NodeGroup group;//集群成员
    private final NodeId selfId;//节点ID
    private final EventBus eventBus;
    private Scheduler scheduler = null;//定时器
    private Connector connector = null;//RPC连接器
    private TaskExecutor taskExecutor = null;//主线程执行器
    private NodeConfig config=new NodeConfig();
    private NodeStore store;
    private Log log;
    private boolean standby = false;

    private NioEventLoopGroup workerNioEventLoopGroup = null;

    //单节点构造函数
    public NodeBuilder(NodeEndPoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }
    //多节点构造函数
    public NodeBuilder(Collection<NodeEndPoint> endpoints,  NodeId selfId) {
        Preconditions.checkNotNull(endpoints);
        Preconditions.checkNotNull(selfId);
        this.group = new NodeGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

    //设置定时器
     NodeBuilder setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }
    //设置通信组件
     NodeBuilder setConnector(Connector connector) {
        this.connector = connector;
        return this;
    }
    //设置任务定时器
    NodeBuilder setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    public NodeBuilder setDataDir(@Nullable String dataDirPath) {
        if (dataDirPath == null || dataDirPath.isEmpty()) {
            return this;
        }
        File dataDir = new File(dataDirPath);
        if (!dataDir.isDirectory() || !dataDir.exists()) {
            throw new IllegalArgumentException("[" + dataDirPath + "] not a directory, or not exists");
        }
        log = new FileLog(dataDir);
        store = new FileNodeStore(new File(dataDir, FileNodeStore.FILE_NAME));
        return this;
    }

    public NodeBuilder setStandby(boolean standby) {
        this.standby = standby;
        return this;
    }

    //构建Node实例
    public Node build(){
        return new NodeImpl(buildContext());
    }

    //构建上下文
    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setGroup(group);
        context.setSelfId(selfId);
        context.setEventBus(eventBus);
        context.setConfig(config);
        context.setScheduler(scheduler != null ? scheduler : new DefaultScheduler(config));
        context.setStore(store != null ? store : new MemoryNodeStore());
        context.setConnector(connector != null ? connector : createNioConnector());
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new SingleThreadTaskExecutor("node"));
        context.setLog(log!=null?log:new MemoryLog());
        return context;


    }


    private NioConnector createNioConnector() {
        int port = group.findSelf().getEndPoint().getPort();
        if (workerNioEventLoopGroup != null) {
            return new NioConnector(workerNioEventLoopGroup, selfId, eventBus, port);
        }
        return new NioConnector(new NioEventLoopGroup(config.getNioWorkerThreads()), false, selfId, eventBus, port);
    }


}
