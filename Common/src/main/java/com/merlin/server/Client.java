package com.merlin.server;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StatFs;

import java.io.File;

public final class Client implements Parcelable {
    private String host;
    private String account;
    private String deviceType;
    private String name;
    private String imageUrl;
    private String platform;
    private String folder;
    private String pathSep;
    private long free;
    private long total;
    private final static String LOCAL_URL="http://127.0.0.1";

    public Client(String name, String host, String account, String imageUrl, String folder, String pathSep){
        this.name=name;
        this.host=host;
        this.account=account;
        this.imageUrl=imageUrl;
        this.folder=folder;
        this.pathSep=pathSep;
    }

    public static Client buildLocalClient(Context context){
        Client meta=new Client(null);
        meta.host=LOCAL_URL;
        meta.platform="Android";
        meta.deviceType="Mobile";
        meta.name="Local";
        meta.account="Local";
        meta.pathSep= File.pathSeparator;
        String path = Environment.getDataDirectory().getPath();
        if (null!=path&&path.length()>0){
            StatFs statFs = new StatFs(path);
            long blockSize = statFs.getBlockSize();
            long totalBlocks = statFs.getBlockCount();
            long availableBlocks = statFs.getAvailableBlocks();
            long rom_length = totalBlocks*blockSize;
            meta.total=rom_length;
            meta.free=availableBlocks*blockSize;
        }
        return meta;
    }

    public boolean isDeviceType(String type){
        String deviceType=null!=type?this.deviceType:null;
        return null!=deviceType&&deviceType.equals(type);
    }

    public String getAccount() {
        return account;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getPlatform() {
        return platform;
    }

    public String getName() {
        return name;
    }

    public long getFree() {
        return free;
    }

    public long getTotal() {
        return total;
    }

    private Client(Parcel in){
        if (null!=in) {
            this.host = in.readString();
            this.account = in.readString();
            this.deviceType = in.readString();
            this.name = in.readString();
            this.imageUrl = in.readString();
            this.platform = in.readString();
            this.free = in.readLong();
            this.total = in.readLong();
            this.folder=in.readString();
            this.pathSep=in.readString();
        }
    }

    public String getHost() {
        return host;
    }

    public String getHome(){
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(host);
        dest.writeString(account);
        dest.writeString(deviceType);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(platform);
        dest.writeLong(free);
        dest.writeLong(total);
        dest.writeString(folder);
        dest.writeString(pathSep);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };


    public boolean isLocalClient() {
        return false;
    }
}