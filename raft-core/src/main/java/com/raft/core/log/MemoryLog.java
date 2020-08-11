package com.raft.core.log;

import com.google.common.eventbus.EventBus;
import com.raft.core.log.sequence.EntrySequence;
import com.raft.core.log.sequence.MemoryEntrySequence;

// *@author liuyaolong
public class MemoryLog extends AbstractLog {

    //构造函数，无参数
    public MemoryLog(){
        this(new MemoryEntrySequence());
    }


    //构造函数，针对测试
    MemoryLog(EntrySequence entrySequence){

        this.entrySequence=entrySequence;
    }

    @Override
    public void close() {

    }
}
