package com.raft.core.log.sequence;

import com.raft.core.log.LogDir;
import com.raft.core.log.LogException;
import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryFactory;
import com.raft.core.log.entry.EntryMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// *@author liuyaolong
public class FileEntrySequence extends AbstractEntrySequence {

    private final EntryFactory entryFactory = new EntryFactory();
    private final EntriesFile entriesFile;
    private final EntryIndexFile entryIndexFile;
    private final LinkedList<Entry> pendingEntries = new LinkedList<>();
    //raft算法中定义初始commitIndex为0，和日志是否持久化无关
    private int commitIndex;
    //构造函数，指定目录
    public FileEntrySequence(LogDir logDir, int logIndexOffset) {
        //默认logIndexOffset由外部决定
        super(logIndexOffset);
        try {
            this.entriesFile = new EntriesFile(logDir.getEntriesFile());
            this.entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
            initialize();
        } catch (IOException e) {
            throw new LogException("failed to open entries file or entry index file", e);
        }
    }
    //构造函数，指定文件
    public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexFile, int logIndexOffset) {
        //默认logIndexOffset由外部决定
        super(logIndexOffset);
        this.entriesFile = entriesFile;
        this.entryIndexFile = entryIndexFile;
        initialize();
    }
    //初始化
    private void initialize() {
        if (entryIndexFile.isEmpty()) {
            commitIndex = logIndexOffset - 1;
            return;
        }
        //使用日志索引文件的minEntryIndex作为logIndexOffset
        logIndexOffset = entryIndexFile.getMinEntryIndex();
        //使用日志索引文件的maxEntryIndex+1作为nextLogOffset
        nextLogIndex = entryIndexFile.getMaxEntryIndex() + 1;
        commitIndex = entryIndexFile.getMaxEntryIndex();
    }
    //获取commitIndex
    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

//    @Override
//    public GroupConfigEntryList buildGroupConfigEntryList() {
//        GroupConfigEntryList list = new GroupConfigEntryList();
//
//        // check file
//        try {
//            int entryKind;
//            for (EntryIndexItem indexItem : entryIndexFile) {
//                entryKind = indexItem.getKind();
//                if (entryKind == Entry.KIND_ADD_NODE || entryKind == Entry.KIND_REMOVE_NODE) {
//                    list.add((GroupConfigEntry) entriesFile.loadEntry(indexItem.getOffset(), entryFactory));
//                }
//            }
//        } catch (IOException e) {
//            throw new LogException("failed to load entry", e);
//        }
//
//        // check pending entries
//        for (Entry entry : pendingEntries) {
//            if (entry instanceof GroupConfigEntry) {
//                list.add((GroupConfigEntry) entry);
//            }
//        }
//        return list;
//    }

    /*
        获取日志条目或者日志条目视图
     */

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        //结果分为来自文件的和来自缓冲的两部分
        List<Entry> result = new ArrayList<>();

        // 从文件获取日志目录
        if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.getMaxEntryIndex()) {
            int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex() + 1, toIndex);
            for (int i = fromIndex; i < maxIndex; i++) {
                result.add(getEntryInFile(i));
            }
        }

        // 从日志缓冲中获取日志条目
        if (!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()) {
            Iterator<Entry> iterator = pendingEntries.iterator();
            Entry entry;
            int index;
            while (iterator.hasNext()) {
                entry = iterator.next();
                index = entry.getIndex();
                if (index >= toIndex) {
                    break;
                }
                if (index >= fromIndex) {
                    result.add(entry);
                }
            }
        }
        return result;
    }
    //获取指定位置的日志条目
    @Override
    protected Entry doGetEntry(int index) {
        if (!pendingEntries.isEmpty()) {
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if (index >= firstPendingEntryIndex) {
                return pendingEntries.get(index - firstPendingEntryIndex);
            }
        }

        // pending entries not empty but index < firstPendingEntryIndex => entry in file
        // pending entries empty => entry in file
        assert !entryIndexFile.isEmpty();
        return getEntryInFile(index);
    }
    //获取日志元信息
    @Override
    public EntryMeta getEntryMeta(int index) {
        if (!isEntryPresent(index)) {
            return null;
        }
        if (entryIndexFile.isEmpty()) {
            return pendingEntries.get(index - doGetFirstLogIndex()).getMeta();
        }
        return entryIndexFile.get(index).toEntryMeta();
    }
    //按照索引获取文件中的日志条目
    private Entry getEntryInFile(int index) {
        long offset = entryIndexFile.getOffset(index);
        try {
            return entriesFile.loadEntry(offset, entryFactory);
        } catch (IOException e) {
            throw new LogException("failed to load entry " + index, e);
        }
    }
    //获取最后一条日志
    @Override
    public Entry getLastEntry() {
        if (isEmpty()) {
            return null;
        }
        if (!pendingEntries.isEmpty()) {
            return pendingEntries.getLast();
        }
        assert !entryIndexFile.isEmpty();
        return getEntryInFile(entryIndexFile.getMaxEntryIndex());
    }

    //追加日志条目
    @Override
    protected void doAppend(Entry entry) {
        pendingEntries.add(entry);
    }

    //移除指定索引后的日志条目
    @Override
    protected void doRemoveAfter(int index) {
        //只需移除缓冲中的条目
        if (!pendingEntries.isEmpty() && index >= pendingEntries.getFirst().getIndex() - 1) {
            // 移除指定数量的日志条目
            // 循环方向是从小到大，但是移除是从后往前
            //最终移除指定数量的日志条目
            for (int i = index + 1; i <= doGetLastLogIndex(); i++) {
                pendingEntries.removeLast();
            }
            nextLogIndex = index + 1;
            return;
        }
        try {
            if (index >= doGetFirstLogIndex()) {
                //索引比日志缓冲中的第一条日志小
                pendingEntries.clear();
                // remove entries whose index >= (index + 1)
                entriesFile.truncate(entryIndexFile.getOffset(index + 1));
                entryIndexFile.removeAfter(index);
                nextLogIndex = index + 1;
                commitIndex = index;
            } else {
                //如果索引比第一条日志的索引都小，则清除所有数据
                pendingEntries.clear();
                entriesFile.clear();
                entryIndexFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset - 1;
            }
        } catch (IOException e) {
            throw new LogException(e);
        }
    }

    @Override
    public void commit(int index) {
        //检查commitIndex
        if (index < commitIndex) {
            throw new IllegalArgumentException("commit index < " + commitIndex);
        }
        if (index == commitIndex) {
            return;
        }
        //如果commitIndex在文件里，则只更新commitIndex
        if(!entryIndexFile.isEmpty() && index<=entryIndexFile.getMaxEntryIndex()){
            commitIndex=index;
            return;
        }
        //检查commitIndex是否在日志缓冲的区间内
        if (pendingEntries.isEmpty()
                || pendingEntries.getFirst().getIndex() > index
                || pendingEntries.getLast().getIndex() < index) {
            throw new IllegalArgumentException("no entry to commit or commit index exceed");
        }
        long offset;
        Entry entry = null;
        try {
            for (int i = commitIndex + 1; i <= index; i++) {
                entry = pendingEntries.removeFirst();
                offset = entriesFile.appendEntry(entry);
                entryIndexFile.appendEntryIndex(i, offset, entry.getKind(), entry.getTerm());
                commitIndex = i;
            }
        } catch (IOException e) {
            throw new LogException("failed to commit entry " + entry, e);
        }
    }

    @Override
    public void close() {
        try {
            entriesFile.close();
            entryIndexFile.close();
        } catch (IOException e) {
            throw new LogException("failed to close", e);
        }
    }


}
