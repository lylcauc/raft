package com.raft.core.node;

// *@author liuyaolong
public class ReplicatingState {

    private int nextIndex;
    private int matchIndex;
    private boolean replicating=false;
    private long lastReplicatedAt=0;

    ReplicatingState(int nextIndex) {
        this(nextIndex,0);
    }

    ReplicatingState(int nextIndex, int matchIndex){
        this.nextIndex=nextIndex;
        this.matchIndex=matchIndex;
    }


    void setReplicating(boolean replicating) {
        this.replicating = replicating;
    }
    void setLastReplicatedAt(long lastReplicatedAt) {
        this.lastReplicatedAt = lastReplicatedAt;
    }

    int getNextIndex(){
        return nextIndex;
    }

    int getMatchIndex(){
        return matchIndex;
    }

    boolean backOffNextIndex(){
        if(nextIndex>1){
            nextIndex--;
            return true;
        }
        return false;
    }

    boolean advance(int lastEntryIndex){
        boolean result = (matchIndex != lastEntryIndex || nextIndex!=(lastEntryIndex+1));

        matchIndex=lastEntryIndex;
        nextIndex=lastEntryIndex+1;
        return result;
    }


}
