package com.raft.kv.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.raft.kv.Protos;

import java.util.UUID;

// *@author liuyaolong
public class SetCommand {

    private final String requestId;
    private final String key;
    private final byte[] value;
    //构造函数，无请求Id
    public SetCommand(String key, byte[] value) {
        this(UUID.randomUUID().toString(), key, value);
    }
    //构造函数
    public SetCommand(String requestId, String key, byte[] value) {
        this.requestId = requestId;
        this.key = key;
        this.value = value;
    }

    //从二进制数组中恢复setCommand
    public static SetCommand fromBytes(byte[] bytes) {
        try {
            Protos.SetCommand protoCommand = Protos.SetCommand.parseFrom(bytes);
            return new SetCommand(
                    protoCommand.getRequestId(),
                    protoCommand.getKey(),
                    protoCommand.getValue().toByteArray()
            );
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("failed to deserialize set command", e);
        }
    }

    //获取请求Id
    public String getRequestId() {
        return requestId;
    }
    //获取key
    public String getKey() {
        return key;
    }
    //获取value
    public byte[] getValue() {
        return value;
    }

    //转换为二进制数组
    public byte[] toBytes() {
        return Protos.SetCommand.newBuilder()
                .setRequestId(this.requestId)
                .setKey(this.key)
                .setValue(ByteString.copyFrom(this.value)).build().toByteArray();
    }

    @Override
    public String toString() {
        return "SetCommand{" +
                "key='" + key + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }

}
