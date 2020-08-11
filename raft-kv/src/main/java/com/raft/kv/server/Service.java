package com.raft.kv.server;

import com.raft.core.log.statemachine.AbstractSingleThreadStateMachine;
import com.raft.core.node.Node;
import com.raft.core.node.role.RoleName;
import com.raft.core.node.role.RoleNameAndLeaderId;
import com.raft.kv.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// *@author liuyaolong
public class Service {
    private static final Logger logger= LoggerFactory.getLogger(Service.class);

    private final Node node;
    //请求ID和CommandRequest的映射类
    private final ConcurrentHashMap<String, CommandRequest<?>> pendingCommands=new ConcurrentHashMap<String, CommandRequest<?>>();
    //kv服务的数据
    private Map<String,byte[]> map=new HashMap<>();

    //构造函数


    public Service(Node node) {
        this.node = node;
        this.node.registerStateMachine(new StateMachineImpl());
    }
    //SET命令
    public void set(CommandRequest<SetCommand> commandRequest) {
        //如果当前节点不是Leader节点，则返回Redirect
        Redirect redirect = checkLeadership();
        if (redirect != null) {
            commandRequest.reply(redirect);
            return;
        }

        SetCommand command = commandRequest.getCommand();
        logger.debug("set {}", command.getKey());
        //记录请求ID和CommandRequest的映射
        this.pendingCommands.put(command.getRequestId(), commandRequest);
        //客户端连接关闭时从映射中移除
        commandRequest.addCloseListener(() -> pendingCommands.remove(command.getRequestId()));
        //追加日志
        this.node.appendLog(command.toBytes());
    }
    //检查是不是Leader节点
    private Redirect checkLeadership() {
        RoleNameAndLeaderId state = node.getRoleNameAndLeaderId();
        if (state.getRoleName() != RoleName.LEADER) {
            return new Redirect(state.getLeaderId());
        }
        return null;
    }

    public void get(CommandRequest<GetCommand> commandRequest) {
        String key = commandRequest.getCommand().getKey();
        logger.debug("get {}", key);
        byte[] value = this.map.get(key);
        // TODO view from node state machine
        commandRequest.reply(new GetCommandResponse(value));
    }


    private class StateMachineImpl extends AbstractSingleThreadStateMachine{

        //应用命令
        @Override
        protected void applyCommand(@Nonnull byte[] commandBytes) {
            //恢复命令
            SetCommand command=SetCommand.fromBytes(commandBytes);
            //修改数据
            map.put(command.getKey(),command.getValue());
            //查找连接
            CommandRequest<?> commandRequest=pendingCommands.remove(command.getRequestId());
            if(commandRequest!=null){
                //回复结果
                commandRequest.reply(Success.INSTANCE);
            }
        }
    }

}
