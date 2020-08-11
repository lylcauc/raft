package com.raft.core.rpc;

import com.raft.core.Protos;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.MessageConstants;
import com.raft.core.rpc.message.RequestVoteRpc;
import com.raft.core.rpc.nio.Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

// *@author liuyaolong
public class DecoderTest {

    @Test
    public void testNodeId() throws Exception{
        ByteBuf buf= Unpooled.buffer();
        buf.writeInt(MessageConstants.MSG_TYPE_NODE_ID);
        buf.writeInt(1);
        buf.writeByte((byte)'A');
        Decoder decoder=new Decoder();
        List<Object> out=new ArrayList<>();
        decoder.decode(null,buf,out);
        Assert.assertEquals(NodeId.of("A"),out.get(0));
    }

    @Test
    public void testRequestVoteRpc() throws Exception{
        Protos.RequestVoteRpc rpc=Protos.RequestVoteRpc.newBuilder()
                .setLastLogIndex(2)
                .setLastLogTerm(1)
                .setTerm(2)
                .setCandidateId("A")
                .build();
        ByteBuf buf=Unpooled.buffer();
        buf.writeInt(MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC);
        byte[] rpcBytes=rpc.toByteArray();
        buf.writeInt(rpcBytes.length);
        buf.writeBytes(rpcBytes);
        Decoder decoder=new Decoder();
        List<Object> out=new ArrayList<>();
        decoder.decode(null,buf,out);
        RequestVoteRpc decodedRpc= (RequestVoteRpc) out.get(0);
        Assert.assertEquals(rpc.getLastLogIndex(),decodedRpc.getLastLogIndex());
    }

}
