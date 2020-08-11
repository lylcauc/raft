package com.raft.core.support;

import java.io.File;
import java.io.IOException;

// *@author liuyaolong
public class Files {

    public static void touch(File file) throws IOException{
        if(!file.createNewFile() && !file.setLastModified(System.currentTimeMillis())){
            throw new IOException("failed to touch file"+file);
        }
    }

}
