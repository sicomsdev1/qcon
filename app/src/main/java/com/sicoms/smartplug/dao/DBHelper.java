package com.sicoms.smartplug.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by wizardkyn on 2015. 3. 12..
 */
public class DBHelper {
    private SQLiteDatabase db = null;
    private DaoSession session = null;
    // private final String DB_NAME = Environment.getExternalStorageDirectory() + "/" + "smartplug";
    private final String DB_NAME = "smartplug";
    private Context mContext;

    private static final String TAG = DBHelper.class.getSimpleName();

    private DaoMaster getMaster(boolean readOnly) {
        if (db == null) {
            db = getDatabase(DB_NAME, readOnly);
        }
        return new DaoMaster(db);
    }

    public DBHelper(Context mContext) {
        this.mContext = mContext;
    }

    public DaoSession getSession(boolean readOnly) {
        if (session == null) {
            session = getMaster(readOnly).newSession();
        }
        return session;
    }
    public void reCreateAllTables() {
        getMaster(false).dropAllTables(db,true);
        getMaster(false).createAllTables(db,true);
    }
    // TODO : 개발 완료 후 삭제할 메소드
    public void insertTestData() {
        getMaster(false).getDatabase().execSQL("delete from tb_group");
        getMaster(false).getDatabase().execSQL("INSERT INTO `tb_group` ('GROUP_SEQ','GROUP_NAME') VALUES (1,'거실'),(2,'주방'),(3,'베란다');");
        getMaster(false).getDatabase().execSQL("delete from tb_plug");
        getMaster(false).getDatabase().execSQL("INSERT INTO `tb_plug` ('PLUG_ID','GROUP_SEQ','PLUG_NAME','PLUG_TYPE','ICON_NO','BSSID','GATEWAY_ID', 'PLUG_IP') VALUES " +
                "('B3E76',1,'Plug #1','B_A_S',1,'','','192.168.8.253')," +
                "('ND080',0,'BL Plug','C',2,'','G1','')," +
                "('ND100',1,'Plug #3','B_G',3,'','G1','');");
        getMaster(false).getDatabase().execSQL("delete from tb_gateway");
        getMaster(false).getDatabase().execSQL("INSERT INTO `tb_gateway` ('GATEWAY_ID','IP_ADDRESS') VALUES ('G1','192.168.8.253');");
    }
    public void closeSession() {
        if (session != null) {
            session.clear();
            session = null;
        }
        if (db != null) {
            db.close();
            db = null;
        }
        Log.d(TAG, "DB Closed");
    }

    private synchronized SQLiteDatabase getDatabase(String name, boolean readOnly) {
        try {
            SQLiteOpenHelper helper = new MyOpenHelper(mContext, name, null);
            if (readOnly) {
                Log.d(TAG, "getDB(" + name + ",readonly=true)");
                return helper.getReadableDatabase();
            } else {
                Log.d(TAG, "getDB("+name+",readonly=false)");
                return helper.getWritableDatabase();
            }
        } catch (Exception ex) {
            if (readOnly) {
                Log.e(TAG, "getDB(" + name + ",readonly=true)", ex);
            } else {
                Log.e(TAG, "getDB("+name+",readonly=false)", ex);
            }
            return null;
        } catch (Error err) {
            if (readOnly) {
                Log.e(TAG, "getDB("+name+",readonly=true)", err);
            } else {
                Log.e(TAG, "getDB("+name+",readonly=false)", err);
            }
            return null;
        }
    }

    private class MyOpenHelper extends DaoMaster.OpenHelper {
        public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name , factory);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "Create DB-Schema (version "+Integer.toString(DaoMaster.SCHEMA_VERSION)+")");
            super.onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Update DB-Schema to version: "+Integer.toString(oldVersion)+"->"+Integer.toString(newVersion));
            switch (oldVersion) {
                case 1:
//                    db.execSQL(SQL_UPGRADE_1To2);
                case 2:
//                    db.execSQL(SQL_UPGRADE_2To3);
                    break;
                default:
                    break;
            }
        }
    }
}
