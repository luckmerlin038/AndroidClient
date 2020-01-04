package com.merlin.media;

import android.os.Parcel;
import android.os.Parcelable;


public final class Sheet implements Parcelable {
    /**
     * name : 我最喜欢的歌单
     * size : 123
     * cover : null
     * id : 1231321
     * create : 2342
     */
    private String account;
    private String name;
    private long size;
    private String cover;
    private String id;
    private String note;
    private long create;

    public Sheet(){
        this(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreate() {
        return create;
    }

    public void setCreate(long create) {
        this.create = create;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    private Sheet(Parcel in){
        if (null!=in){
            name=in.readString();
            size=in.readLong();
            cover=in.readString();
            id=in.readString();
            create=in.readLong();
            account=in.readString();
            note=in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(cover);
        dest.writeString(id);
        dest.writeLong(create);
        dest.writeString(account);
        dest.writeString(note);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Sheet> CREATOR = new Parcelable.Creator<Sheet>(){

        @Override
        public Sheet createFromParcel(Parcel source) {
            return new Sheet(source);
        }

        @Override
        public Sheet[] newArray(int size) {
            return new Sheet[size];
        }

    };

}
