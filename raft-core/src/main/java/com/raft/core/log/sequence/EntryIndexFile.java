package com.raft.core.log.sequence;

import com.raft.core.support.RandomAccessFileAdapter;
import com.raft.core.support.SeekableFile;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
    日志条目索引文件
 */
// *@author liuyaolong
public class EntryIndexFile implements Iterable<EntryIndexItem> {

    //最大条目索引的偏移
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    //单条日志条目元信息的长度
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    private final SeekableFile seekableFile;
    private int entryIndexCount;//日志条目数
    private int minEntryIndex;//最小日志索引
    private int maxEntryIndex;//最大日志索引
    private Map<Integer, EntryIndexItem> entryIndexMap = new HashMap<>();
    //构造函数，普通文件
    public EntryIndexFile(File file) throws IOException {
        this(new RandomAccessFileAdapter(file));
    }
    //构造函数，SeekableFile
    public EntryIndexFile(SeekableFile seekableFile) throws IOException {
        this.seekableFile = seekableFile;
        load();
    }
    //加载所有日志元信息
    private void load() throws IOException {
        if (seekableFile.size() == 0L) {
            entryIndexCount = 0;
            return;
        }
        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        updateEntryIndexCount();
        //逐条加载
        long offset;
        int kind;
        int term;
        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            offset = seekableFile.readLong();
            kind = seekableFile.readInt();
            term = seekableFile.readInt();
            entryIndexMap.put(i, new EntryIndexItem(i, offset, kind, term));
        }
    }
    //更新日志条目数量
    private void updateEntryIndexCount() {
        entryIndexCount = maxEntryIndex - minEntryIndex + 1;
    }
    //
    public boolean isEmpty() {
        return entryIndexCount == 0;
    }

    public int getMinEntryIndex() {
        checkEmpty();
        return minEntryIndex;
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("no entry index");
        }
    }

    public int getMaxEntryIndex() {
        checkEmpty();
        return maxEntryIndex;
    }

    public int getEntryIndexCount() {
        return entryIndexCount;
    }

    public void appendEntryIndex(int index, long offset, int kind, int term) throws IOException {
        if (seekableFile.size() == 0L) {
            //如果文件为空，则写入minEntryIndex
            seekableFile.writeInt(index);
            minEntryIndex = index;
        } else {
            //索引检查
            if (index != maxEntryIndex + 1) {
                throw new IllegalArgumentException("index must be " + (maxEntryIndex + 1) + ", but was " + index);
            }
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX); // 跳过 minEntryIndex
        }

        // 写入 maxEntryIndex
        seekableFile.writeInt(index);
        maxEntryIndex = index;
        updateEntryIndexCount();

        // 移动到文件最后
        seekableFile.seek(getOffsetOfEntryIndexItem(index));
        seekableFile.writeLong(offset);
        seekableFile.writeInt(kind);
        seekableFile.writeInt(term);
        entryIndexMap.put(index, new EntryIndexItem(index, offset, kind, term));
    }
    //获取指定索引的日志的偏移
    private long getOffsetOfEntryIndexItem(int index) {
        return (index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2;
    }
    //清除全部
    public void clear() throws IOException {
        seekableFile.truncate(0L);
        entryIndexCount = 0;
        entryIndexMap.clear();
    }
    //移除某个索引之后的数据
    public void removeAfter(int newMaxEntryIndex) throws IOException {
        //判断是否为空
        if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) {
            return;
        }
        //判断新的maxEntryIndex是否比minEntryIndex小
        //如果是则全部移除
        if (newMaxEntryIndex < minEntryIndex) {
            clear();
            return;
        }
        //修改maxEntryIndex
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        seekableFile.writeInt(newMaxEntryIndex);
        //裁剪文件
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1));
        //移除缓存中的元信息
        for (int i = newMaxEntryIndex + 1; i <= maxEntryIndex; i++) {
            entryIndexMap.remove(i);
        }
        maxEntryIndex = newMaxEntryIndex;
        entryIndexCount = newMaxEntryIndex - minEntryIndex + 1;
    }

    public long getOffset(int entryIndex) {
        return get(entryIndex).getOffset();
    }

    @Nonnull
    public EntryIndexItem get(int entryIndex) {
        checkEmpty();
        if (entryIndex < minEntryIndex || entryIndex > maxEntryIndex) {
            throw new IllegalArgumentException("index < min or index > max");
        }
        return entryIndexMap.get(entryIndex);
    }

    @Override
    @Nonnull
    public Iterator<EntryIndexItem> iterator() {
        //索引是否为空
        if (isEmpty()) {
            return Collections.emptyIterator();
        }
        return new EntryIndexIterator(entryIndexCount, minEntryIndex);
    }

    public void close() throws IOException {
        seekableFile.close();
    }

    private class EntryIndexIterator implements Iterator<EntryIndexItem> {

        private final int entryIndexCount;//条目总数
        private int currentEntryIndex;//当前索引
        //构造函数
        EntryIndexIterator(int entryIndexCount, int minEntryIndex) {
            this.entryIndexCount = entryIndexCount;
            this.currentEntryIndex = minEntryIndex;
        }
        //是否存在下一条
        @Override
        public boolean hasNext() {
            checkModification();
            return currentEntryIndex <= maxEntryIndex;
        }
        //检查是否修改
        private void checkModification() {
            if (this.entryIndexCount != EntryIndexFile.this.entryIndexCount) {
                throw new IllegalStateException("entry index count changed");
            }
        }
        //获取下一条
        @Override
        public EntryIndexItem next() {
            checkModification();
            return entryIndexMap.get(currentEntryIndex++);
        }
    }

}
