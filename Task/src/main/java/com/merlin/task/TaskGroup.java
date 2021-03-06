package com.merlin.task;

import com.task.debug.Debug;

import java.util.ArrayList;
import java.util.List;

public class TaskGroup extends Task{
    private List<Task> mTasks;
    private Task mExecuting=null;

    public TaskGroup(String name){
        this(name,null);
    }

    public TaskGroup(String name,List<Task> tasks){
        super(name);
        add(tasks);
    }

    public final boolean add(List<Task> tasks){
        List<Task> current=mTasks;
        List<Task> global=null!=tasks&&tasks.size()>0?(null!=current?current:(mTasks=new ArrayList<>())):null;
        return null!=global&&global.addAll(tasks);
    }

    public final boolean add(Task task){
        List<Task> current=mTasks;
        List<Task> global=null!=task?(null!=current?current:(mTasks=new ArrayList<>(1))):null;
        return null!=global&&global.add(task);
    }

    public final boolean remove(Object task,boolean pause,String debug){
        List<Task> global=null!=task?mTasks:null;
        int index=null!=global?global.indexOf(task):null;
        Task taskObj=index>=0&&index<global.size()?global.get(index):null;
        if (null!=taskObj&&global.remove(taskObj)){
            Debug.D("Remove task from group."+task);
            if (pause){
                taskObj.pause(true,"While remove from group.");
            }
            return true;
        }
        return false;
    }

    @Override
    protected final void onExecute(Networker networker) {
        final List<Task> tasks=mTasks;
        if (null!=tasks&&tasks.size()>0){
            Task task=getNextUnFinishTask(mExecuting);
            if (null==task){
                Debug.D("All task of group executed."+this);
                notifyStatus(Status.FINISH, What.WHAT_ERROR,"All task of group executed");
                return;
            }
            (mExecuting=task).execute(networker,
                    (int status, int what, String note, Object obj, Task child) ->{
                super.notifyStatus(status, what, note, obj);
            });
            mExecuting=null;
            onExecute(networker);
        }
    }

    public final int getFinishedSize(){
       List<Task> tasks= getFinished();
       return null!=tasks?tasks.size():-1;
    }

    public final int size(){
        List<Task> tasks=mTasks;
        return null!=tasks?tasks.size():-1;
    }

    public final Task getNextUnFinishTask(Task task){
        List<Task> tasks=mTasks;
        if (null!=tasks){
            synchronized (tasks){
                int currIndex=null!=task?tasks.indexOf(task):0;
                int size=tasks.size();

                if (currIndex>=0&&currIndex<size){
                    for (int i = currIndex; i < size; i++) {
                        if (null!=(task=tasks.get(i))&&task.isIdle()){
                            return task;
                        }
                    }
                }
            }
        }
        return null;
    }

    public final List<Task> getFinishSucceed(){
        return getTasks((Task task)->null!=task&&task.isFinishSucceed());
    }

    public final List<Task> getFinished(){
        return getTasks((Task task)->null!=task&&task.isFinished());
    }

    public final List<Task> getTasks(Matcher matcher) {
        List<Task> tasks=mTasks;
        int size=null!=tasks?tasks.size():-1;
        if (size>0){
            List<Task> result=null;
            if (null==matcher){
                (result=new ArrayList<>(size)).addAll(tasks);
            }else{
                result=new ArrayList(size);
                for (Task child:tasks) {
                    Boolean match=matcher.match(child);
                    if (null==match){
                        return result;
                    }else if (match){
                        result.add(child);
                    }
                }
            }
            return result;
        }
        return null;
    }

    public final Task getExecuting() {
        return mExecuting;
    }

    public final List<Task> getTasks() {
        return getTasks(null);
    }
}
