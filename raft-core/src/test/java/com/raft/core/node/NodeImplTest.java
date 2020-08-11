package com.raft.core.node;

import com.raft.core.node.role.CandidateNodeRole;
import com.raft.core.node.role.FollowerNodeRole;
import com.raft.core.node.role.LeaderNodeRole;
import com.raft.core.node.role.RoleName;
import com.raft.core.node.store.MemoryNodeStore;
import com.raft.core.rpc.MockConnector;
import com.raft.core.rpc.message.*;
import com.raft.core.schedule.NullScheduler;
import com.raft.core.support.DirectTaskExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// *@author liuyaolong
public class NodeImplTest {

    //快速构建测试用的NodeBuilder
    private NodeBuilder newNodeBuilder(NodeId selfId,NodeEndPoint... endPoints){
        return new NodeBuilder(Arrays.asList(endPoints),selfId)
                .setScheduler(new NullScheduler())
                .setConnector(new MockConnector())
                .setTaskExecutor(new DirectTaskExecutor());
    }

    //系统启动
    @Test
    public void testStart(){
        NodeImpl node= (NodeImpl) newNodeBuilder(NodeId.of("A"),
                new NodeEndPoint("A","localhost",2333))
                .build();
        node.start();
//        System.out.println(node.getRole().getName());
//        System.out.println(node.getRole().getTerm());
        FollowerNodeRole role= (FollowerNodeRole) node.getRole();
        Assert.assertEquals(0,role.getTerm());
    }

    //选举超时
    @Test
    public void testElectionTimeoutWhenFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout();
        CandidateNodeRole role= (CandidateNodeRole) node.getRole();
        Assert.assertEquals(1, role.getTerm());
        Assert.assertEquals(1, role.getVotesCount());
//        RoleState state = node.getRoleState();
//        Assert.assertEquals(RoleName.CANDIDATE, state.getRoleName());


        MockConnector mockConnector = (MockConnector) node.getContext().connector();
        RequestVoteRpc rpc = (RequestVoteRpc) mockConnector.getRpc();
        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(NodeId.of("A"), rpc.getCandidateId());
        Assert.assertEquals(0, rpc.getLastLogIndex());
        Assert.assertEquals(0, rpc.getLastLogTerm());
    }

    //收到RequestVote消息
    @Test
    public void testOnReceiveRequestVoteRpcFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335))
                .build();
        node.start();
        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(1);
        rpc.setCandidateId(NodeId.of("C"));
        rpc.setLastLogIndex(0);
        rpc.setLastLogTerm(0);
        node.onReceiveRequestVoteRpc(new RequestVoteRpcMessage(rpc, NodeId.of("C"), null));
        MockConnector mockConnector = (MockConnector) node.getContext().connector();
        RequestVoteResult result = (RequestVoteResult) mockConnector.getResult();
        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isVoteGranted());
        Assert.assertEquals(NodeId.of("C"), ((FollowerNodeRole)node.getRole()).getVotedFor());
    }

    //收到RequestVote响应
    @Test
    public void testOnReceiveRequestVoteResult() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout();

        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));
        LeaderNodeRole role= (LeaderNodeRole) node.getRole();
        Assert.assertEquals(1,role.getTerm());

    }
    //成为Leader后的心跳信息
    @Test
    public void testReplicateLog() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout(); // 发送RequestVote消息
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));
        node.replicateLog(); // 发送两条日志复制消息

        MockConnector mockConnector = (MockConnector) node.getContext().connector();
        //总共加起来三条消息
        Assert.assertEquals(3, mockConnector.getMessageCount());

        // 检查目标节点
        List<MockConnector.Message> messages = mockConnector.getMessages();
        Set<NodeId> destinationNodeIds = messages.subList(1, 3).stream()
                .map(MockConnector.Message::getDestinationNodeId)
                .collect(Collectors.toSet());
        Assert.assertEquals(2, destinationNodeIds.size());
        Assert.assertTrue(destinationNodeIds.contains(NodeId.of("B")));
        Assert.assertTrue(destinationNodeIds.contains(NodeId.of("C")));

        AppendEntriesRpc rpc = (AppendEntriesRpc) messages.get(2).getRpc();
        Assert.assertEquals(1, rpc.getTerm());
    }

    //收到来自leader节点的信息
    @Test
    public void testOnReceiveAppendEntriesRpcFollower() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335))
                .build();
        node.start();
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(1);
        rpc.setLeaderId(NodeId.of("B"));
        node.onReceiveAppendEntriesRpc(new AppendEntriesRpcMessage(rpc, NodeId.of("B"), null));
        MockConnector connector = (MockConnector) node.getContext().connector();
        AppendEntriesResult result = (AppendEntriesResult) connector.getResult();
        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isSuccess());

        FollowerNodeRole role= (FollowerNodeRole) node.getRole();
        Assert.assertEquals(RoleName.FOLLOWER, role.getName());
        Assert.assertEquals(1, role.getTerm());
        Assert.assertEquals(NodeId.of("B"), role.getLeaderId());
    }

    //leader节点收到其他节点的响应
//    @Test
//    public void testOnReceiveAppendEntriesRpcLeader() {
//        NodeImpl node = (NodeImpl) newNodeBuilder(
//                NodeId.of("A"),
//                new NodeEndPoint("A", "localhost", 2333),
//                new NodeEndPoint("B", "localhost", 2334),
//                new NodeEndPoint("C", "localhost", 2335))
//                .build();
//        node.start();
//        node.electionTimeout();//变为Candidate
//        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));//变为leader
//        node.replicateLog();
//        node.onReceiveAppendEntriesResult(new AppendEntriesResultMessage(
//                new AppendEntriesResult(,1,true),
//                NodeId.of("B"),
//                new AppendEntriesRpc()
//        ));
//
//    }
    @Test
    public void testOnReceiveAppendEntriesRpcLeader() {
        NodeImpl node = (NodeImpl) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndPoint("A", "localhost", 2333),
                new NodeEndPoint("B", "localhost", 2334),
                new NodeEndPoint("C", "localhost", 2335))
                .build();
        node.start();
        node.electionTimeout();
        node.onReceiveRequestVoteResult(new RequestVoteResult(2, true));
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(2);
        rpc.setLeaderId(NodeId.of("B"));
        node.onReceiveAppendEntriesRpc(new AppendEntriesRpcMessage(rpc, NodeId.of("B"), null));
        MockConnector connector = (MockConnector) node.getContext().connector();
        AppendEntriesResult result = (AppendEntriesResult) connector.getResult();
        Assert.assertEquals(2, result.getTerm());
        Assert.assertFalse(result.isSuccess());

//        RoleState state = node.getRoleState();
//        Assert.assertEquals(RoleName.LEADER, state.getRoleName());
//        Assert.assertEquals(2, state.getTerm());
    }



}
