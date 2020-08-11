package com.raft.core.log.sequence;

import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryMeta;

import java.util.List;

// *@author liuyaolong

//日志条目序列(日志组件的主要操作对象)
public interface EntrySequence {

    //判断是否为空
    boolean isEmpty();
    //获取第一条日志的索引
    int getFirstLogIndex();
    //获取最后一条日志的索引
    int getLastLogIndex();
    //获取下一条日志的索引
    int getNextLogIndex();
    //获取序列的子视图，到最后一条日志
    List<Entry> subList(int fromIndex);

    List<Entry> subView(int fromIndex);

    // [fromIndex, toIndex)  获取序列的子视图，指定范围，不包括toIndex所指向的日志
    List<Entry> subList(int fromIndex, int toIndex);
    //检查某个日志条目是否存在
    boolean isEntryPresent(int index);
    //获取某个日志条目的元信息
    EntryMeta getEntryMeta(int index);
    //获取某个日志条目
    Entry getEntry(int index);
    //获取最后一个日志条目
    Entry getLastEntry();
    //追加日志条目
    void append(Entry entry);
    //追加多条日志
    void append(List<Entry> entries);
    //推进commitIndex
    void commit(int index);
    //获取当前的commitIndex
    int getCommitIndex();
    //移除某个索引之后的日志条目
    void removeAfter(int index);
    //关闭日志序列
    void close();


}
