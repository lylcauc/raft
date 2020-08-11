package com.raft.core.service;

// *@author liuyaolong
public class ChannelException extends RuntimeException{

    public ChannelException() {
    }

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
