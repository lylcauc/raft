package com.raft.core.log.entry;

// *@author liuyaolong


/*
    日志分为至少两种：
    1.普通日志条目，即上层服务产生的日志，日志负载为上层服务操作的内容
    2.NO-OP日志条目，即选举产生的新Leader节点增加的第一条空日志，不需要在上层服务的状态机中应用
 */


public interface Entry {

    //日志条目类型
    int KIND_NO_OP=0;
    int KIND_GENERAL = 1;
//    int KIND_ADD_NODE = 3;
//    int KIND_REMOVE_NODE = 4;

    //获取类型
    int getKind();
    //获取索引
    int getIndex();
    //获取Term
    int getTerm();
    //获取元信息(kind,term和index)
    EntryMeta getMeta();
    //获取日志负载
    byte[] getCommandBytes();

}
