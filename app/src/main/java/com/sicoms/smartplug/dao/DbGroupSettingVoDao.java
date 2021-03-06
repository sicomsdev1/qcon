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
 * DAO for table tb_group_setting.
*/
public class DbGroupSettingVoDao extends AbstractDao<DbGroupSettingVo, String> {

    public static final String TABLENAME = "tb_group_setting";

    /**
     * Properties of entity DbGroupSettingVo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property GroupId = new Property(0, Long.class, "groupId", false, "GROUP_ID");
        public final static Property SetId = new Property(1, String.class, "setId", true, "SET_ID");
        public final static Property SetVal = new Property(2, String.class, "setVal", false, "SET_VAL");
    }

    private DaoSession daoSession;

    private Query<DbGroupSettingVo> dbGroupVo_GroupSettingListQuery;

    public DbGroupSettingVoDao(DaoConfig config) {
        super(config);
    }
    
    public DbGroupSettingVoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'tb_group_setting' (" + //
                "'GROUP_ID' INTEGER," + // 0: groupId
                "'SET_ID' TEXT PRIMARY KEY NOT NULL ," + // 1: setId
                "'SET_VAL' TEXT NOT NULL );"); // 2: setVal
        // Add Indexes
        db.execSQL("CREATE UNIQUE INDEX " + constraint + "IDX_tb_group_setting_GROUP_ID_SET_ID ON tb_group_setting" +
                " (GROUP_ID,SET_ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'tb_group_setting'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbGroupSettingVo entity) {
        stmt.clearBindings();
 
        Long groupId = entity.getGroupId();
        if (groupId != null) {
            stmt.bindLong(1, groupId);
        }
 
        String setId = entity.getSetId();
        if (setId != null) {
            stmt.bindString(2, setId);
        }
        stmt.bindString(3, entity.getSetVal());
    }

    @Override
    protected void attachEntity(DbGroupSettingVo entity) {
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
    public DbGroupSettingVo readEntity(Cursor cursor, int offset) {
        DbGroupSettingVo entity = new DbGroupSettingVo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // groupId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // setId
            cursor.getString(offset + 2) // setVal
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbGroupSettingVo entity, int offset) {
        entity.setGroupId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setSetId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSetVal(cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DbGroupSettingVo entity, long rowId) {
        return entity.getSetId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DbGroupSettingVo entity) {
        if(entity != null) {
            return entity.getSetId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "groupSettingList" to-many relationship of DbGroupVo. */
    public List<DbGroupSettingVo> _queryDbGroupVo_GroupSettingList(Long groupId) {
        synchronized (this) {
            if (dbGroupVo_GroupSettingListQuery == null) {
                QueryBuilder<DbGroupSettingVo> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.GroupId.eq(null));
                dbGroupVo_GroupSettingListQuery = queryBuilder.build();
            }
        }
        Query<DbGroupSettingVo> query = dbGroupVo_GroupSettingListQuery.forCurrentThread();
        query.setParameter(0, groupId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getDbGroupVoDao().getAllColumns());
            builder.append(" FROM tb_group_setting T");
            builder.append(" LEFT JOIN tb_group T0 ON T.'GROUP_ID'=T0.'GROUP_ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected DbGroupSettingVo loadCurrentDeep(Cursor cursor, boolean lock) {
        DbGroupSettingVo entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        DbGroupVo dbGroupVo = loadCurrentOther(daoSession.getDbGroupVoDao(), cursor, offset);
        entity.setDbGroupVo(dbGroupVo);

        return entity;    
    }

    public DbGroupSettingVo loadDeep(Long key) {
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
    public List<DbGroupSettingVo> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<DbGroupSettingVo> list = new ArrayList<DbGroupSettingVo>(count);
        
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
    
    protected List<DbGroupSettingVo> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<DbGroupSettingVo> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
