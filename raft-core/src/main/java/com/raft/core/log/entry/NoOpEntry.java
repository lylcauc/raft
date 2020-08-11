package com.raft.core.log.entry;

// *@author liuyaolong

//新Leader产生后的第一条空日志
public class NoOpEntry extends AbstractEntry{

    //构造函数
    public NoOpEntry(int index, int term) {
        super(KIND_NO_OP, index, term);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }


    @Override
    public String toString() {
        return "NoOpEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }
}
