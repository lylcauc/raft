package com.raft.core.log.entry;

// *@author liuyaolong


//除了日志负载以外的其他日志信息
public class EntryMeta {

    private final int kind;
    private final int index;
    private final int term;

    public EntryMeta(int kind, int index, int term) {
        this.kind = kind;
        this.index = index;
        this.term = term;
    }

    public int getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public int getTerm() {
        return term;
    }
}
