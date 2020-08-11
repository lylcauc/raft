package com.raft.core.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

// *@author liuyaolong
public class NodeGroup {

    private static final Logger logger= LoggerFactory.getLogger(NodeGroup.class);
    private final NodeId selfId;//当前节点ID
    private Map<NodeId,GroupMember> memberMap;//成员表

    //单节点构造函数
    NodeGroup(NodeEndPoint endPoint){
        this(Collections.singleton(endPoint),endPoint.getId());
    }
    //多节点构造函数
    NodeGroup(Collection<NodeEndPoint> endPoints,NodeId selfId){
        this.memberMap=bulidMemberMap(endPoints);
        this.selfId=selfId;
    }

    //从节点列表中构造成员映射表
    private Map<NodeId, GroupMember> bulidMemberMap(Collection<NodeEndPoint> endPoints) {
        Map<NodeId,GroupMember> map=new HashMap<>();
        for(NodeEndPoint endPoint:endPoints){
            map.put(endPoint.getId(),new GroupMember(endPoint));
        }

        //不允许成员表为空
        if(map.isEmpty()){
            throw new IllegalArgumentException("endpoint is empty");
        }
        return map;
    }
    //按照节点ID查找成员,找不到时抛出错误
    GroupMember findMember(NodeId id){
        GroupMember member=getMember(id);
        if(member==null){
            throw new IllegalArgumentException("no such node"+ id);
        }
        return member;
    }

    //按照节点ID查找成员,找不到时返回空
    GroupMember getMember(NodeId id){
        return memberMap.get(id);
    }
    //列出日志复制的对象节点，即除自己以外的所有节点
    Collection<GroupMember> listReplicationTarget(){
        return memberMap.values().stream().filter(
                m -> !m.idEquals(selfId)).collect(Collectors.toList());

    }

    Set<NodeEndPoint> listEndpointExceptSelf(){
        Set<NodeEndPoint> endPoints=new HashSet<>();
        for(GroupMember member:memberMap.values()){
            //判断是不是当前节点
            if(!member.getId().equals(selfId)){
                endPoints.add(member.getEndPoint());
            }
        }
        return endPoints;
    }

    int getMatchIndexOfMajor(){
        List<NodeMatchIndex> matchIndices=new ArrayList<>();

        for(GroupMember member:memberMap.values()){
            if(!member.idEquals(selfId)){
                matchIndices.add(new NodeMatchIndex(member.getId(),member.getMatchIndex()));
            }
        }

        int count=matchIndices.size();
        //没有节点的情况
        if(count==0){
            throw new IllegalStateException("standalone or no major node");
        }
        Collections.sort(matchIndices);
        logger.debug("match indices {}" ,matchIndices);
        //取排序后中间位置的matchIndex
        return matchIndices.get(count/2).getMatchIndex();
    }

    void resetReplicatingStates(int nextLogIndex) {
        for (GroupMember member : memberMap.values()) {
            if (!member.idEquals(selfId)) {
                member.setReplicatingState(new ReplicatingState(nextLogIndex));
            }
        }
    }


    //需要重写
    int getCountOfMajor(){
        return (int )memberMap.values().stream().count();
    }


    private static class NodeMatchIndex implements Comparable<NodeMatchIndex> {

        private final NodeId nodeId;
        private final int matchIndex;
        //构造函数
        NodeMatchIndex(NodeId nodeId, int matchIndex) {
            this.nodeId = nodeId;
            this.matchIndex = matchIndex;
        }
        //获取matchIndex
        int getMatchIndex() {
            return matchIndex;
        }

        @Override
        public int compareTo(@Nonnull NodeMatchIndex o) {
            return -Integer.compare(o.matchIndex, this.matchIndex);
        }

        @Override
        public String toString() {
            return "<" + nodeId + ", " + matchIndex + ">";
        }

    }

    GroupMember findSelf() {
        return findMember(selfId);
    }

}
