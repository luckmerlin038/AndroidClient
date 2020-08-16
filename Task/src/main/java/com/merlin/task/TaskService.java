package com.merlin.task;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.List;

public class TaskService extends Service {
    protected final TaskExecutor mExecutor=new TaskExecutor(new Handler(Looper.getMainLooper()));

    @Override
    public IBinder onBind(Intent intent) {
        return new TaskBinder(mExecutor);
    }

    protected final boolean execute(Task task,OnTaskUpdate callback,String debug){
        TaskExecutor executor=null!=task?mExecutor:null;
        return null!=executor&&executor.start(task,debug,callback);
    }
}
