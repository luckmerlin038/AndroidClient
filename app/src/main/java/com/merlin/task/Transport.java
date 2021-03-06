package com.merlin.task;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.merlin.util.FileSize;

import java.io.File;

public final class Transport implements Parcelable , IStatus {
    private final static int MASK_DOWNLOAD=0x01;//0000 0001
    private final static int MASK_COVER_REPLACE=0x02;//0000 0010
    private final static int MASK_COVER_KEEP_ALL=0x04;//0000 0100
    private final static int MASK_COVER_SKIP=0x06;//0000 0110
    private final static int MASK_BREAKPOINT=0x08;//0000 1000
    private String mFromAccount;
    private String mSrc;
    private String mTargetFolder;
    private String mUnique;
    private String mTargetName;
    private int mType;
    private boolean mDeleteIncomplete;
    private long mTotal;
    private long mRemain;
    private int mStatus=UNKNOWN;
    private String mMD5;
    private String mFormat;

    private Transport(Parcel in){
        mSrc= in.readString();
        mTargetFolder = in.readString();
        mUnique=in.readString();
        mTargetName=in.readString();
        mType=in.readInt();
        mFromAccount=in.readString();
        mDeleteIncomplete=in.readByte()==1;
        mTotal=in.readLong();
        mRemain=in.readLong();
        mStatus=in.readInt();
        mMD5=in.readString();
    }

    public Transport(String fromAccount, String src, String name, String targetFolder, String unique){
        mFromAccount=fromAccount;
        mSrc=src;
        mTargetName=name;
        mTargetFolder=targetFolder;
        mUnique=unique;
    }

    public void setDeleteIncomplete(boolean deleteIncomplete) {
        this.mDeleteIncomplete = deleteIncomplete;
    }

    public boolean isDeleteIncomplete() {
        return mDeleteIncomplete;
    }

    public String getTargetName() {
        return mTargetName;
    }

    public void setTargetName(String name) {
        this.mTargetName = name;
    }

    public String getTargetPath(){
        String folder=mTargetFolder;
        String name=mTargetName;
        return (null!=folder?folder:"")+(null!=name?name:"");
    }

    public String getSrc() {
        return mSrc;
    }

    public String getTargetFolder() {
        return mTargetFolder;
    }

    public String getUnique() {
        return mUnique;
    }

    public String getFromAccount() {
        return mFromAccount;
    }

    public String getFormat() {
        return mFormat;
    }

    public boolean isTypeMaskExist(int mask){
        return (mask&mType)!=0;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    protected void setStatus(int status) {
        this.mStatus = status;
    }

    protected void setTotal(long total) {
        this.mTotal = total;
    }

    protected void setRemain(long remain) {
        this.mRemain = remain;
    }

    protected void setFormat(String format) {
        this.mFormat = format;
    }

    protected void setMD5(String md5) {
        this.mMD5 = md5;
    }

    protected void setSrcPath(String src){
        mSrc=src;
    }

    public int getStatus() {
        return mStatus;
    }

    public long getRemain() {
        return mRemain;
    }

    public long getTotal() {
        return mTotal;
    }

    public String getMD5() {
        return mMD5;
    }

    public void setFromAccount(String fromAccount) {
        this.mFromAccount = fromAccount;
    }

    public int getProgressInt(){
        return (int)getProgress();
    }

    public float getProgress(){
        long remain=mRemain;
        long total=mTotal;
        return remain>=0&&remain<=total&&total>0?((total-remain)*100/(float)total):0;
    }

    public boolean buildRemainFromFile(){
        String folder=mTargetFolder;
        String name=mTargetName;
        if (null!=folder&&null!=name&&folder.length()>0&&name.length()>0){
            long total=mTotal;
            long size=new File(folder,name).length();
            if (total>=size){
                mRemain=total-size;
            }
            return false;
        }
        return false;
    }

    public long getDownloadedSize(){
        long remain=mRemain;
        long total=mTotal;
        return remain>=0&&total>=0&&remain<=total?total-remain:0;
    }

    public String getDownloadSizeText(){
        return FileSize.formatSizeText(getDownloadedSize());
    }

    public String getTotalSizeText(){
        long total=mTotal;
        return FileSize.formatSizeText(total>0?total:0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSrc);
        dest.writeString(mTargetFolder);
        dest.writeString(mUnique);
        dest.writeString(mTargetName);
        dest.writeInt(mType);
        dest.writeString(mFromAccount);
        dest.writeByte((byte)(mDeleteIncomplete?1:0));
        dest.writeLong(mTotal);
        dest.writeLong(mRemain);
        dest.writeInt(mStatus);
        dest.writeString(mMD5);
    }

    public static final Parcelable.Creator<Transport> CREATOR = new Parcelable.Creator<Transport>(){

        @Override
        public Transport createFromParcel(Parcel source) {
            return new Transport(source);
        }

        @Override
        public Transport[] newArray(int size) {
            return new Transport[size];
        }

    };

    @Override
    public String toString() {
        return "Transport{" +
                "mSrc='" + mSrc + '\'' +
                ", mTarget='" + mTargetFolder + '\'' +
                ", mUnique='" + mUnique + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null!=obj) {
            if (obj instanceof Transport) {
                Transport d = (Transport) obj;
                return equal(mFromAccount, d.mFromAccount) && equal(mTargetName, d.mTargetName) && equal(mSrc, d.mSrc)
                        && equal(mTargetFolder, d.mTargetFolder);
            }
        }
        return super.equals(obj);
    }

    private boolean equal(String val,String val2){
        if (null==val&&null==val2){
            return true;
        }
        return null!=val&&null!=val2&&val.equals(val2);
    }
}
