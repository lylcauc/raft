package com.raft.core.node.store;

// *@author liuyaolong

import com.raft.core.node.NodeId;
import com.raft.core.support.Files;
import com.raft.core.support.RandomAccessFileAdapter;
import com.raft.core.support.SeekableFile;

import java.io.File;
import java.io.IOException;

/*
    利用文件对角色状态进行持久化
    使用二进制格式存放两个数据
    （1）4字节，currentTerm
    （2）4字节，votedFor
    （3）变长，votedFor内容
 */
public class FileNodeStore implements NodeStore {

    public static final String FILE_NAME="node.bin";
    private static final long OFFSET_TERM=0;
    private static final long OFFSET_VOTED_FOR=4;
    private final SeekableFile seekableFile;
    private int term=0;
    private NodeId votedFor=null;

    //从文件读取
    public FileNodeStore(File file) {
        try {
            if(!file.exists()){
                Files.touch(file);
            }
            seekableFile =new RandomAccessFileAdapter(file);
            initializeOrLoad();
        }catch (IOException e){
            throw new NodeStoreException(e);
        }
    }



    //从模拟文件读取,便于测试
    public FileNodeStore(SeekableFile seekableFile){
        this.seekableFile=seekableFile;
        try{
            initializeOrLoad();
        }catch (IOException e){
            throw new NodeStoreException(e);
        }
    }

    //初始化或者加载
    private void initializeOrLoad() throws IOException {
        if(seekableFile.size()==0){
            //初始化
            //(term,4)+(votedFor length,4)=8
            seekableFile.truncate(8L);
            seekableFile.seek(0);
            seekableFile.writeInt(0);//term
            seekableFile.writeInt(0);//votedFor length
        }else {
            //加载
            //读取term
            term=seekableFile.readInt();
            //读取votedFor
            int length=seekableFile.readInt();
            if(length>0){
                byte[] bytes=new byte[length];
                seekableFile.read(bytes);
                votedFor=new NodeId(new String(bytes));
            }
        }
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        try {
            //定位到term
            seekableFile.seek(OFFSET_TERM);
            seekableFile.writeInt(term);
        }catch (IOException e){
            throw new NodeStoreException(e);
        }
        this.term=term;
    }

    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        try {
            seekableFile.seek(OFFSET_VOTED_FOR);
            //votedFor 为空
            if(votedFor==null){
                seekableFile.writeInt(0);
                seekableFile.truncate(8L);
            }else{
                byte[] bytes=votedFor.getValue().getBytes();
                seekableFile.writeInt(bytes.length);
                seekableFile.write(bytes);
            }
        }catch (IOException e){
            throw new NodeStoreException(e);
        }
        this.votedFor=votedFor;
    }

    @Override
    public void close() {
        try {
            seekableFile.close();
        }catch (IOException e){
            throw new NodeStoreException(e);
        }
    }
}
