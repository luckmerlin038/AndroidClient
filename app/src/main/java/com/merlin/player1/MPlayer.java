package com.merlin.player1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.merlin.debug.Debug;
import com.merlin.player.OnPlayerStatusChange;
import com.merlin.player.Playable;
import com.merlin.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MPlayer extends Player {
    public static final int PLAY_TYPE_NONE = 0x00; //0000 0000
    public static final int PLAY_TYPE_ORDER_NEXT = 0x01; //0000 0001
    public static final int PLAY_TYPE_PLAY_NOW = 0x02; //0000 0010
    public static final int PLAY_TYPE_ADD_INTO_QUEUE = 0x04; //0000 0100
    public static final int PLAY_TYPE_CLEAN_QUEUE = 0x08; //0000 1000
    private final List<Playable> mQueue=new ArrayList<>(1);
    private Indexer mIndexer;
    private AudioTrack mAudioTrack;

    public MPlayer(){
        this(null,null);
    }

    public MPlayer(String cacheFile,Indexer indexer){
        super(cacheFile);
        mIndexer=null!=indexer?indexer:new MIndexer();
    }

    @Override
    protected void onFrameDecoded(int mediaType, byte[] bytes, int channels, int sampleRate, int speed) {
        final int length=null!=bytes?bytes.length:-1;
        if (length<=0){//Invalid frame data
            return;
        }
        AudioTrack audioTrack=buildAudioTrack(sampleRate, AudioFormat.CHANNEL_IN_STEREO);
        if (null==audioTrack){
            Debug.W(getClass(),"Can't play media frame. sampleRate="+sampleRate);
            return;
        }
        if (audioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING){
            audioTrack.play();
        }
        audioTrack.write(bytes,0,length);
    }

    private AudioTrack buildAudioTrack(int sampleRateInHz,int channelConfig){
        if (sampleRateInHz<=0||channelConfig<=0){
            Debug.W(getClass(),"Can't build audio track.sampleRate="+sampleRateInHz+" channelConfig="+channelConfig);
            return null;
        }
        AudioTrack audioTrack=mAudioTrack;
        if (null!=audioTrack){
            int currSampleRateInHz=audioTrack.getSampleRate();
            int currChannelConfig=audioTrack.getChannelConfiguration();
            if (sampleRateInHz==currSampleRateInHz&&channelConfig==currChannelConfig){
                return audioTrack;
            }
            Debug.W(getClass(),"Release existed audio track when sampleRate changed."+
                    currSampleRateInHz+" "+sampleRateInHz+" "+currChannelConfig+" "+channelConfig);
            mAudioTrack=null;
            audioTrack.stop();
            audioTrack.release();
        }
        mAudioTrack=audioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, AudioTrack.getMinBufferSize(sampleRateInHz,
                channelConfig, AudioFormat.ENCODING_PCM_16BIT),AudioTrack.MODE_STREAM);
        return audioTrack;
    }

    public final boolean setIndexer(Indexer indexer){
        if (null!=indexer){
            mIndexer=indexer;
            return true;
        }
        return false;
    }

    public final Indexer getIndexer() {
        return mIndexer;
    }

    public final boolean cleanQueue(String debug){
        List<Playable> queue=mQueue;
        int size=null!=queue?queue.size():-1;
        if (size>0){
            Debug.D(getClass(),"Clean playing queue("+size+")"+(null!=debug?debug:"."));
            queue.clear();
            return true;
        }
        return false;
    }

    public final boolean pre(double seek,OnPlayerStatusChange change,String debug){
        Indexer indexer=mIndexer;
        return null!=indexer&&play(indexer.pre(getPlayingIndex(null),size()),seek,change,debug);
    }

    public final boolean next(double seek, OnPlayerStatusChange change, String debug){
        return next(true,seek,change,debug);
    }

    private final boolean next(boolean user,double seek,OnPlayerStatusChange change,String debug){
        Indexer indexer=mIndexer;
        return null!=indexer&&play(indexer.next(getPlayingIndex(null),size(),user),seek,change,debug);
    }

    public final int getPlayingIndex(Object arg){
        return getPlayingIndex(arg,null);
    }

    public final int getPlayingIndex(Object arg,Boolean justPlaying){
        Playable playing=getPlaying(arg,justPlaying);
        return null!=playing?index(playing):-1;
    }

    public final boolean play(int index,double seek,OnPlayerStatusChange change,String debug){
        Playable playable=get(index);
        return null!=playable&&play(playable,seek,change,false,debug);
    }

    public final boolean play(Playable playable, double seek){
        return play(playable,seek,null,false,null);
    }

    public final boolean play(Playable playable, double seek, OnPlayerStatusChange change,boolean add, String debug){
        if (null==playable){
            Debug.W(getClass(),"Can't play media which playable is NULL "+(null!=debug?debug:"."));
            notifyStatusChange(STOP,playable,null,"While playable is NULL.",change);
            return false;
        }
        if (super.play(playable,seek)){//Play succeed
            return (add&&append(playable,true))||true;
        }
        Debug.W(getClass(),"Fail play media."+playable+(null!=debug?debug:"."));
        return false;
    }

    public final boolean exist(Object playable){
        return null!=playable&&index(playable)>=0;
    }

    public final boolean append(Playable playable,boolean skipExist){
        List<Playable> queue=null!=playable?mQueue:null;
        if (null!=queue){
            synchronized (queue){
                if((!skipExist||!queue.contains(playable))&&queue.add(playable)){
                    notifyStatusChange(ADD,playable,queue.size()-1,null);
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean remove(Playable playable){
        List<Playable> queue=null!=playable?mQueue:null;
        if (null!=queue){
            synchronized (queue){
                if (queue.remove(playable)){
                    notifyStatusChange(REMOVE,playable,null,null);
                    return true;
                }
            }
        }
        return false;
    }

    public final boolean toggle(int status, Object arg, String debug){
        switch (status){
            case STOP:
                return stop(arg,debug);
            case PAUSE:
                return pause(arg,debug);
            case START:
                return start(arg,debug);
        }
        return false;
    }

    public final boolean listener(int status, OnPlayerStatusChange change, String debug){
        switch (status){
            case ADD:
                return addListener(change);
            case REMOVE:
                return removeListener(change);
        }
        return false;
    }

    public final int size(){
        List<Playable> queue=mQueue;
        if (null!=queue){
            synchronized (queue){
                return queue.size();
            }
        }
        return -1;
    }

    public final Playable get(Object index){
        List<Playable> queue=null!=index?mQueue:null;
        if (null!=queue){
            synchronized (queue){
                int position=index instanceof Integer?((Integer)index):queue.indexOf(index);
                return position>=0&&position<queue.size()?queue.get(position):null;
            }
        }
        return null;
    }

    public final int index(Object object){
        List<Playable> queue=null!=object?mQueue:null;
        if (null!=queue){
            synchronized (queue){
                return queue.indexOf(object);
            }
        }
        return -1;
    }

    public final List<Playable> getQueue(boolean containPlaying) {
        List<Playable> result=null;
        List<Playable> list=mQueue;
        int size=null!=list?list.size():0;
        if (size>0){
            result=new ArrayList<>(size);
            result.addAll(list);
        }
        if (containPlaying) {
            Playable playing = getPlaying(null);
            if (null != playing && !(null==result?result=new ArrayList<>(1):result).contains(playing)) {
                result.add(playing);
            }
        }
        return result;
    }

}
