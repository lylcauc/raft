package com.raft.core.log.sequence;

import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryFactory;
import com.raft.core.support.RandomAccessFileAdapter;
import com.raft.core.support.SeekableFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
    日志条目文件

    1.追加日志条目
    2.加载指定位置偏移的日志条目
    3.获取文件大小
    4.裁剪文件大小，包括清空，主要用于日志的removeAfter操作
 */

// *@author liuyaolong
public class EntriesFile {

    private final SeekableFile seekableFile;

    //构造函数，普通文件
    public EntriesFile(File file) throws FileNotFoundException {
        this(new RandomAccessFileAdapter(file));
    }
    //构造函数,SeekableFile
    public EntriesFile(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
    }
    //追加日志条目
    public long appendEntry(Entry entry) throws IOException {
        long offset = seekableFile.size();
        seekableFile.seek(offset);
        seekableFile.writeInt(entry.getKind());
        seekableFile.writeInt(entry.getIndex());
        seekableFile.writeInt(entry.getTerm());
        byte[] commandBytes = entry.getCommandBytes();
        seekableFile.writeInt(commandBytes.length);
        seekableFile.write(commandBytes);
        return offset;
    }
    //从指定偏移加载日志条目
    public Entry loadEntry(long offset, EntryFactory factory) throws IOException {
        if (offset > seekableFile.size()) {
            throw new IllegalArgumentException("offset > size");
        }
        seekableFile.seek(offset);
        int kind = seekableFile.readInt();
        int index = seekableFile.readInt();
        int term = seekableFile.readInt();
        int length = seekableFile.readInt();
        byte[] bytes = new byte[length];
        seekableFile.read(bytes);
        return factory.create(kind, index, term, bytes);
    }
    //获取大小
    public long size() throws IOException {
        return seekableFile.size();
    }
    //清除所有内容
    public void clear() throws IOException {
        truncate(0L);
    }
    //裁剪到指定大小，偏移由调用者提供
    public void truncate(long offset) throws IOException {
        seekableFile.truncate(offset);
    }
    //关闭文件
    public void close() throws IOException {
        seekableFile.close();
    }


}
