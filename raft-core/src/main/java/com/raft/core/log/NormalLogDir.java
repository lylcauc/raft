package com.raft.core.log;

import java.io.File;

// *@author liuyaolong
public class NormalLogDir extends AbstractLogDir {

    NormalLogDir(File dir){
        super(dir);
    }

    @Override
    public String toString() {
        return "NormalLogDir{" +
                "dir=" + dir +
                '}';
    }
}
