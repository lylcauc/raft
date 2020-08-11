package com.raft.core.rpc.message;

import com.raft.core.log.entry.Entry;
import com.raft.core.node.NodeId;

import java.util.Collections;
import java.util.List;

// *@author liuyaolong
public class AppendEntriesRpc {

    private int term;//选举term
    private NodeId leaderId;//leader节点id
    private int prevLogIndex = 0;//前一条日志的索引
    private int prevLogTerm;//前一条日志的term
    private List<Entry> entries = Collections.emptyList();//复制的日志条目
    private int leaderCommit;//leader的commitIndex
    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(int prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(int prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public int getLastEntryIndex(){
        return this.entries.isEmpty()?this.prevLogIndex:this.entries.get(this.entries.size()-1).getIndex();
    }
    @Override
    public String toString() {
        return "AppendEntriesRpc{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", entries=" + entries +
                ", leaderCommit=" + leaderCommit +
                '}';
    }
}
