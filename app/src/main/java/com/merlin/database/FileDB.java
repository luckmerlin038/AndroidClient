package com.merlin.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class FileDB {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    @Unique
    private String path;
    @NotNull
    private String md5;
    private long createTime;
    private byte[] image;

    @Generated(hash = 2083376745)
    public FileDB(Long id, @NotNull String path, @NotNull String md5,
            long createTime, byte[] image) {
        this.id = id;
        this.path = path;
        this.md5 = md5;
        this.createTime = createTime;
        this.image = image;
    }
    @Generated(hash = 1254237660)
    public FileDB() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getMd5() {
        return this.md5;
    }
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public byte[] getImage() {
        return this.image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
