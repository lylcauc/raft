package com.raft.core.service;

import com.raft.core.node.NodeId;

// *@author liuyaolong
public class RedirectException extends ChannelException {

    private final NodeId leaderId;

    public RedirectException(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }
}
