package com.sicoms.smartplug.plug.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbGatewayVo;
import com.sicoms.smartplug.dao.DbGatewayVoDao;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbLastDataVoDao;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbRouterVo;
import com.sicoms.smartplug.dao.DbRouterVoDao;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.menu.service.PlaceService;

import java.util.Date;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class PlugDBService {
    private static final String TAG = PlugDBService.class.getSimpleName();

    private Context mContext;
    private DBHelper mDBHelper;

    public PlugDBService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }

    /*
     * Local DB Method
     */

    // Update Plug
    public boolean updateDbDevice(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao dbPlugVoDao = daoSession.getDbPlugVoDao();

            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DbPlugVo dbPlugVo = new DbPlugVo(
                    placeVo.getPlaceId(), plugVo.getPlugId(), plugVo.getPlugName(), plugVo.getNetworkType()
                    , plugVo.getPlugIconImg(), plugVo.getBssid(), plugVo.getRouterIp(), plugVo.getGatewayIp(), plugVo.getUuid());

            dbPlugVoDao.insertOrReplace(dbPlugVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbDevice(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao dbPlugVoDao = daoSession.getDbPlugVoDao();

            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DbPlugVo dbPlugVo = dbPlugVoDao.queryBuilder().where(DbPlugVoDao.Properties.PlaceId.eq(placeVo.getPlaceId()))
                                                        .where(DbPlugVoDao.Properties.PlugId.eq(plugVo.getPlugId()))
                                                        .unique();

            dbPlugVoDao.delete(dbPlugVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean updateDbLastStatusData(String plugId, String onoff){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo theDbVo = theDao.queryBuilder().where(DbLastDataVoDao.Properties.PlugId.eq(plugId)).unique();
            if( theDbVo == null){
                theDbVo = new DbLastDataVo(plugId, new Date(), 0.0f, 0.0f, onoff, SPConfig.STATUS_ON);
            }
            theDbVo.setOnOff(onoff);
            theDao.update(theDbVo);
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean updateDbLastStatusDataList(List<DbLastDataVo> dbLastDataVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            theDao.insertOrReplaceInTx(dbLastDataVoList);
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public DbRouterVo selectDbRouterData(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbRouterVoDao theDao = daoSession.getDbRouterVoDao();
            DbRouterVo theDbVo = theDao.queryBuilder().where(DbRouterVoDao.Properties.RouterIp.eq(plugVo.getRouterIp())).unique();

            mDBHelper.closeSession();
            return theDbVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public DbGatewayVo selectDbGatewayData(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbGatewayVoDao theDao = daoSession.getDbGatewayVoDao();
            DbGatewayVo theDbVo = theDao.queryBuilder().where(DbGatewayVoDao.Properties.GatewayIp.eq(plugVo.getGatewayIp())).unique();

            mDBHelper.closeSession();
            return theDbVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public DbLastDataVo selectDbLastData(String plugId){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo dbLastDataVo = theDao.queryBuilder().where(DbPlugVoDao.Properties.PlugId.eq(plugId)).unique();

            return dbLastDataVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
