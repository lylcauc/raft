package com.raft.core.service;

// *@author liuyaolong
public class NoAvailableServerException extends RuntimeException{

    public NoAvailableServerException(String message){
        super(message);
    }
}
