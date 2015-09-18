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

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table tb_last_data.
*/
public class DbLastDataVoDao extends AbstractDao<DbLastDataVo, String> {

    public static final String TABLENAME = "tb_last_data";

    /**
     * Properties of entity DbLastDataVo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property PlugId = new Property(0, String.class, "plugId", true, "PLUG_ID");
        public final static Property RecTime = new Property(1, java.util.Date.class, "recTime", false, "REC_TIME");
        public final static Property Wh = new Property(2, Float.class, "wh", false, "WH");
        public final static Property W = new Property(3, Float.class, "w", false, "W");
        public final static Property OnOff = new Property(4, String.class, "onOff", false, "ON_OFF");
        public final static Property LedOnOff = new Property(5, String.class, "ledOnOff", false, "LED_ON_OFF");
    }

    private DaoSession daoSession;


    public DbLastDataVoDao(DaoConfig config) {
        super(config);
    }
    
    public DbLastDataVoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'tb_last_data' (" + //
                "'PLUG_ID' TEXT PRIMARY KEY NOT NULL ," + // 0: plugId
                "'REC_TIME' INTEGER," + // 1: recTime
                "'WH' REAL," + // 2: wh
                "'W' REAL," + // 3: w
                "'ON_OFF' TEXT," + // 4: onOff
                "'LED_ON_OFF' TEXT);"); // 5: ledOnOff
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'tb_last_data'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbLastDataVo entity) {
        stmt.clearBindings();
 
        String plugId = entity.getPlugId();
        if (plugId != null) {
            stmt.bindString(1, plugId);
        }
 
        java.util.Date recTime = entity.getRecTime();
        if (recTime != null) {
            stmt.bindLong(2, recTime.getTime());
        }
 
        Float wh = entity.getWh();
        if (wh != null) {
            stmt.bindDouble(3, wh);
        }
 
        Float w = entity.getW();
        if (w != null) {
            stmt.bindDouble(4, w);
        }
 
        String onOff = entity.getOnOff();
        if (onOff != null) {
            stmt.bindString(5, onOff);
        }
 
        String ledOnOff = entity.getLedOnOff();
        if (ledOnOff != null) {
            stmt.bindString(6, ledOnOff);
        }
    }

    @Override
    protected void attachEntity(DbLastDataVo entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public DbLastDataVo readEntity(Cursor cursor, int offset) {
        DbLastDataVo entity = new DbLastDataVo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // plugId
            cursor.isNull(offset + 1) ? null : new java.util.Date(cursor.getLong(offset + 1)), // recTime
            cursor.isNull(offset + 2) ? null : cursor.getFloat(offset + 2), // wh
            cursor.isNull(offset + 3) ? null : cursor.getFloat(offset + 3), // w
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // onOff
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // ledOnOff
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbLastDataVo entity, int offset) {
        entity.setPlugId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setRecTime(cursor.isNull(offset + 1) ? null : new java.util.Date(cursor.getLong(offset + 1)));
        entity.setWh(cursor.isNull(offset + 2) ? null : cursor.getFloat(offset + 2));
        entity.setW(cursor.isNull(offset + 3) ? null : cursor.getFloat(offset + 3));
        entity.setOnOff(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setLedOnOff(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DbLastDataVo entity, long rowId) {
        return entity.getPlugId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DbLastDataVo entity) {
        if(entity != null) {
            return entity.getPlugId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getDbPlugVoDao().getAllColumns());
            builder.append(" FROM tb_last_data T");
            builder.append(" LEFT JOIN tb_plug T0 ON T.'PLUG_ID'=T0.'PLUG_ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected DbLastDataVo loadCurrentDeep(Cursor cursor, boolean lock) {
        DbLastDataVo entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        DbPlugVo dbPlugVo = loadCurrentOther(daoSession.getDbPlugVoDao(), cursor, offset);
        entity.setDbPlugVo(dbPlugVo);

        return entity;    
    }

    public DbLastDataVo loadDeep(Long key) {
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
    public List<DbLastDataVo> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<DbLastDataVo> list = new ArrayList<DbLastDataVo>(count);
        
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
    
    protected List<DbLastDataVo> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<DbLastDataVo> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
