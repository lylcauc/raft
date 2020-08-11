package com.raft.core.log.entry;

// *@author liuyaolong
public abstract class AbstractEntry implements Entry{


    private final int kind;
    protected final int index;
    protected final int term;

    //构造函数
    AbstractEntry(int kind, int index, int term) {
        this.kind = kind;
        this.index = index;
        this.term = term;
    }

    //获取日志类型
    @Override
    public int getKind() {
        return this.kind;
    }
    //获取日志索引
    @Override
    public int getIndex() {
        return index;
    }
    //获取日志term
    @Override
    public int getTerm() {
        return term;
    }
    //获取元信息
    @Override
    public EntryMeta getMeta() {
        return new EntryMeta(kind, index, term);
    }

}
