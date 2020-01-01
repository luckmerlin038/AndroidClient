package com.merlin.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.merlin.media.Media;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MEDIA".
*/
public class MediaDao extends AbstractDao<Media, Long> {

    public static final String TABLENAME = "MEDIA";

    /**
     * Properties of entity Media.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "id");
        public final static Property Title = new Property(1, String.class, "title", false, "title");
        public final static Property Path = new Property(2, String.class, "path", false, "path");
        public final static Property Md5 = new Property(3, String.class, "md5", false, "md5");
        public final static Property CloudUrl = new Property(4, String.class, "cloudUrl", false, "cloudUrl");
        public final static Property Account = new Property(5, String.class, "account", false, "account");
        public final static Property Album = new Property(6, String.class, "album", false, "album");
        public final static Property Artist = new Property(7, String.class, "artist", false, "artist");
        public final static Property Duration = new Property(8, long.class, "duration", false, "duration");
    }


    public MediaDao(DaoConfig config) {
        super(config);
    }
    
    public MediaDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MEDIA\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"title\" TEXT NOT NULL ," + // 1: title
                "\"path\" TEXT NOT NULL ," + // 2: path
                "\"md5\" TEXT," + // 3: md5
                "\"cloudUrl\" TEXT," + // 4: cloudUrl
                "\"account\" TEXT," + // 5: account
                "\"album\" TEXT," + // 6: album
                "\"artist\" TEXT," + // 7: artist
                "\"duration\" INTEGER NOT NULL );"); // 8: duration
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_MEDIA_path ON \"MEDIA\"" +
                " (\"path\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MEDIA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Media entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindString(2, entity.getTitle());
        stmt.bindString(3, entity.getPath());
 
        String md5 = entity.getMd5();
        if (md5 != null) {
            stmt.bindString(4, md5);
        }
 
        String cloudUrl = entity.getCloudUrl();
        if (cloudUrl != null) {
            stmt.bindString(5, cloudUrl);
        }
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(6, account);
        }
 
        String album = entity.getAlbum();
        if (album != null) {
            stmt.bindString(7, album);
        }
 
        String artist = entity.getArtist();
        if (artist != null) {
            stmt.bindString(8, artist);
        }
        stmt.bindLong(9, entity.getDuration());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Media entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindString(2, entity.getTitle());
        stmt.bindString(3, entity.getPath());
 
        String md5 = entity.getMd5();
        if (md5 != null) {
            stmt.bindString(4, md5);
        }
 
        String cloudUrl = entity.getCloudUrl();
        if (cloudUrl != null) {
            stmt.bindString(5, cloudUrl);
        }
 
        String account = entity.getAccount();
        if (account != null) {
            stmt.bindString(6, account);
        }
 
        String album = entity.getAlbum();
        if (album != null) {
            stmt.bindString(7, album);
        }
 
        String artist = entity.getArtist();
        if (artist != null) {
            stmt.bindString(8, artist);
        }
        stmt.bindLong(9, entity.getDuration());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public Media readEntity(Cursor cursor, int offset) {
        Media entity = new Media( //
            cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // title
            cursor.getString(offset + 2), // path
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // md5
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // cloudUrl
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // account
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // album
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // artist
            cursor.getLong(offset + 8) // duration
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Media entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setTitle(cursor.getString(offset + 1));
        entity.setPath(cursor.getString(offset + 2));
        entity.setMd5(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setCloudUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAccount(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAlbum(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setArtist(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setDuration(cursor.getLong(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Media entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Media entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Media entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}