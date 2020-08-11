package com.raft.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;

import java.util.Collections;

// *@author liuyaolong
public abstract class AbstractTaskExector implements TaskExecutor{

    @Override
    public void submit(Runnable task, FutureCallback<Object> callback){
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callback);
        submit(task, Collections.singletonList(callback));
    }
}
