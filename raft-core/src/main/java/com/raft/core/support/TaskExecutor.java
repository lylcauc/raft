package com.raft.core.support;

import com.google.common.util.concurrent.FutureCallback;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

// *@author liuyaolong
public interface TaskExecutor {

    //提交任务
    Future<?> submit(Runnable task);

    //提交任务，任务有返回值
    <V> Future<V> submit(Callable<V> task);

    void submit(Runnable task, FutureCallback<Object> callback);

    void submit(Runnable task, Collection<FutureCallback<Object>> callbacks);

    //关闭任务执行器
    void shutdown() throws InterruptedException;
}
