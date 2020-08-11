package com.raft.kv.server;

import com.raft.kv.MessageConstants;
import com.raft.kv.Protos;
import com.raft.kv.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

// *@author liuyaolong
public class Decoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //等待数据
        if (in.readableBytes() < 8) return;
        //记录读取位置
        in.markReaderIndex();
        int messageType = in.readInt();
        int payloadLength = in.readInt();
        //处理半包或者粘包
        if (in.readableBytes() < payloadLength) {
            in.resetReaderIndex();
            return;
        }
        //单条数据就绪
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                //成功
                out.add(Success.INSTANCE);
                break;
            case MessageConstants.MSG_TYPE_FAILURE:
                //失败
                Protos.Failure protoFailure = Protos.Failure.parseFrom(payload);
                out.add(new Failure(protoFailure.getErrorCode(), protoFailure.getMessage()));
                break;
            case MessageConstants.MSG_TYPE_REDIRECT:
                //重定向
                Protos.Redirect protoRedirect = Protos.Redirect.parseFrom(payload);
                out.add(new Redirect(protoRedirect.getLeaderId()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND:
                //GET命令
                Protos.GetCommand protoGetCommand = Protos.GetCommand.parseFrom(payload);
                out.add(new GetCommand(protoGetCommand.getKey()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                //GET响应
                Protos.GetCommandResponse protoGetCommandResponse = Protos.GetCommandResponse.parseFrom(payload);
                out.add(new GetCommandResponse(protoGetCommandResponse.getFound(), protoGetCommandResponse.getValue().toByteArray()));
                break;
            case MessageConstants.MSG_TYPE_SET_COMMAND:
                //SET命令
                Protos.SetCommand protoSetCommand = Protos.SetCommand.parseFrom(payload);
                out.add(new SetCommand(protoSetCommand.getKey(), protoSetCommand.getValue().toByteArray()));
                break;
            default:
                throw new IllegalStateException("unexpected message type " + messageType);
        }
    }
}
