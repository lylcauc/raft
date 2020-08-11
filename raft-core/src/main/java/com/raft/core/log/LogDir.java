package com.raft.core.log;

import java.io.File;

// *@author liuyaolong

/*
    抽象化的获取指定文件地址的接口，避免硬编码文件名
 */
public interface LogDir {

    //初始化目录
    void initialize();

    //是否存在
    boolean exists();

    //File getSnapshotFile();


    //获取EntriesFile对应的文件
    File getEntriesFile();

    //获取EntryIndexFile对应的文件
    File getEntryOffsetIndexFile();

    //获取目录
    File get();

    //重命名目录
    boolean renameTo(LogDir logDir);


}
