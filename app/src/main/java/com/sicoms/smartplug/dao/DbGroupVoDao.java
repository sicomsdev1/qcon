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
 * DAO for table tb_group.
*/
public class DbGroupVoDao extends AbstractDao<DbGroupVo, Long> {

    public static final String TABLENAME = "tb_group";

    /**
     * Properties of entity DbGroupVo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property PlaceId = new Property(0, String.class, "placeId", false, "PLACE_ID");
        public final static Property GroupId = new Property(1, Long.class, "groupId", true, "GROUP_ID");
        public final static Property GroupName = new Property(2, String.class, "groupName", false, "GROUP_NAME");
        public final static Property SuperId = new Property(3, String.class, "superId", false, "SUPER_ID");
        public final static Property GroupImg = new Property(4, String.class, "groupImg", false, "GROUP_IMG");
    }

    private DaoSession daoSession;

    private Query<DbGroupVo> dbPlaceVo_GroupListQuery;

    public DbGroupVoDao(DaoConfig config) {
        super(config);
    }
    
    public DbGroupVoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'tb_group' (" + //
                "'PLACE_ID' TEXT," + // 0: placeId
                "'GROUP_ID' INTEGER PRIMARY KEY ," + // 1: groupId
                "'GROUP_NAME' TEXT NOT NULL ," + // 2: groupName
                "'SUPER_ID' TEXT NOT NULL ," + // 3: superId
                "'GROUP_IMG' TEXT);"); // 4: groupImg
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_tb_group_PLACE_ID_GROUP_ID ON tb_group" +
                " (PLACE_ID,GROUP_ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'tb_group'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbGroupVo entity) {
        stmt.clearBindings();
 
        String placeId = entity.getPlaceId();
        if (placeId != null) {
            stmt.bindString(1, placeId);
        }
 
        Long groupId = entity.getGroupId();
        if (groupId != null) {
            stmt.bindLong(2, groupId);
        }
        stmt.bindString(3, entity.getGroupName());
        stmt.bindString(4, entity.getSuperId());
 
        String groupImg = entity.getGroupImg();
        if (groupImg != null) {
            stmt.bindString(5, groupImg);
        }
    }

    @Override
    protected void attachEntity(DbGroupVo entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }    

    /** @inheritdoc */
    @Override
    public DbGroupVo readEntity(Cursor cursor, int offset) {
        DbGroupVo entity = new DbGroupVo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // placeId
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // groupId
            cursor.getString(offset + 2), // groupName
            cursor.getString(offset + 3), // superId
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // groupImg
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbGroupVo entity, int offset) {
        entity.setPlaceId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setGroupId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setGroupName(cursor.getString(offset + 2));
        entity.setSuperId(cursor.getString(offset + 3));
        entity.setGroupImg(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(DbGroupVo entity, long rowId) {
        entity.setGroupId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(DbGroupVo entity) {
        if(entity != null) {
            return entity.getGroupId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "groupList" to-many relationship of DbPlaceVo. */
    public List<DbGroupVo> _queryDbPlaceVo_GroupList(String placeId) {
        synchronized (this) {
            if (dbPlaceVo_GroupListQuery == null) {
                QueryBuilder<DbGroupVo> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.PlaceId.eq(null));
                queryBuilder.orderRaw("GROUP_ID DESC");
                dbPlaceVo_GroupListQuery = queryBuilder.build();
            }
        }
        Query<DbGroupVo> query = dbPlaceVo_GroupListQuery.forCurrentThread();
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
            builder.append(" FROM tb_group T");
            builder.append(" LEFT JOIN tb_place T0 ON T.'PLACE_ID'=T0.'PLACE_ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected DbGroupVo loadCurrentDeep(Cursor cursor, boolean lock) {
        DbGroupVo entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        DbPlaceVo dbPlaceVo = loadCurrentOther(daoSession.getDbPlaceVoDao(), cursor, offset);
        entity.setDbPlaceVo(dbPlaceVo);

        return entity;    
    }

    public DbGroupVo loadDeep(Long key) {
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
    public List<DbGroupVo> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<DbGroupVo> list = new ArrayList<DbGroupVo>(count);
        
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
    
    protected List<DbGroupVo> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<DbGroupVo> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
