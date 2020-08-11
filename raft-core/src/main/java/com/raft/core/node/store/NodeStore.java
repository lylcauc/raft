package com.raft.core.node.store;

import com.raft.core.node.NodeId;

// *@author liuyaolong
public interface NodeStore {

    //获取currentTerm
    int getTerm();

    //设置currentTerm
    void setTerm(int term);

    //获取votedFor
    NodeId getVotedFor();

    //设置votedFor
    void setVotedFor(NodeId votedFor);

    //关闭文件
    void close();



}
