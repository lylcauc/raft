package com.raft.core.rpc;

import com.google.common.base.Preconditions;

// *@author liuyaolong
public class Address {

    private final String host;
    private final int port;

    public Address(String host, int port) {
        Preconditions.checkNotNull(host);
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
