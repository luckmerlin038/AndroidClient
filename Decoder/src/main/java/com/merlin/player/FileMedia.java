package com.merlin.player;

import com.merlin.debug.Debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileMedia implements BytesMedia {
    private FileInputStream mInput=null;
    private final String mPath;
    private long mLength=0;

    public FileMedia(String path){
        mPath=path;
    }

    @Override
    public final boolean open() {
        final String mediaPath=mPath;
        if (null==mediaPath||mediaPath.length()<=0){
            Debug.W(getClass(),"Can't open media file which path is invalid."+mediaPath);
            return false;
        }
        FileInputStream input=mInput;
        if (input!=null){//Already opened
            return false;
        }
        try {
            File file=new File(mediaPath);
            mLength=file.length();
            if (mLength<=0){
                Debug.W(getClass(),"Can't open media file which length is invalid."+mediaPath);
                return false;
            }
            mInput=new FileInputStream(file);
            Debug.D(getClass(),"Opened media file."+mediaPath);
            return true;
        } catch (FileNotFoundException e) {
            Debug.E(getClass(),"Exception while open file media.e="+e+" "+mediaPath,e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final boolean isOpened() {
        FileInputStream input=mInput;
        return null!=input&&null!=input.getChannel();
    }

    @Override
    public boolean cache(CacheReady cacheReady) throws IOException {
//        if (null!=cacheReady){
//            FileInputStream input=mInput;
//            cacheReady.onCacheReady(null==input?Player.FATAL_ERROR:Player.NORMAL,input,mLength);
//            return true;
//        }
        //Do nothing
        return false;
    }

    @Override
    public int read(byte[] buffer, int offset) throws IOException {
        FileInputStream input=mInput;
        int length=null!=buffer?buffer.length:-1;
        if (null==input||length<0||offset<0||offset>length){
            Debug.W(getClass(),"Fail read media file bytes which input is NULL.");
            return Player.FATAL_ERROR;
        }
        return input.read(buffer,offset,length-offset);
    }

    @Override
    public Meta getMeta() {
        return new Meta(mLength);
    }

    @Override
    public final boolean close() {
        FileInputStream input=mInput;
        if (null!=input){
            mInput=null;
            mLength=0;
            try {
                input.close();
                Debug.D(getClass(),"Closed media file."+this);
                return true;
            } catch (IOException e) {
                Debug.E(getClass(),"Exception while close media file.e="+e+" "+this,e);
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        Meta meta=getMeta();
        String name=null!=meta?meta.getName():null;
        return (null!=name?name:"")+super.toString();
    }
}
