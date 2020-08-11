package com.raft.core.log;

import com.raft.core.log.sequence.FileEntrySequence;

import java.io.File;

// *@author liuyaolong
public class FileLog extends AbstractLog{

    private final RootDir rootDir;
    //构造函数
    public FileLog(File baseDir){
        rootDir=new RootDir(baseDir);
        //获取最新的日志代
        LogGeneration latestGeneration=rootDir.getLatestGeneration();
        if(latestGeneration!=null) {
            //日志存在
            entrySequence = new FileEntrySequence(
                    //TODO ??
                    latestGeneration, latestGeneration.getLastIncludedIndex());
        }else{
                //日志不存在
                LogGeneration firstGeneration=rootDir.createFirstGeneration();
                entrySequence=new FileEntrySequence(firstGeneration,1);
            }
        }


    @Override
    public void close() {

    }
}
