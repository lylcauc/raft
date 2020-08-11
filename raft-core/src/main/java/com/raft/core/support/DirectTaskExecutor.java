package com.raft.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

// *@author liuyaolong
public class DirectTaskExecutor extends AbstractTaskExector{

    private final boolean throwWhenFailed;
    public DirectTaskExecutor() {
        this(false);
    }

    public DirectTaskExecutor(boolean throwWhenFailed) {
        this.throwWhenFailed=throwWhenFailed;
    }

    @Override
    public Future<?> submit(Runnable task) {
        Preconditions.checkNotNull(task);
        FutureTask<?> futureTask=new FutureTask<>(task,null);
        futureTask.run();
        return futureTask;
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        Preconditions.checkNotNull(task);
        FutureTask<V> futureTask=new FutureTask<>(task);
        futureTask.run();
        return futureTask;
    }

    @Override
    public void submit(Runnable task, Collection<FutureCallback<Object>> callbacks) {
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callbacks);
        try {
            task.run();
                callbacks.forEach(c -> c.onSuccess(null));
            }catch (Throwable t){
                callbacks.forEach(c -> c.onFailure(t));
                if(throwWhenFailed){
                    throw t;
            }
        }
    }


    @Override
    public void shutdown() throws InterruptedException {

    }
}
