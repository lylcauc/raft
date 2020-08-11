package com.raft.core.node.role;

import com.raft.core.node.NodeId;
import com.raft.core.schedule.ElectionTimeout;

// *@author liuyaolong
public class CandidateNodeRole extends AbstractNodeRole {

    private final int votesCount; //票数
    private final ElectionTimeout electionTimeout;//选举超时


    //构造函数，票数1
    public CandidateNodeRole(int term , ElectionTimeout electionTimeout) {
        this(term,1,electionTimeout);
    }

    //构造函数，可指定票数
    public CandidateNodeRole(int term,int votesCount,ElectionTimeout electionTimeout){
        super(RoleName.CANDIDATE,term);
        this.votesCount=votesCount;
        this.electionTimeout=electionTimeout;
    }

    //获取投票数
    public int getVotesCount() {
        return votesCount;
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return null;
    }

    //取消选举超时
    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public String toString() {
        return "CandidateNodeRole{" +
                "votesCount=" + votesCount +
                ", electionTimeout=" + electionTimeout +
                ", term=" + term +
                '}';
    }
}
