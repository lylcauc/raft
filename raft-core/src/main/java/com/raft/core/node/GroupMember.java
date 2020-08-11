package com.raft.core.node;

// *@author liuyaolong
public class GroupMember {

    private final NodeEndPoint endPoint;
    private ReplicatingState replicatingState;

    //无复制状态的构造函数
    GroupMember(NodeEndPoint endPoint){
        this(endPoint,null);
    }

    //带复制状态的构造函数
    GroupMember(NodeEndPoint endPoint, ReplicatingState replicatingState){
        this.endPoint=endPoint;
        this.replicatingState=replicatingState;
    }

    NodeId getId(){
        return endPoint.getId();
    }
    NodeEndPoint getEndPoint() {
        return endPoint;
    }

    ReplicatingState getReplicatingState() {
        return replicatingState;
    }

    void replicateNow() {
        replicateAt(System.currentTimeMillis());
    }

    void replicateAt(long replicatedAt) {
        ReplicatingState replicatingState = ensureReplicatingState();
        replicatingState.setReplicating(true);
        replicatingState.setLastReplicatedAt(replicatedAt);
    }



    //获取nextIndex
    int getNextIndex(){
        return ensureReplicatingState().getNextIndex();
    }

    //获取matchIndex
    int getMatchIndex(){
        return ensureReplicatingState().getMatchIndex();
    }

    boolean idEquals(NodeId id){
        return endPoint.getId().equals(id);
    }
    //获取复制进度
    private ReplicatingState ensureReplicatingState(){
        if(replicatingState==null){
            throw new IllegalStateException("replication state not set");
        }
        return replicatingState;
    }

    void setReplicatingState(ReplicatingState replicatingState) {
        this.replicatingState = replicatingState;
    }

    boolean backOffNextIndex(){
        return ensureReplicatingState().backOffNextIndex();
    }

    boolean advanceReplicatingState(int lastEntryIndex){
        return ensureReplicatingState().advance(lastEntryIndex);
    }
}
