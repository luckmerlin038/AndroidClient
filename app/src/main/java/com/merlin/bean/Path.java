package com.merlin.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.merlin.api.Reply;
import com.merlin.api.What;

import java.io.File;

public class Path implements Parcelable {
    private String parent;
    private String name;
    private String extension;
    private String host;

    public Path(){
        this(null,null,null);
    }

    public Path(String parent,String name,String extension){
        this(null,parent,name,extension);
    }

    public Path(String host,String parent,String name,String extension){
        this.host=host;
        this.parent=parent;
        this.name=name;
        this.extension=extension;
    }

    public final String getExtension() {
        return extension;
    }

    public final String getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    public final String getName(boolean extension) {
        return null!=name&&extension&&null!=this.extension?name+this.extension:name;
    }

    public final String getHost() {
        return host;
    }

    public final String getPath() {
        return getPath(null);
    }

    public final String getPath(String hostDivider) {
        String value=getName(true);
        String path=null!=parent&&null!=value?parent+value:null;
        String host=null!=hostDivider?this.host:null;
        return null!=host?host+(hostDivider.length()<=0?"/":hostDivider):path;
    }

    public final boolean applyPathChange(Reply<Path> reply){
        if (null!=reply&&reply.isSuccess()&&reply.getWhat()== What.WHAT_SUCCEED){
            Path path=reply.getData();
            return null!=path&&setPath(path.host,path.parent,path.name,path.extension);
        }
        return false;
    }

    protected final boolean setPath(String host,String parent,String name,String extension){
        this.host=host;
        this.parent=parent;
        this.name=name;
        this.extension=extension;
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (null!=obj&&obj instanceof Path){
            Path file=(Path)obj;
            return equals(file.parent,parent)&&equals(file.name,name)&&equals(file.extension,extension);
        }
        return super.equals(obj);
    }

    protected final boolean equals(String v1,String v2){
        return null!=v1&&null!=v2?v1.equals(v2):(null==v1&&null==v2);
    }

    public static <T extends Path> Path build(Object object,T result){
        Path path=null;
        if (null!=object){
            if (object instanceof File){
                File file=(File)object;
                String parent=file.getParent();
                parent=null!=parent?parent+ java.io.File.separator:null;
                String name=null!=file?file.getName():null;
                int index=null!=name?name.lastIndexOf("."):-1;
                String extension=index>0?name.substring(index):null;
                name=index>0?name.substring(0,index):name;
                if (null==result){
                    path=new Path(null,parent,name,extension);
                }else{
                    (path=result).setPath(null,parent,name,extension);
                }
            }
        }
        return path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parent);
        dest.writeString(name);
        dest.writeString(extension);
        dest.writeString(host);
    }

    protected Path(Parcel parcel){
        parent=parcel.readString();
        name=parcel.readString();
        extension=parcel.readString();
        host=parcel.readString();
    }

    public static final Creator<Path> CREATOR = new Creator<Path>() {
        @Override
        public Path createFromParcel(Parcel in) {
            return new Path(in);
        }

        @Override
        public Path[] newArray(int size) {
            return new Path[size];
        }
    };

}
