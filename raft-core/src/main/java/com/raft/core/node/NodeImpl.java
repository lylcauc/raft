package com.raft.core.node;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.raft.core.log.entry.EntryMeta;
import com.raft.core.log.statemachine.StateMachine;
import com.raft.core.node.role.*;
import com.raft.core.node.store.NodeStore;
import com.raft.core.rpc.message.*;
import com.raft.core.schedule.ElectionTimeout;
import com.raft.core.schedule.LogReplicationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

// *@author liuyaolong
public class NodeImpl implements Node{

    private static final Logger logger= LoggerFactory.getLogger(NodeImpl.class);//日志


    private final NodeContext context;//核心组件上下文

    private boolean started;//是否已启动

    private AbstractNodeRole role;//当前的角色及信息

    NodeImpl(NodeContext context){
        this.context=context;
    }

    //获取核心上下文组件
    NodeContext getContext(){
        return this.context;
    }
    //获取当前角色
    AbstractNodeRole getRole(){
        return this.role;
    }

    // callback for async tasks.
    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {
            //logger.info("success");
        }

        @Override
        public void onFailure(@Nonnull Throwable t) {
            logger.warn("failure", t);
        }
    };

    @Override
    public synchronized void start() {
        if(started){
            return;
        }
        //注册自己到EventBus
        context.eventBus().register(this);

        //初始化连接器
        context.connector().initialize();

        //启动时为Follower角色
        NodeStore store=context.store();
        changeToRole(new FollowerNodeRole(store.getTerm(),
                store.getVotedFor(),null,scheduleElectionTimeout()));
        started=true;
    }
    //统一的角色变更方法
    private void changeToRole(AbstractNodeRole newRole) {
        logger.debug("node {}, role state changed -> {}",context.selfId(),newRole);

        NodeStore store=context.store();
        store.setTerm(newRole.getTerm());
        if(newRole.getName()== RoleName.FOLLOWER){
            store.setVotedFor(((FollowerNodeRole) newRole).getVotedFor());
        }

        role=newRole;
    }

    private ElectionTimeout scheduleElectionTimeout() {
        return context.scheduler().scheduleElectionTimeout(this::electionTimeout);
    }



    @Override
    public void stop() throws InterruptedException{
        //不允许没有启动时关闭
        if(!started){
            throw new IllegalStateException("node not start");
        }
        //关闭定时器
        context.scheduler().stop();
        //关闭连接器
        context.connector().close();
        //关闭任务执行器
        context.taskExecutor().shutdown();
        started=false;
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        Preconditions.checkNotNull(stateMachine);
        context.log().setStateMachine(stateMachine);
    }

    @Override
    public void appendLog(byte[] commandBytes) {
        Preconditions.checkNotNull(commandBytes);
        ensureLeader();
        context.taskExecutor().submit(() ->{
            context.log().appendEntry(role.getTerm(),commandBytes);
            doReplicateLog();
        },LOGGING_FUTURE_CALLBACK);
    }

    private void ensureLeader() {
        RoleNameAndLeaderId result = role.getNameAndLeaderId(context.selfId());
        if (result.getRoleName() == RoleName.LEADER) {
            return;
        }
        NodeEndPoint endpoint = result.getLeaderId() != null ? context.group().findMember(result.getLeaderId()).getEndPoint(): null;
        throw new NotLeaderException(result.getRoleName(), endpoint);
    }

    @Override
    public RoleNameAndLeaderId getRoleNameAndLeaderId() {
        return role.getNameAndLeaderId(context.selfId());
    }

    void electionTimeout(){
        context.taskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout(){
        //leader角色下不可能有选举超时
        if(role.getName()==RoleName.LEADER){
            logger.warn("node {} ,current role is leader, ignore election timeout",context.selfId());
            return;
        }

        //对于follower节点来说是发起选举
        //对于candidate节点来说是再次发起选举
        //选举term加1
        int newTerm=role.getTerm()+1;
        role.cancelTimeoutOrTask();
        logger.info("start election");
        //变为candidate角色
        changeToRole(new CandidateNodeRole(newTerm,scheduleElectionTimeout()));

        //发送RequestVote消息
        EntryMeta lastEntryMeta=context.log().getLastEntryMeta();

        RequestVoteRpc rpc=new RequestVoteRpc();
        rpc.setTerm(newTerm);
        rpc.setCandidateId(context.selfId());
        rpc.setLastLogIndex(lastEntryMeta.getIndex());
        rpc.setLastLogTerm(lastEntryMeta.getTerm());
        context.connector().sendRequestVote(rpc,context.group().listEndpointExceptSelf());

    }

    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage){

        context.taskExecutor().submit(
                () -> context.connector().replyRequestVote(
                        doProcessRequestVoteRpc(rpcMessage),rpcMessage),
                        LOGGING_FUTURE_CALLBACK
        );
    }

//    @Subscribe
//    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage){
//        context.taskExecutor().submit(
//                () -> context.connector().replyRequestVote(
//                        doProcessRequestVoteRpc(rpcMessage),
//                        //发送消息的节点
//                        context.group().findMember(rpcMessage.getSourceNodeId()).getEndPoint()
//                )
//        );
//    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        //如果对方的term比自己小，则不投票并且返回自己的term给对象
        RequestVoteRpc rpc=rpcMessage.get();
        if(rpc.getTerm()<role.getTerm()){
            logger.debug("term from rpc < current term ,don't vote ({} < {})",
                    rpc.getTerm(),role.getTerm());
            return new RequestVoteResult(role.getTerm(),false);
        }
        boolean voteForCandidate=!context.log().isNewerThan(rpc.getLastLogIndex(),rpc.getLastLogTerm());
        //如果对象的term比自己大，则切换为follower角色
        if(rpc.getTerm()>role.getTerm()){
            becomeFollower(rpc.getTerm(),(voteForCandidate? rpc.getCandidateId():null),null,true);
            return new RequestVoteResult(rpc.getTerm(),voteForCandidate);
        }

        //本地的term与消息的term一致
        switch (role.getName()){
            case FOLLOWER:
                FollowerNodeRole follower = (FollowerNodeRole) role;
                NodeId votedFor = follower.getVotedFor();
                //以下两种情况下投票
                //case 1,自己尚未投过票
                //case 2,自己已经给对方投过票
                //投票后需要切换为Follower角色
                if((votedFor==null && voteForCandidate) || //case1
                Objects.equals(votedFor,rpc.getCandidateId())){// case2
                    becomeFollower(role.getTerm(),rpc.getCandidateId(),null,true);
                    return new RequestVoteResult(rpc.getTerm(),true);
                }
                return new RequestVoteResult(role.getTerm(),false);
            case CANDIDATE: //已经给自己投过票，所以不会给其他节点投票
            case LEADER:
                return new RequestVoteResult(role.getTerm(),false);
            default:
                throw new IllegalStateException("unexpected node role["+role.getName()+"]");
        }

    }
    public void becomeFollower(int term,NodeId votedFor,NodeId leaderId,boolean scheduleElectionTimeout){
        role.cancelTimeoutOrTask();//取消超时或定时器
        if(leaderId!=null && !leaderId.equals(role.getLeaderId(context.selfId()))){
            logger.info("current leader is {} ,term {}" ,leaderId,term);
        }
        //重新创建选举超时定时器或者空定时器
        ElectionTimeout electionTimeout=scheduleElectionTimeout?
                scheduleElectionTimeout():ElectionTimeout.NONE;
        changeToRole(new FollowerNodeRole(term,votedFor,leaderId,electionTimeout));
    }

    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result){
        context.taskExecutor().submit(
                () -> doProcessRequestVoteResult(result)
        );
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

        //如果对象的term比自己大，则退化为follower对象
        if(result.getTerm() > role.getTerm()){
            becomeFollower(result.getTerm(),null,null,true);
            return;
        }
        //如果自己不是Candidate角色
        if(role.getName()!=RoleName.CANDIDATE){
            logger.debug("receive request vote result and current role is not candidate,ignore");
            return;
        }

        //当前票数
        int currentVotesCount=( (CandidateNodeRole) role).getVotesCount()+1;
        int countOfMajor=context.group().getCountOfMajor();
        logger.debug("votes count {} ,node count {}",currentVotesCount,countOfMajor);
        //取消选举超时定时器
        role.cancelTimeoutOrTask();
        if(currentVotesCount> countOfMajor/2){ //票数过半
            //成为leader角色
            logger.info("become leader, term{}",role.getTerm());
            resetReplicatingStates();
            changeToRole(new LeaderNodeRole(role.getTerm(),scheduleLogReplicationTask()));
            context.log().appendEntry(role.getTerm());// no-op log
            context.connector().resetChannels(); // close all inbound channels
        }else{
            changeToRole(new CandidateNodeRole(role.getTerm(),currentVotesCount,scheduleElectionTimeout()));
        }
    }
    private void resetReplicatingStates() {
        context.group().resetReplicatingStates(context.log().getNextIndex());
    }

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    void replicateLog() {
        context.taskExecutor().submit((Runnable) this::doReplicateLog);
    }

    private void doReplicateLog() {
        logger.debug("replicate log");
        for(GroupMember member:context.group().listReplicationTarget()){
            doReplicateLog(member,context.config().getMaxReplicationEntries());
        }
    }
    //发送AppendEntries消息
    private void doReplicateLog(GroupMember member, int maxEntries) {
        member.replicateNow();
        AppendEntriesRpc rpc=context.log().createAppendEntriesRpc(
                role.getTerm(),context.selfId(),member.getNextIndex(),maxEntries
        );
        context.connector().sendAppendEntries(rpc,member.getEndPoint());
    }

//    @Subscribe
//    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage){
//        context.taskExecutor().submit(
//                () -> context.connector().replyAppendEntries(
//                        doProcessAppendEntriesRpc(rpcMessage),
//                        //发送消息的节点
//                        context.group().findMember(rpcMessage.getSourceNodeId()).getEndPoint()
//                )
//        );
//    }

    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage){
        context.taskExecutor().submit(
                () -> context.connector().replyAppendEntries(
                        doProcessAppendEntriesRpc(rpcMessage),rpcMessage),
                        LOGGING_FUTURE_CALLBACK
        );
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc=rpcMessage.get();
        //如果对方的term比自己小，则回复自己的term
        if(rpc.getTerm()<role.getTerm()){ // case 1
            return new AppendEntriesResult(rpc.getMessageId(),role.getTerm(),false);
        }
        //如果对方的term比自己大，则退化为follower角色
        if(rpc.getTerm()>role.getTerm()){ //case 2
            becomeFollower(rpc.getTerm(),null,rpc.getLeaderId(),true);
            //并追加日志
            return new AppendEntriesResult(rpc.getMessageId(),rpc.getTerm(),appendEntries(rpc));
        }
        assert rpc.getTerm()==role.getTerm();
        
        switch (role.getName()){
            case FOLLOWER: //case3
                logger.info("node {}, receive AppendEntries from {}",role.getName(),rpc.getLeaderId());
                //设置leader并重置选举定时器
                becomeFollower(rpc.getTerm(),((FollowerNodeRole) role).getVotedFor(),
                        rpc.getLeaderId(),true);
                //追加日志
                return new AppendEntriesResult(rpc.getMessageId(),rpc.getTerm(),appendEntries(rpc));
            case CANDIDATE: //case 4
                //如果有两个Candidate角色，并且另一个Candidate先成了leader
                //则当前节点退化为Follower角色并重置选举定时器
                becomeFollower(rpc.getTerm(),null,rpc.getLeaderId(),true);
                //追加日志
                return new AppendEntriesResult(rpc.getMessageId(),rpc.getTerm(),appendEntries(rpc));
            case LEADER:
                //leader收到AppendEntries消息，打印警告日志
                logger.warn("receive append entries rpc from another {},ignore",rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getMessageId(),rpc.getTerm(),false);
            default:
                throw new IllegalStateException("unexpected node role ["+role.getName()+"]");
        }
    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean result=context.log().appendEntriesFromLeader(
                rpc.getPrevLogIndex(),rpc.getPrevLogTerm(),rpc.getEntries());
        if(result){
            context.log().advanceCommitIndex(
                    Math.min(rpc.getLeaderCommit(),rpc.getLastEntryIndex()),rpc.getTerm());
        }
        return result;
    }
    
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage){
        context.taskExecutor().submit(
                () -> doProcessAppendEntriesResult(resultMessage)
        );
    }

    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result=resultMessage.get();
        //如果对方的term比自己大，则退化为Follower角色
        if(result.getTerm()>role.getTerm()){
            becomeFollower(result.getTerm(),null,null,true);
            return;
        }
        //检查自己的角色
        if(role.getName()!=RoleName.LEADER){
            logger.warn("receive append entries result from node {} but current node is not leader, ignore",
                    resultMessage.getSourceNodeId());
        }

        //计算新的commitIndex
        NodeId sourceNodeId = resultMessage.getSourceNodeId();
        GroupMember member = context.group().getMember(sourceNodeId);

        //没有指定的成员
        if(member==null){
            logger.info("unexpected append entries result from node {} ,node maybe removed",sourceNodeId);
            return;
        }

        AppendEntriesRpc rpc=resultMessage.getRpc();
        if(result.isSuccess()){
            //回复成功
            //推进matchIndex和nextIndex
            if(member.advanceReplicatingState(rpc.getLastEntryIndex())){
                //推进本地的commitIndex
                context.log().advanceCommitIndex(
                        context.group().getMatchIndexOfMajor(),role.getTerm()
                );
            }else{
                //对方回复失败
                if(!member.backOffNextIndex()){
                    logger.warn("can't back off next index more, node {}",sourceNodeId);
                }
            }
        }
    }




}
