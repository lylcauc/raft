package com.raft.kv.client;

import com.raft.core.service.NoAvailableServerException;

// *@author liuyaolong
public class RaftRemoveNodeCommand implements Command{


    @Override
    public String getName() {
        return "raft-remove-node";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("usage " + getName() + " <node-id>");
        }

        try {
            context.getClient().removeNode(arguments);
        } catch (NoAvailableServerException e) {
            System.err.println(e.getMessage());
        }
    }
}
