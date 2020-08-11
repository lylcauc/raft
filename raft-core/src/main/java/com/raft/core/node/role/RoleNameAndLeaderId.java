package com.raft.core.node.role;

import com.google.common.base.Preconditions;
import com.raft.core.node.NodeId;

// *@author liuyaolong
public class RoleNameAndLeaderId {

    private final RoleName roleName;
    private final NodeId leaderId;

    public RoleNameAndLeaderId(RoleName roleName, NodeId leaderId) {
        Preconditions.checkNotNull(roleName);
        this.roleName = roleName;
        this.leaderId = leaderId;
    }

    public RoleName getRoleName(){
        return roleName;
    }

    public NodeId getLeaderId(){
        return leaderId;
    }
}
