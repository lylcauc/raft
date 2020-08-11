package com.raft.core.rpc.message;

// *@author liuyaolong
public class AppendEntriesResult {

    private final int term;//选举term
    private final boolean success;//是否追加成功
    private final String rpcMessageId;



    //构造函数
//    public AppendEntriesResult(int term, boolean success) {
//        this.term = term;
//        this.success = success;
//    }

    public AppendEntriesResult(String rpcMessageId,int term, boolean success) {
        this.term = term;
        this.success = success;
        this.rpcMessageId=rpcMessageId;
    }

    //获取term
    public int getTerm() {
        return term;
    }
    //获取是否成功
    public boolean isSuccess() {
        return success;
    }

    public String getRpcMessageId() {
        return rpcMessageId;
    }

    @Override
    public String toString() {
        return "AppendEntriesResult{" +
                "term=" + term +
                ", success=" + success +
                '}';
    }
}
