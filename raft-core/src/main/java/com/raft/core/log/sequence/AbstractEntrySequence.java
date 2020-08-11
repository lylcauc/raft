package com.raft.core.log.sequence;

import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryMeta;

import java.util.Collections;
import java.util.List;

// *@author liuyaolong
abstract class AbstractEntrySequence implements EntrySequence{

    int logIndexOffset;
    int nextLogIndex;

    /*
        日志条目序列索引相关方法
     */

    //构造函数，默认为空日志条目序列
    AbstractEntrySequence(int logIndexOffset) {
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = logIndexOffset;
    }
    //判断是否为空
    @Override
    public boolean isEmpty() {
        return logIndexOffset==nextLogIndex;
    }

    //获取第一条日志的索引，为空时抛出错误
    @Override
    public int getFirstLogIndex() {
        if(isEmpty()){
            throw new EmptySequenceException();
        }
        return doGetFirstLogIndex();
    }
    //获取日志索引偏移
    int doGetFirstLogIndex(){
        return logIndexOffset;
    }
    //获取最后一条日志的索引，为空时抛出错误
    @Override
    public int getLastLogIndex() {
        if(isEmpty()){
            throw new EmptySequenceException();
        }
        return doGetLastLogIndex();
    }

    //获取最后一条日志的索引
    int doGetLastLogIndex(){
        return nextLogIndex-1;
    }
    //获取下一条日志的索引
    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }


    //判断日志是否存在
    @Override
    public boolean isEntryPresent(int index) {

        return !isEmpty() && index>=doGetFirstLogIndex()
                && index<=doGetLastLogIndex();
    }


    /*
        以下为随机获取日志条目相关方法的抽象实现
     */

    //获取指定索引的日志条目
    @Override
    public Entry getEntry(int index) {
        if(!isEntryPresent(index)){
            return null;
        }
        return doGetEntry(index);
    }

    //获取指定索引的日志条目元信息
    @Override
    public EntryMeta getEntryMeta(int index) {
        Entry entry=getEntry(index);
        return entry!=null ?entry.getMeta():null;
    }

    //获取最后一条日志条目
    @Override
    public Entry getLastEntry() {
        return isEmpty()?null:doGetEntry(doGetLastLogIndex());
    }

    //获取指定索引的日志条目(抽象方法)
    protected abstract Entry doGetEntry(int index);

    /*
        以下为日志序列子视图的方法
     */

    //获取一个子视图，不指定结束索引
    @Override
    public List<Entry> subList(int fromIndex) {
        if(isEmpty() || fromIndex>doGetLastLogIndex()){
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex,doGetFirstLogIndex()),nextLogIndex);
    }
    //获取一个子视图，指定结束索引
    @Override
    public List<Entry> subList(int fromIndex, int toIndex) {
        if(isEmpty()){
            throw new EmptySequenceException();
        }
        //检查索引
        if(fromIndex<doGetFirstLogIndex()
                || toIndex>doGetLastLogIndex()+1
                || fromIndex>toIndex){
            throw new IllegalArgumentException(
                    "illegal from index"+fromIndex+"or to index"+toIndex
            );

        }
        return doSubList(fromIndex,toIndex);

    }

    @Override
    public List<Entry> subView(int fromIndex) {
        if (isEmpty() || fromIndex > doGetLastLogIndex()) {
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex, doGetFirstLogIndex()), nextLogIndex);
    }

    //获取一个子视图(抽象方法)
    protected abstract List<Entry> doSubList(int fromIndex, int toIndex);

    //追加单条日志
    @Override
    public void append(Entry entry) {
        //保证新日志的索引是当前序列的下一条索引
        if(entry.getIndex()!=nextLogIndex){
            throw new IllegalArgumentException("entry index must be"
            +nextLogIndex);
        }
        doAppend(entry);
        //递增序列的日志索引
        nextLogIndex++;
    }
    //追加日志(抽象方法)
    protected abstract void doAppend(Entry entry);
    //追加多条日志
    @Override
    public void append(List<Entry> entries) {
        for(Entry entry:entries){
            append(entry);
        }
    }

    //移除指定索引后的日志条目
    @Override
    public void removeAfter(int index) {
        if(isEmpty() || index>=doGetLastLogIndex()){
            return;
        }
        doRemoveAfter(index);
    }
    //移除指定索引后的日志条目
    protected abstract void doRemoveAfter(int index);


    @Override
    public void commit(int index) {

    }

    @Override
    public int getCommitIndex() {
        return 0;
    }

    @Override
    public void close() {

    }
}
