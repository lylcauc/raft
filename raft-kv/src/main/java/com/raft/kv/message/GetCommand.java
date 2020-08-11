package com.raft.kv.message;

// *@author liuyaolong
public class GetCommand {

    private final String key;
    //构造函数
    public GetCommand(String key){
        this.key=key;
    }
    //获取key
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GetCommand{" +
                "key='" + key + '\'' +
                '}';
    }
}
