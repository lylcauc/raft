package com.raft.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import java.util.Collection;
import java.util.concurrent.*;

// *@author liuyaolong
public class SingleThreadTaskExecutor extends AbstractTaskExector {

    private final ExecutorService executorService;
    //构造函数，默认
    public SingleThreadTaskExecutor(){
        this(Executors.defaultThreadFactory());
    }
    //构造函数，指定名称
    public SingleThreadTaskExecutor(String name){
        this(r -> new Thread(r, name));
    }
    //构造函数，指定ThreadFactory
    private SingleThreadTaskExecutor(ThreadFactory threadFactory){
        executorService=Executors.newSingleThreadExecutor(threadFactory);
    }



    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void submit(Runnable task, Collection<FutureCallback<Object>> callbacks) {
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callbacks);
        executorService.submit(() ->{
           try {
               task.run();
               callbacks.forEach(c -> c.onSuccess(null));
           }catch (Exception e){
               callbacks.forEach(c -> c.onFailure(e));
           }
        });
    }

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1,TimeUnit.SECONDS);
    }
}
