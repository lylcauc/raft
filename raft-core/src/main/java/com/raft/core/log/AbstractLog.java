package com.raft.core.log;

import com.google.common.eventbus.EventBus;
import com.raft.core.log.entry.Entry;
import com.raft.core.log.entry.EntryMeta;
import com.raft.core.log.entry.GeneralEntry;
import com.raft.core.log.entry.NoOpEntry;
import com.raft.core.log.sequence.EntrySequence;
import com.raft.core.log.statemachine.EmptyStateMachine;
import com.raft.core.log.statemachine.StateMachine;
import com.raft.core.log.statemachine.StateMachineContext;
import com.raft.core.node.NodeId;
import com.raft.core.rpc.message.AppendEntriesRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

// *@author liuyaolong
abstract class AbstractLog implements Log{

    private final Logger logger= LoggerFactory.getLogger(AbstractLog.class);



    protected final EventBus eventBus;
    //protected Snapshot snapshot;
    protected EntrySequence entrySequence;

    //protected SnapshotBuilder snapshotBuilder = new NullSnapshotBuilder();
    //protected GroupConfigEntryList groupConfigEntryList = new GroupConfigEntryList();
//    private final StateMachineContext stateMachineContext = new StateMachineContextImpl();
    protected StateMachine stateMachine = new EmptyStateMachine();
    protected int commitIndex = 0;

    AbstractLog(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected AbstractLog() {
        eventBus=null;
    }


    //获取最后一条日志的元信息
    @Override
    @Nonnull
    public EntryMeta getLastEntryMeta() {
        if (entrySequence.isEmpty()) {
            return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        }
        return entrySequence.getLastEntry().getMeta();
    }
    //创建AppendEntries消息
    @Override
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) {
        //检查nextIndex
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            throw new IllegalArgumentException("illegal next index " + nextIndex);
        }
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setMessageId(UUID.randomUUID().toString());
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex);

        //设置前一条日志的元信息，有可能不存在
        Entry entry=entrySequence.getEntry(nextIndex-1);
        if(entry!=null){
            rpc.setPrevLogIndex(entry.getIndex());
            rpc.setPrevLogTerm(entry.getTerm());
        }
        //设置entries
        if (!entrySequence.isEmpty()) {
            int maxIndex = (maxEntries == ALL_ENTRIES ? nextLogIndex : Math.min(nextLogIndex, nextIndex + maxEntries));
            rpc.setEntries(entrySequence.subList(nextIndex, maxIndex));
        }
        return rpc;
    }
    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    //用于检查RequestVote中的投票检查
    @Override
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm) {
        EntryMeta lastEntryMeta = getLastEntryMeta();
        logger.debug("last entry ({}, {}), candidate ({}, {})", lastEntryMeta.getIndex(), lastEntryMeta.getTerm(), lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex() > lastLogIndex;
    }
    //追加No-OP条目
    @Override
    public NoOpEntry appendEntry(int term) {
        NoOpEntry entry = new NoOpEntry(entrySequence.getNextLogIndex(), term);
        entrySequence.append(entry);
        return entry;
    }
    //追加一般日志
    @Override
    public GeneralEntry appendEntry(int term, byte[] command) {
        GeneralEntry entry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
        entrySequence.append(entry);
        return entry;
    }
    @Override
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> leaderEntries) {
        logger.debug("看这里");
        logger.debug(prevLogIndex+"----"+prevLogTerm);

        // 检查前一条日志是否匹配
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
            return false;
        }

        // heartbeat（检查Leader节点传递过来的日志条目是否为空）
        if (leaderEntries.isEmpty()) {
            return true;
        }
        assert prevLogIndex + 1 == leaderEntries.get(0).getIndex();
        //移除冲突的日志条目并返回接下来要追加的日志条目（如果还有的话）
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));
        //仅追加日志
        appendEntriesFromLeader(newEntries);
        return true;
    }
    //顺序追加日志
    private void appendEntriesFromLeader(EntrySequenceView leaderEntries) {
        if (leaderEntries.isEmpty()) {
            return;
        }
        logger.debug("append entries from leader from {} to {}", leaderEntries.getFirstLogIndex(), leaderEntries.getLastLogIndex());
        for (Entry leaderEntry : leaderEntries) {
            entrySequence.append(leaderEntry);
            //appendEntryFromLeader(leaderEntry);
        }
    }

//    private void appendEntryFromLeader(Entry leaderEntry) {
//        entrySequence.append(leaderEntry);
//        if (leaderEntry instanceof GroupConfigEntry) {
//            eventBus.post(new GroupConfigEntryFromLeaderAppendEvent(
//                    (GroupConfigEntry) leaderEntry)
//            );
//        }
//    }

    private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries) {
        //Leader过来的节点不为空
        assert !leaderEntries.isEmpty();
        //找到第一个不匹配的日志索引
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        //没有不匹配的日志
        if(firstUnmatched<0){
            return new EntrySequenceView(Collections.emptyList());
        }
        //移除不匹配的日志索引开始的所有日志
        removeEntriesAfter(firstUnmatched - 1);
        //返回之后追加的日志条目
        return leaderEntries.subView(firstUnmatched);
    }
    //查找第一条不匹配的日志
    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries) {
        assert !leaderEntries.isEmpty();
        int logIndex;
        EntryMeta followerEntryMeta;
        //从前往后遍历leaderEntryMeta
        for (Entry leaderEntry : leaderEntries) {
            logIndex = leaderEntry.getIndex();
            //按照索引查找日志条目元信息
            followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            //日志不存在或者term不一致
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != leaderEntry.getTerm()) {
                return logIndex;
            }
        }
        //否则没有不一致的日志条目
        return -1;
        //return leaderEntries.getLastLogIndex() + 1;
    }

    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm) {
        logger.debug("匹配看这里");
        logger.debug(entrySequence.toString());

        if(prevLogIndex==0){
            if(prevLogTerm!=0){
                return false;
            }
            return true;
        }

        //检查指定索引的日志条目
        EntryMeta meta=entrySequence.getEntryMeta(prevLogIndex);
        //日志不存在
        if(meta==null){
            logger.debug("previous log {} not found",prevLogIndex);
            return false;
        }

//        Entry entry = entrySequence.getEntry(prevLogIndex);
//        if (entry == null) {
//            logger.debug("previous log {} not found", prevLogIndex);
//            return false;
//        }
//        int term=entry.getTerm();
        int term = meta.getTerm();
        if (term != prevLogTerm) {
            logger.debug("different term of previous log, local {}, remote {}", term, prevLogTerm);
            return false;
        }
        return true;
    }
    //移除指定索引后的所有日志条目
    private void removeEntriesAfter(int index) {
        if (entrySequence.isEmpty() || index >= entrySequence.getLastLogIndex()) {
            return;
        }
//        int lastApplied = stateMachine.getLastApplied();
//        if (index < lastApplied && entrySequence.subList(index + 1, lastApplied + 1).stream().anyMatch(this::isApplicable)) {
//            logger.warn("applied log removed, reapply from start");
//            applySnapshot(snapshot);
//            logger.debug("apply log from {} to {}", entrySequence.getFirstLogIndex(), index);
//            entrySequence.subList(entrySequence.getFirstLogIndex(), index + 1).forEach(this::applyEntry);
//        }
        //注意，此处如果移除了已经应用的日志，需要从头开始重新构建状态机
        logger.debug("remove entries after {}", index);
        entrySequence.removeAfter(index);
        if (index < commitIndex) {
            commitIndex = index;
        }
//        GroupConfigEntry firstRemovedEntry = groupConfigEntryList.removeAfter(index);
//        if (firstRemovedEntry != null) {
//            logger.info("group config removed");
//            eventBus.post(new GroupConfigEntryBatchRemovedEvent(firstRemovedEntry));
//        }
    }
    //推进commitIndex的方法
    @Override
    public void advanceCommitIndex(int newCommitIndex, int currentTerm) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) {
            return;
        }
        logger.debug("advance commit index from {} to {}", commitIndex, newCommitIndex);
        entrySequence.commit(newCommitIndex);
//        groupConfigsCommitted(newCommitIndex);
        //commitIndex = newCommitIndex;

        advanceApplyIndex();
    }

//    @Override
//    public void generateSnapshot(int lastIncludedIndex, Set<NodeEndpoint> groupConfig) {
//        logger.info("generate snapshot, last included index {}", lastIncludedIndex);
//        EntryMeta lastAppliedEntryMeta = entrySequence.getEntryMeta(lastIncludedIndex);
//        replaceSnapshot(generateSnapshot(lastAppliedEntryMeta, groupConfig));
//    }
//
    private void advanceApplyIndex() {
        // start up and snapshot exists
        int lastApplied = stateMachine.getLastApplied();
        //int lastIncludedIndex = snapshot.getLastIncludedIndex();
//        if (lastApplied == 0 && lastIncludedIndex > 0) {
//            assert commitIndex >= lastIncludedIndex;
//            applySnapshot(snapshot);
//            lastApplied = lastIncludedIndex;
//        }
        for (Entry entry : entrySequence.subList(lastApplied + 1, commitIndex + 1)) {
            applyEntry(entry);
        }
    }
//
//    private void applySnapshot(Snapshot snapshot) {
//        logger.debug("apply snapshot, last included index {}", snapshot.getLastIncludedIndex());
//        try {
//            stateMachine.applySnapshot(snapshot);
//        } catch (IOException e) {
//            throw new LogException("failed to apply snapshot", e);
//        }
//    }
//
    private void applyEntry(Entry entry) {
        // skip no-op entry and membership-change entry
//        if (isApplicable(entry)) {
//            stateMachine.applyLog(stateMachineContext, entry.getIndex(), entry.getCommandBytes(), entrySequence.getFirstLogIndex());
//        }
    }
//
    private boolean isApplicable(Entry entry) {
        return entry.getKind() == Entry.KIND_GENERAL;
    }
//
//    private void groupConfigsCommitted(int newCommitIndex) {
//        for (GroupConfigEntry groupConfigEntry : groupConfigEntryList.subList(commitIndex + 1, newCommitIndex + 1)) {
//            eventBus.post(new GroupConfigEntryCommittedEvent(groupConfigEntry));
//        }
//    }
    //检查新的commitIndex
    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm) {
        //小于当前的commitIndex
        if (newCommitIndex <= commitIndex) {
            return false;
        }
        Entry entry = entrySequence.getEntry(newCommitIndex);
        if (entry == null) {
            logger.debug("log of new commit index {} not found", newCommitIndex);
            return false;
        }
        //日志条目的term必须是当前term，才可以推进commitIndex
        if (entry.getTerm() != currentTerm) {
            logger.debug("log term of new commit index != current term ({} != {})", entry.getTerm(), currentTerm);
            return false;
        }
        return true;
    }

//    protected abstract Snapshot generateSnapshot(EntryMeta lastAppliedEntryMeta, Set<NodeEndpoint> groupConfig);
//
//    @Override
//    public InstallSnapshotState installSnapshot(InstallSnapshotRpc rpc) {
//        if (rpc.getLastIndex() <= snapshot.getLastIncludedIndex()) {
//            logger.debug("snapshot's last included index from rpc <= current one ({} <= {}), ignore",
//                    rpc.getLastIndex(), snapshot.getLastIncludedIndex());
//            return new InstallSnapshotState(InstallSnapshotState.StateName.ILLEGAL_INSTALL_SNAPSHOT_RPC);
//        }
//        if (rpc.getOffset() == 0) {
//            assert rpc.getLastConfig() != null;
//            snapshotBuilder.close();
//            snapshotBuilder = newSnapshotBuilder(rpc);
//        } else {
//            snapshotBuilder.append(rpc);
//        }
//        if (!rpc.isDone()) {
//            return new InstallSnapshotState(InstallSnapshotState.StateName.INSTALLING);
//        }
//        Snapshot newSnapshot = snapshotBuilder.build();
//        applySnapshot(newSnapshot);
//        replaceSnapshot(newSnapshot);
//        int lastIncludedIndex = snapshot.getLastIncludedIndex();
//        if (commitIndex < lastIncludedIndex) {
//            commitIndex = lastIncludedIndex;
//        }
//        return new InstallSnapshotState(InstallSnapshotState.StateName.INSTALLED, newSnapshot.getLastConfig());
//    }
//
//    protected abstract SnapshotBuilder newSnapshotBuilder(InstallSnapshotRpc firstRpc);
//
//    protected abstract void replaceSnapshot(Snapshot newSnapshot);
//
    @Override
    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
//
//    @Override
//    public void close() {
//        snapshot.close();
//        entrySequence.close();
//        snapshotBuilder.close();
//        stateMachine.shutdown();
//    }
//
//    private class StateMachineContextImpl implements StateMachineContext {
//
//        @Override
//        public void generateSnapshot(int lastIncludedIndex) {
//            eventBus.post(new SnapshotGenerateEvent(lastIncludedIndex));
//        }
//
//    }


    //按照日志索引获取、根据子视图检查等实际功能
    private static class EntrySequenceView implements Iterable<Entry> {

        private final List<Entry> entries;
        private int firstLogIndex;
        private int lastLogIndex;
        //构造函数
        EntrySequenceView(List<Entry> entries) {
            this.entries = entries;
            if (!entries.isEmpty()) {
                firstLogIndex = entries.get(0).getIndex();
                lastLogIndex = entries.get(entries.size() - 1).getIndex();
            }
        }
        //获取指定位置的日志条目
        Entry get(int index) {
            if (entries.isEmpty() || index < firstLogIndex || index > lastLogIndex) {
                return null;
            }
            return entries.get(index - firstLogIndex);
        }
        //判断是否为空
        boolean isEmpty() {
            return entries.isEmpty();
        }
        //获取第一条记录的索引，此处没有非空校验
        int getFirstLogIndex() {
            return firstLogIndex;
        }
        //获取最后一条记录的索引，此处没有非空校验
        int getLastLogIndex() {
            return lastLogIndex;
        }
        //获取子视图
        EntrySequenceView subView(int fromIndex) {
            if (entries.isEmpty() || fromIndex > lastLogIndex) {
                return new EntrySequenceView(Collections.emptyList());
            }
            return new EntrySequenceView(
                    entries.subList(fromIndex - firstLogIndex, entries.size())
            );
        }
        //遍历使用
        @Override
        @Nonnull
        public Iterator<Entry> iterator() {
            return entries.iterator();
        }

    }



}
