package com.raft.core.rpc.message;

// *@author liuyaolong
public class RequestVoteResult {

    private final int term;//选举term
    private final boolean voteGranted;//是否投票

    //构造函数
    public RequestVoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    //获取term
    public int getTerm() {
        return term;
    }

    //获取是否投票
    public boolean isVoteGranted() {
        return voteGranted;
    }

    @Override
    public String toString() {
        return "RequestVoteResult{" +
                "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }
}
