package com.merlin.media;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.merlin.bean.Media;
import com.merlin.debug.Debug;
import com.merlin.player.OnPlayerStatusUpdate;
import com.merlin.player.Playable;
import com.merlin.player.Status;
import com.merlin.player1.MPlayer;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayService extends Service implements Status {
    private final MPlayer mPlayer=new MPlayer();
    private final static String LABEL_MEDIAS="medias";
    private final static String LABEL_POSITION="position";
    private final static String LABEL_ADD_INTO_QUEUE="addIntoQueue";
    private final static String LABEL_PLAY="play";
    private final static String LABEL_INDEX="index";
    private final static List<ServiceConnection> mConnections=new ArrayList<>();

    private final MediaPlayer mPlayerBinder=new MediaPlayer(){
        @Override
        public boolean play(Object object, float seek, OnPlayerStatusUpdate update) {
            MPlayer player=mPlayer;
            return null!=player&&player.play(object,seek,update);
        }

        @Override
        public boolean pre() {
            MPlayer player=mPlayer;
            return null!=player&&player.playPre();
        }

        @Override
        public boolean next() {
            MPlayer player=mPlayer;
            return null!=player&&player.playNext(true);
        }

        @Override
        public boolean pause(boolean stop, Object... obj) {
            MPlayer player=mPlayer;
            return null!=player&&player.pause(stop,obj);
        }

        @Override
        public Mode playMode(Mode mode) {
            MPlayer player=mPlayer;
            return null!=player?player.playMode(mode):null;
        }

        @Override
        public boolean togglePlayPause(Object media) {
            MPlayer player=mPlayer;
            return null!=player&& player.togglePausePlay(media);
        }

        @Override
        public int getPlayState() {
            MPlayer player=mPlayer;
            return null!=player?player.getPlayerStatus(): STATUS_UNKNOWN;
        }

        @Override
        public long getDuration() {
            MPlayer player=mPlayer;
            return null!=player?player.getDuration():0;
        }

        @Override
        public long getPosition() {
            MPlayer player=mPlayer;
            return null!=player?player.getPosition():0;
        }

        @Override
        public Playable getPlaying(Object... obj) {
            MPlayer player=mPlayer;
            return null!=player?player.getPlayingMedia(obj):null;
        }

        @Override
        public List<Media> getQueue() {
            MPlayer player=mPlayer;
            return null!=player?player.getQueue():null;
        }

        @Override
        public boolean addListener(OnPlayerStatusUpdate update) {
            MPlayer player=mPlayer;
            return null!=player&&null!=update&&player.addListener(update);
        }

        @Override
        public boolean removeListener(OnPlayerStatusUpdate update) {
            MPlayer player=mPlayer;
            return null!=player&&null!=update&&player.removeListener(update);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.D(getClass(),"Media play service onCreate.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle=null!=intent?intent.getExtras():null;
        handStartIntent(bundle);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handStartIntent(Bundle bundle){
        Object object=null!=bundle?bundle.get(LABEL_MEDIAS):null;
        if (null!=object){
            if (object instanceof Media){
                if (bundle.getBoolean(LABEL_PLAY,false)){
                    Object pos=bundle.get(LABEL_POSITION);
                    float seek= null!=pos?pos instanceof Integer?(float)((int) pos):pos instanceof Float?(Float)pos:0:0;
                    mPlayerBinder.play(object,seek,null);
                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.D(getClass(),"Media play service onDestroy.");
        MPlayer player=mPlayer;
        if (null!=player){
            player.destroy();
        }
    }

    public static boolean start(Context context, Intent intent){
        if (null!=context){
            intent=null!=intent?intent:new Intent();
            intent.setClass(context,MediaPlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startService(intent);
            }else{
                context.startService(intent);
            }
            return true;
        }
        Debug.W(MediaPlayService.class,"Can't start media play service. context="+context);
        return false;
    }

    public static boolean play(Context context, Media media,int position, boolean addIntoQueue){
        if (null!=media&&null!=context) {
            Intent intent = new Intent();
            intent.putExtra(LABEL_MEDIAS, media);
            intent.putExtra(LABEL_POSITION, position);
            intent.putExtra(LABEL_ADD_INTO_QUEUE, addIntoQueue);
            intent.putExtra(LABEL_PLAY, true);
            return start(context, intent);
        }
        Debug.W(MediaPlayService.class,"Can't play media by start service.media="+media+" context="+context);
        return false;
    }

    public static boolean add(Context context, Media media, int index){
        if (null!=context&&null!=media){
            Intent intent=new Intent();
            intent.putExtra(LABEL_MEDIAS,media);
            intent.putExtra(LABEL_INDEX,index);
            return start(context,intent);
        }
        Debug.W(MediaPlayService.class,"Can't add media by start service.media="+media+" context="+context);
        return false;
    }

    public static boolean bind(Activity activity){
        if (null!=activity){
            if (mConnections.contains(activity)){
                Debug.W(MediaPlayService.class,"Already bind activity.");
                return false;
            }
            if (start(activity,null)){
                if (activity instanceof ServiceConnection){
                    Intent intent=new Intent(activity,MediaPlayService.class);
                    Debug.D(MediaPlayService.class,"Bind media play service for "+activity.getClass());
                    mConnections.add((ServiceConnection)activity);
                    if(activity.bindService(intent,(ServiceConnection)activity,Context.BIND_AUTO_CREATE)){
                        return true;
                    }
                    mConnections.remove(activity);
                    Debug.W(MediaPlayService.class,"Bind media play service failed.");
                    return false;
                }
                Debug.W(MediaPlayService.class,"Can't bind media play service.Activity not extends service connection.");
                return false;
            }
            Debug.W(MediaPlayService.class,"Can't bind media play service,Start failed.");
            return false;
        }
        return false;
    }

    public static boolean unbind(Activity activity){
        if (null!=activity){
            if (activity instanceof ServiceConnection){
                Debug.D(MediaPlayService.class,"UnBind media play service for "+activity.getClass());
                mConnections.remove(activity);
                activity.unbindService((ServiceConnection)activity);
                return true;
            }
            return false;
        }
        Debug.W(MediaPlayService.class,"Can't bind media play service.Activity not extends service connection.");
        return false;
    }

    public static boolean isRunning(Context context){
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
//        if (!(serviceList.size() > 0)) {
//            return false;
//        }
//        for (int i = 0; i < serviceList.size(); i++) {
//            RunningServiceInfo serviceInfo = serviceList.get(i);
//            ComponentName serviceName = serviceInfo.service;
//
//            if (serviceName.getClassName().equals(className)) {
//                return true;
//            }
//        }
        return false;
    }

}
