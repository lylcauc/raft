package com.raft.core.node;

import com.google.common.base.Preconditions;
import com.raft.core.rpc.Address;

import java.util.Objects;

// *@author liuyaolong
public class NodeEndPoint {

    private final NodeId id;
    private final Address address;

    //节点ID，主机和端口的构造函数
    public NodeEndPoint(String id,String host,int port){
        this(new NodeId(id),new Address(host,port));
    }

    //节点和地址的构造函数
    public NodeEndPoint(NodeId id,Address address){
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(address);
        this.id=id;
        this.address=address;
    }

    public NodeId getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public int getPort(){
        return this.address.getPort();
    }

    public String getHost(){
        return this.address.getHost();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeEndPoint endPoint = (NodeEndPoint) o;
        return Objects.equals(id, endPoint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NodeEndPoint{" +
                "id=" + id +
                ", address=" + address +
                '}';
    }
}
