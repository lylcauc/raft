package com.raft.kv.client;

// *@author liuyaolong
public interface Command {

    //获取命令名
    String getName();
    //执行
    void execute(String arguments,CommandContext context);
}
