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
 * DAO for table tb_router.
*/
public class DbRouterVoDao extends AbstractDao<DbRouterVo, String> {

    public static final String TABLENAME = "tb_router";

    /**
     * Properties of entity DbRouterVo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property RouterIp = new Property(0, String.class, "routerIp", true, "ROUTER_IP");
        public final static Property SsId = new Property(1, String.class, "ssId", false, "SS_ID");
        public final static Property Password = new Property(2, String.class, "password", false, "PASSWORD");
    }

    private DaoSession daoSession;


    public DbRouterVoDao(DaoConfig config) {
        super(config);
    }
    
    public DbRouterVoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'tb_router' (" + //
                "'ROUTER_IP' TEXT PRIMARY KEY NOT NULL ," + // 0: routerIp
                "'SS_ID' TEXT NOT NULL ," + // 1: ssId
                "'PASSWORD' TEXT NOT NULL );"); // 2: password
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'tb_router'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbRouterVo entity) {
        stmt.clearBindings();
 
        String routerIp = entity.getRouterIp();
        if (routerIp != null) {
            stmt.bindString(1, routerIp);
        }
        stmt.bindString(2, entity.getSsId());
        stmt.bindString(3, entity.getPassword());
    }

    @Override
    protected void attachEntity(DbRouterVo entity) {
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
    public DbRouterVo readEntity(Cursor cursor, int offset) {
        DbRouterVo entity = new DbRouterVo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // routerIp
            cursor.getString(offset + 1), // ssId
            cursor.getString(offset + 2) // password
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbRouterVo entity, int offset) {
        entity.setRouterIp(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setSsId(cursor.getString(offset + 1));
        entity.setPassword(cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(DbRouterVo entity, long rowId) {
        return entity.getRouterIp();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(DbRouterVo entity) {
        if(entity != null) {
            return entity.getRouterIp();
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
            builder.append(" FROM tb_router T");
            builder.append(" LEFT JOIN tb_plug T0 ON T.'ROUTER_IP'=T0.'PLUG_ID'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected DbRouterVo loadCurrentDeep(Cursor cursor, boolean lock) {
        DbRouterVo entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        DbPlugVo dbPlugVo = loadCurrentOther(daoSession.getDbPlugVoDao(), cursor, offset);
        entity.setDbPlugVo(dbPlugVo);

        return entity;    
    }

    public DbRouterVo loadDeep(Long key) {
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
    public List<DbRouterVo> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<DbRouterVo> list = new ArrayList<DbRouterVo>(count);
        
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
    
    protected List<DbRouterVo> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<DbRouterVo> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}