package com.raft.core.log.entry;

// *@author liuyaolong
public class GeneralEntry extends AbstractEntry {

    private final byte[] commandBytes;

    //构造函数
    public GeneralEntry(int index, int term, byte[] commandBytes) {
        super(KIND_GENERAL, index, term);
        this.commandBytes = commandBytes;
    }

    //获取命令数据
    @Override
    public byte[] getCommandBytes() {
        return this.commandBytes;
    }


    @Override
    public String toString() {
        return "GeneralEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }
}
