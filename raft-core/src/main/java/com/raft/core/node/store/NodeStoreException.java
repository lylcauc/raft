package com.raft.core.node.store;

// *@author liuyaolong
public class NodeStoreException extends RuntimeException{

    public NodeStoreException(Throwable cause){
        super(cause);
    }

    public NodeStoreException(String message,Throwable cause){
        super(message,cause);
    }

}
