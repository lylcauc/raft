package com.raft.kv.message;

// *@author liuyaolong
public class GetCommandResponse {
    private final boolean found;
    private final byte[] value;


    //构造函数
    public GetCommandResponse(byte[] value) {
        this(value!=null,value);
    }

    //构造函数，指示是否找到
    public GetCommandResponse(boolean found, byte[] value) {
        this.found = found;
        this.value = value;
    }

    //是否存在
    public boolean isFound() {
        return found;
    }
    //获取value
    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GetCommandResponse{" +
                "found=" + found +
                '}';
    }
}
