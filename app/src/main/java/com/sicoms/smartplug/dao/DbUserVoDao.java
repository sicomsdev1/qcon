package com.sicoms.smartplug.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table tb_user.
*/
public class DbUserVoDao extends AbstractDao<DbUserVo, String> {

    public static final String TABLENAME = "tb_user";

    /**
     * Properties of entity DbUserVo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property PlaceId = new Property(0, String.class, "placeId", false, "PLACE_ID");
        public final static Property UserId = new Property(1, String.class, "userId", true, "USER_ID");
        public final static Property UserName = new Property(2, String.class, "userName", false, "USER_NAME");
        public final static Property ProfileImg = new Property(3, String.class, "profileImg", false, "PROFILE_IMG");
        public final static Property Auth = new Property(4, int.class, "auth", false, "AUTH");
    }

    private DaoSession daoSession;

    private Query<DbUserVo> dbPlaceVo_UserListQuery;

    public DbUserVoDao(DaoConfig config) {
        super(config);
    }
    
    public DbUserVoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'tb_user' (" + //
                "'PLACE_ID' TEXT," + // 0: placeId
                "'USER_ID' TEXT PRIMARY KEY NOT NULL ," + // 1: userId
                "'USER_NAME' TEXT NOT NULL ," + // 2: userName
                "'PROFILE_IMG' TEXT," + // 3: profileImg
                "'AUTH' INTEGER NOT NULL );"); // 4: auth
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_tb_user_PLACE_ID_USER_ID ON tb_user" +
                " (PLACE_ID,USER_ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'tb_user'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbUserVo entity) {
        stmt.clearBindings();
 
        String placeId = entity.getPlaceId();
        if (placeId != null) {
            stmt.bindString(1, placeId);
        }
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(2, userId);
        }
        stmt.bindString(3, entity.getUserName());
 
        String profileImg = entity.getProfileImg();
        if (profileImg != null) {
            stmt.bindString(4, profileImg);
        }
        stmt.bindLong(5, entity.getAuth());
    }

    @Override
    protected void attachEntity(DbUserVo entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
    }    

    /** @inheritdoc */
    @Override
    public DbUserVo readEntity(Cursor cursor, int offset) {
        DbUserVo entity = new DbUserVo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // placeId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // userId
            cursor.getString(offset + 2), // userName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // profileImg
            cursor.getInt(offset + 4) // auth
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbUserVo entity, int offset) {
        entity.setPlaceId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setUserId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUserName(cursor.getString(offset + 2));
        entity.setProfileImg(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAuth(cursor.getInt(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DbUserVo entity, long rowId) {
        return entity.getUserId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DbUserVo entity) {
        if(entity != null) {
            return entity.getUserId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "userList" to-many relationship of DbPlaceVo. */
    public List<DbUserVo> _queryDbPlaceVo_UserList(String placeId) {
        synchronized (this) {
            if (dbPlaceVo_UserListQuery == null) {
                QueryBuilder<DbUserVo> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.PlaceId.eq(null));
                dbPlaceVo_UserListQuery = queryBuilder.build();
            }
        }
        Query<DbUserVo> query = dbPlaceVo_UserListQuery.forCurrentThread();
        query.setParameter(0, placeId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getDbPlaceVoDao().getAllColumns());
            builder.append(" FROM tb_user T");
            builder.append(" LEFT JOIN tb_place T0 ON T.'PLACE_ID'=T0.'PLACE_ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected DbUserVo loadCurrentDeep(Cursor cursor, boolean lock) {
        DbUserVo entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        DbPlaceVo dbPlaceVo = loadCurrentOther(daoSession.getDbPlaceVoDao(), cursor, offset);
        entity.setDbPlaceVo(dbPlaceVo);

        return entity;    
    }

    public DbUserVo loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<DbUserVo> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<DbUserVo> list = new ArrayList<DbUserVo>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<DbUserVo> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<DbUserVo> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}