package com.raft.core.rpc.nio;

// *@author liuyaolong
public class ConnectorException extends RuntimeException{

    public ConnectorException(String message,Throwable cause) {
        super(message, cause);
    }
}
