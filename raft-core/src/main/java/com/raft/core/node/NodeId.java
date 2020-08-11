package com.raft.core.node;



import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Objects;

// *@author liuyaolong
public class NodeId implements Serializable {

    private final String value;

    //构造函数
    public NodeId(String value){
        Preconditions.checkNotNull(value);
        this.value=value;
    }

    //快速创建实例得一个静态方法
    public static NodeId of(String value){
        return new NodeId(value);
    }

    //与hashCode方法一起重载
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof NodeId)) return false;
        NodeId id= (NodeId) o;

        return Objects.equals(value,id.value);
    }

    //获取内部的标示符
    public String getValue() {
        return value;
    }

    //获取Hash码
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
