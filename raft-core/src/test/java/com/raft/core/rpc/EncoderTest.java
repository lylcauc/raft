package com.raft.core.rpc;

import com.raft.core.Protos;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.MessageConstants;
import com.raft.core.rpc.message.RequestVoteRpc;
import com.raft.core.rpc.nio.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

// *@author liuyaolong
public class EncoderTest {

//    @Test
//    public void testNodeId() throws Exception{
//        Encoder encoder=new Encoder();
//        ByteBuf buf= Unpooled.buffer();
//        encoder.encode(null, NodeId.of("A"),buf);
//
//       assert (MessageConstants.MSG_TYPE_NODE_ID ==buf.readInt());
//       assert (1==buf.readInt());
//       assert (((byte) 'A')==buf.readByte());
//    }

//    @Test
//    public void testRequestVoteRpc() throws Exception{
//        Encoder encoder=new Encoder();
//        ByteBuf buf=Unpooled.buffer();
//        RequestVoteRpc rpc=new RequestVoteRpc();
//        rpc.setLastLogIndex(2);
//        rpc.setLastLogTerm(1);
//        rpc.setTerm(2);
//        rpc.setCandidateId(NodeId.of("A"));
//        encoder.encode(null,rpc,buf);
//        Assert.assertEquals(MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC,buf.readInt());
//        buf.readInt();
//        Protos.RequestVoteRpc decodedRpc=Protos.RequestVoteRpc.parseFrom(new ByteBufInputStream(buf));
//        Assert.assertEquals(rpc.getLastLogIndex(),decodedRpc.getLastLogIndex());
//    }

}
