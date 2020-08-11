package com.raft.kv.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.raft.core.node.NodeId;
import com.raft.core.service.Channel;
import com.raft.core.service.RedirectException;
import com.raft.kv.MessageConstants;
import com.raft.kv.Protos;
import com.raft.kv.message.GetCommand;
import com.raft.kv.message.SetCommand;
import io.netty.channel.ChannelException;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

// *@author liuyaolong
public class SocketChannel implements Channel {

    private final String host;
    private final int port;

    //构造函数
    public SocketChannel(String host,int port){
        this.port=port;
        this.host=host;
    }

    //发送消息
    public Object send(Object payload){
        // try-resource语法，退出代码块时自动关闭socket
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(this.host, this.port));
            //写入消息
            this.write(socket.getOutputStream(), payload);
            //读取响应
            return this.read(socket.getInputStream());
        } catch (IOException e) {
            throw new ChannelException("failed to send and receive", e);
        }
    }

    private Object read(InputStream input) throws IOException {
        DataInputStream dataInput = new DataInputStream(input);
        int messageType = dataInput.readInt();
        int payloadLength = dataInput.readInt();
        byte[] payload = new byte[payloadLength];
        dataInput.readFully(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                return null;
            case MessageConstants.MSG_TYPE_FAILURE:
                Protos.Failure protoFailure = Protos.Failure.parseFrom(payload);
                throw new ChannelException("error code " + protoFailure.getErrorCode() + ", message " + protoFailure.getMessage());
            case MessageConstants.MSG_TYPE_REDIRECT:
                Protos.Redirect protoRedirect = Protos.Redirect.parseFrom(payload);
                throw new RedirectException(new NodeId(protoRedirect.getLeaderId()));
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                Protos.GetCommandResponse protoGetCommandResponse = Protos.GetCommandResponse.parseFrom(payload);
                if (!protoGetCommandResponse.getFound()) return null;
                return protoGetCommandResponse.getValue().toByteArray();
            default:
                throw new ChannelException("unexpected message type " + messageType);
        }
    }

    private void write(OutputStream output, Object payload) throws IOException {
        if (payload instanceof GetCommand) {
            Protos.GetCommand protoGetCommand = Protos.GetCommand.newBuilder().setKey(((GetCommand) payload).getKey()).build();
            this.write(output, MessageConstants.MSG_TYPE_GET_COMMAND, protoGetCommand);
        } else if (payload instanceof SetCommand) {
            SetCommand setCommand = (SetCommand) payload;
            Protos.SetCommand protoSetCommand = Protos.SetCommand.newBuilder()
                    .setKey(setCommand.getKey())
                    .setValue(ByteString.copyFrom(setCommand.getValue())).build();
            this.write(output, MessageConstants.MSG_TYPE_SET_COMMAND, protoSetCommand);
        }
    }

    private void write(OutputStream output, int messageType, MessageLite message) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(output);
        byte[] messageBytes = message.toByteArray();
        dataOutput.writeInt(messageType);
        dataOutput.writeInt(messageBytes.length);
        dataOutput.write(messageBytes);
        dataOutput.flush();
    }






}
