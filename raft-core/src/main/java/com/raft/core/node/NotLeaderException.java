package com.raft.core.node;

import com.google.common.base.Preconditions;
import com.raft.core.node.role.RoleName;

// *@author liuyaolong
public class NotLeaderException extends RuntimeException{

    private final RoleName roleName;
    private final NodeEndPoint leaderEndpoint;


    public NotLeaderException(RoleName roleName,NodeEndPoint leaderEndpoint){
        super("not leader");
        Preconditions.checkNotNull(roleName);
        this.roleName=roleName;
        this.leaderEndpoint=leaderEndpoint;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public NodeEndPoint getLeaderEndpoint() {
        return leaderEndpoint;
    }
}
