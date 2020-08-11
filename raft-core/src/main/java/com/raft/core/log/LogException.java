package com.raft.core.log;

// *@author liuyaolong
public class LogException extends RuntimeException{


    public LogException(){

    }
    public LogException(String message){
        super(message);
    }

    public LogException(Throwable cause){
        super(cause);
    }

    public LogException(String message,Throwable cause){
        super(message,cause);
    }

}
