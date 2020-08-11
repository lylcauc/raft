package com.raft.kv.server;

import com.raft.kv.message.CommandRequest;
import com.raft.kv.message.GetCommand;
import com.raft.kv.message.SetCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// *@author liuyaolong
public class ServiceHandler extends ChannelInboundHandlerAdapter {

    private final Service service;

    public ServiceHandler(Service service){
        this.service=service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //分发命令
        if(msg instanceof GetCommand){
            service.get(new CommandRequest<>(
                    (GetCommand) msg,ctx.channel()
            ));
        }else if(msg instanceof SetCommand){
            service.set(new CommandRequest<>(
                    (SetCommand) msg,ctx.channel()
            ));
        }

    }
}
