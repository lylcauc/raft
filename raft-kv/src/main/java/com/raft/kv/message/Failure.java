package com.raft.kv.message;

// *@author liuyaolong

//处理GET异常的情况
public class Failure {

    private final int errorCode;
    private final String message;
    //构造函数
    public Failure(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    //获取错误码(设计了两种：100表示通用异常，101表示超时异常)
    public int getErrorCode() {
        return errorCode;
    }
    //获取描述
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Failure{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }

}
