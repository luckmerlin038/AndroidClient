package com.file.task;

import android.os.Handler;
import android.os.Looper;
import com.merlin.file.transport.FileUploadNasTask;
import com.merlin.task.Task;
import com.merlin.task.file.HttpDownloadTask;

public class TaskService extends com.merlin.task.TaskService {
    private Task mTask=new FileUploadNasTask("任务34242","",null,"");

    @Override
    public void onCreate() {
        super.onCreate();
        Handler handler= new Handler(Looper.getMainLooper());
//        String name,String from, String to,String method
        String path="{\n" +
                "        \"modifyTime\":1549323360,\n" +
                "        \"parent\":\"../../\",\n" +
                "        \"size\":-10009,\n" +
                "        \"mode\":33279,\n" +
                "        \"permissions\":33279,\n" +
                "        \"name\":\"rebot.zip\",\n" +
                "        \"accessTime\":1595076902,\n" +
                "        \"length\":9322928,\n" +
                "        \"port\":2018,\n" +
                "        \"md5\":null,\n" +
                "        \"mime\":\"audio/mpeg\",\n" +
                "        \"host\":\"192.168.168.3\",\n" +
                "        \"createTime\":1571472865,\n" +
                "        \"extension\":\".mp3\"\n" +
                "    }";


//        Task task=new NasFileDownloadTask(new Gson().fromJson(path, Path.class),
        Task task=new HttpDownloadTask("HHHS",
                "http://192.168.168.3:2018/.jpg.source.jpg",
                "/sdcard/LIN3.jpg", "POST");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mExecutor.addTask(task,"");
                mExecutor.start("");
//                mExecutor.removeTask((d)->{ return true;},null,"");
//                handler.postDelayed();
            }
        },3000);
    }
}
