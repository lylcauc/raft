package com.raft.kv.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import com.raft.kv.MessageConstants;
import com.raft.kv.Protos;
import com.raft.kv.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

// *@author liuyaolong
public class Encoder extends MessageToByteEncoder<Object> {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //成功
        if (msg instanceof Success) {
            this.writeMessage(MessageConstants.MSG_TYPE_SUCCESS, Protos.Success.newBuilder().build(), out);
        } else if (msg instanceof Failure) {
            //失败
            Failure failure = (Failure) msg;
            Protos.Failure protoFailure = Protos.Failure.newBuilder().setErrorCode(failure.getErrorCode()).setMessage(failure.getMessage()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_FAILURE, protoFailure, out);
        } else if (msg instanceof Redirect) {
            //重定向
            Redirect redirect = (Redirect) msg;
            Protos.Redirect protoRedirect = Protos.Redirect.newBuilder().setLeaderId(redirect.getLeaderId()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_REDIRECT, protoRedirect, out);
        } else if (msg instanceof GetCommand) {
            //GET命令
            GetCommand command = (GetCommand) msg;
            Protos.GetCommand protoGetCommand = Protos.GetCommand.newBuilder().setKey(command.getKey()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND, protoGetCommand, out);
        } else if (msg instanceof GetCommandResponse) {
            //GET命令响应
            GetCommandResponse response = (GetCommandResponse) msg;
            byte[] value = response.getValue();
            Protos.GetCommandResponse protoResponse = Protos.GetCommandResponse.newBuilder()
                    .setFound(response.isFound())
                    .setValue(value != null ? ByteString.copyFrom(value) : ByteString.EMPTY).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE, protoResponse, out);
        } else if (msg instanceof SetCommand) {
            //SET命令
            SetCommand command = (SetCommand) msg;
            Protos.SetCommand protoSetCommand = Protos.SetCommand.newBuilder()
                    .setKey(command.getKey())
                    .setValue(ByteString.copyFrom(command.getValue()))
                    .build();
            this.writeMessage(MessageConstants.MSG_TYPE_SET_COMMAND, protoSetCommand, out);
        }
    }

    //写数据
    private void writeMessage(int messageType, MessageLite message, ByteBuf out) throws IOException {
        out.writeInt(messageType);
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
