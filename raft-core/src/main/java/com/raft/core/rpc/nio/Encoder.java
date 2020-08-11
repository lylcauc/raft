package com.raft.core.rpc.nio;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.raft.core.Protos;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

// *@author liuyaolong
public class Encoder extends MessageToByteEncoder<Object> {
    private static final Logger logger= LoggerFactory.getLogger(Encoder.class);

    //序列化
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        //判断消息类型
        if (msg instanceof NodeId) {
            this.writeMessage(out, MessageConstants.MSG_TYPE_NODE_ID, ((NodeId) msg).getValue().getBytes());
        } else if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc) msg;
            Protos.RequestVoteRpc protoRpc = Protos.RequestVoteRpc.newBuilder()
                    .setTerm(rpc.getTerm())
                    .setCandidateId(rpc.getCandidateId().getValue())
                    .setLastLogIndex(rpc.getLastLogIndex())
                    .setLastLogTerm(rpc.getLastLogTerm())
                    .build();
            this.writeMessage(out, MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC, protoRpc);
        } else if (msg instanceof RequestVoteResult) {
            RequestVoteResult result = (RequestVoteResult) msg;
            Protos.RequestVoteResult protoResult = Protos.RequestVoteResult.newBuilder()
                    .setTerm(result.getTerm())
                    .setVoteGranted(result.isVoteGranted())
                    .build();
            this.writeMessage(out, MessageConstants.MSG_TYPE_REQUEST_VOTE_RESULT, protoResult);
        } else if (msg instanceof AppendEntriesRpc) {
            AppendEntriesRpc rpc = (AppendEntriesRpc) msg;
            Protos.AppendEntriesRpc protoRpc = Protos.AppendEntriesRpc.newBuilder()
                    .setMessageId(rpc.getMessageId())
                    .setTerm(rpc.getTerm())
                    .setLeaderId(rpc.getLeaderId().getValue())
                    .setLeaderCommit(rpc.getLeaderCommit())
                    .setPrevLogIndex(rpc.getPrevLogIndex())
                    .setPrevLogTerm(rpc.getPrevLogTerm())
                    .addAllEntries(
                            rpc.getEntries().stream().map(e ->
                                    Protos.AppendEntriesRpc.Entry.newBuilder()
                                            .setKind(e.getKind())
                                            .setIndex(e.getIndex())
                                            .setTerm(e.getTerm())
                                            .setCommand(ByteString.copyFrom(e.getCommandBytes()))
                                            .build()
                            ).collect(Collectors.toList())
                    ).build();
            this.writeMessage(out, MessageConstants.MSG_TYPE_APPEND_ENTRIES_RPC, protoRpc);
        }
        else if (msg instanceof AppendEntriesResult) {
            AppendEntriesResult result = (AppendEntriesResult) msg;
            Protos.AppendEntriesResult protoResult = Protos.AppendEntriesResult.newBuilder()
                    .setRpcMessageId(result.getRpcMessageId())
                    .setTerm(result.getTerm())
                    .setSuccess(result.isSuccess())
                    .build();
            this.writeMessage(out, MessageConstants.MSG_TYPE_APPEND_ENTRIES_RESULT, protoResult);
        }
//        else if (msg instanceof InstallSnapshotRpc) {
//            InstallSnapshotRpc rpc = (InstallSnapshotRpc) msg;
//            Protos.InstallSnapshotRpc protoRpc = Protos.InstallSnapshotRpc.newBuilder()
//                    .setTerm(rpc.getTerm())
//                    .setLeaderId(rpc.getLeaderId().getValue())
//                    .setLastIndex(rpc.getLastIndex())
//                    .setLastTerm(rpc.getLastTerm())
//                    .addAllLastConfig(
//                            rpc.getLastConfig().stream().map(e ->
//                                    Protos.NodeEndpoint.newBuilder()
//                                            .setId(e.getId().getValue())
//                                            .setHost(e.getHost())
//                                            .setPort(e.getPort())
//                                            .build()
//                            ).collect(Collectors.toList()))
//                    .setOffset(rpc.getOffset())
//                    .setData(ByteString.copyFrom(rpc.getData()))
//                    .setDone(rpc.isDone()).build();
//            this.writeMessage(out, MessageConstants.MSG_TYPE_INSTALL_SNAPSHOT_PRC, protoRpc);
//        } else if (msg instanceof InstallSnapshotResult) {
//            InstallSnapshotResult result = (InstallSnapshotResult) msg;
//            Protos.InstallSnapshotResult protoResult = Protos.InstallSnapshotResult.newBuilder()
//                    .setTerm(result.getTerm()).build();
//            this.writeMessage(out, MessageConstants.MSG_TYPE_INSTALL_SNAPSHOT_RESULT, protoResult);
//        }
    }
    //写入消息(1)
    private void writeMessage(ByteBuf out, int messageType, MessageLite message) throws IOException {
        //写入消息类型
        out.writeInt(messageType);
//        ByteBufOutputStream bbOut=new ByteBufOutputStream(out);
//        message.writeDelimitedTo(bbOut);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        message.writeTo(byteOutput);
        this.writeBytes(out, byteOutput.toByteArray());
    }
    //写入消息(2)
    private void writeMessage(ByteBuf out, int messageType, byte[] bytes) {
        // 4 + 4 + VAR
        out.writeInt(messageType);
        this.writeBytes(out, bytes);
    }

    //写入消息(3)
    private void writeBytes(ByteBuf out, byte[] bytes) {
        //写入长度
        out.writeInt(bytes.length);
        //写入负载
        out.writeBytes(bytes);
    }
}
