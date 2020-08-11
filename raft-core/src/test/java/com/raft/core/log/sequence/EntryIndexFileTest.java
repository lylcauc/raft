package com.raft.core.log.sequence;

import com.raft.core.support.ByteArraySeekableFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

// *@author liuyaolong
public class EntryIndexFileTest {


    //构造文件内容
    private ByteArraySeekableFile makeEntryIndexFileContent(int minEntryIndex, int maxEntryIndex) throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        seekableFile.writeInt(minEntryIndex);
        seekableFile.writeInt(maxEntryIndex);
        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            seekableFile.writeLong(10L * i); // offset
            seekableFile.writeInt(1); // kind
            seekableFile.writeInt(i); // term
        }
        seekableFile.seek(0L);
        return seekableFile;
    }


    //测试加载
    @Test
    public void testLoad() throws IOException {
        ByteArraySeekableFile seekableFile = makeEntryIndexFileContent(3, 4);

        EntryIndexFile file = new EntryIndexFile(seekableFile);
        Assert.assertEquals(3, file.getMinEntryIndex());
        Assert.assertEquals(4, file.getMaxEntryIndex());
        Assert.assertEquals(2, file.getEntryIndexCount());

        EntryIndexItem item = file.get(3);
        Assert.assertNotNull(item);
        Assert.assertEquals(30L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());

        item = file.get(4);
        Assert.assertNotNull(item);
        Assert.assertEquals(40L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(4, item.getTerm());
    }

    //测试追加
    @Test
    public void testAppendEntryIndex() throws IOException {
        ByteArraySeekableFile seekableFile = new ByteArraySeekableFile();
        EntryIndexFile file = new EntryIndexFile(seekableFile);

        // 当为空时添加
        file.appendEntryIndex(10, 100L, 1, 2);
        Assert.assertEquals(1, file.getEntryIndexCount());
        Assert.assertEquals(10, file.getMinEntryIndex());
        Assert.assertEquals(10, file.getMaxEntryIndex());

        // 检查文件内容
        seekableFile.seek(0L);
        Assert.assertEquals(10, seekableFile.readInt()); // min entry index
        Assert.assertEquals(10, seekableFile.readInt()); // max entry index
        Assert.assertEquals(100L, seekableFile.readLong()); // offset
        Assert.assertEquals(1, seekableFile.readInt()); // kind
        Assert.assertEquals(2, seekableFile.readInt()); // term

        EntryIndexItem item = file.get(10);
        Assert.assertNotNull(item);
        Assert.assertEquals(100L, item.getOffset());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(2, item.getTerm());

        // 当不为空时添加
        file.appendEntryIndex(11, 200L, 1, 2);
        Assert.assertEquals(2, file.getEntryIndexCount());
        Assert.assertEquals(10, file.getMinEntryIndex());
        Assert.assertEquals(11, file.getMaxEntryIndex());

        // 检查文件内容
        seekableFile.seek(24L); // skip min/max and first entry index
        Assert.assertEquals(200L, seekableFile.readLong()); // offset
        Assert.assertEquals(1, seekableFile.readInt()); // kind
        Assert.assertEquals(2, seekableFile.readInt()); // term
    }

    //测试清除
    @Test
    public void testClear() throws IOException {
        ByteArraySeekableFile seekableFile = makeEntryIndexFileContent(5, 6);
        EntryIndexFile file = new EntryIndexFile(seekableFile);
        Assert.assertFalse(file.isEmpty());
        file.clear();
        Assert.assertTrue(file.isEmpty());
        Assert.assertEquals(0, file.getEntryIndexCount());
        Assert.assertEquals(0L, seekableFile.size());
    }

    //测试获取
    @Test
    public void testGet() throws IOException {
        EntryIndexFile file = new EntryIndexFile(makeEntryIndexFileContent(3, 4));
        EntryIndexItem item = file.get(3);
        Assert.assertNotNull(item);
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());
    }

    //测试遍历
    @Test
    public void testIterator() throws IOException {
        EntryIndexFile file = new EntryIndexFile(makeEntryIndexFileContent(3, 4));
        Iterator<EntryIndexItem> iterator = file.iterator();
        Assert.assertTrue(iterator.hasNext());
        EntryIndexItem item = iterator.next();
        Assert.assertEquals(3, item.getIndex());
        Assert.assertEquals(1, item.getKind());
        Assert.assertEquals(3, item.getTerm());
        Assert.assertTrue(iterator.hasNext());
        item = iterator.next();
        Assert.assertEquals(4, item.getIndex());
        Assert.assertFalse(iterator.hasNext());
    }



}
