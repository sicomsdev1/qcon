package com.sicoms.smartplug.plug.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DbApVoDao;
import com.sicoms.smartplug.dao.DbBluetoothVoDao;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbApVo;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbLastDataVoDao;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.domain.BluetoothVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 12..
 */
public class PlugAllService {
    private static final String TAG = PlugAllService.class.getSimpleName();

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public PlugAllService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
    /*
     * Cloud Server Method
     */
    // 플러그 리스트 삭제
    public void requestDeletePlugList(List<PlugVo> voList){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_DELETE_PLUG;

        try{
            if( voList.size() < 1){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(voList);
            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_DELETE_PLUG, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 플러그 리스트 가져오기
    public void requestSelectPlugList(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_PLUG_LIST;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            RequestParams params = new RequestParams();
            params.add("placeId", placeVo.getPlaceId());
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_PLUG_LIST, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 블루투스 리스트 가져오기
    public void requestGetBluetoothList(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_BLUETOOTH_LIST;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            RequestParams params = new RequestParams();
            params.add("placeId", placeVo.getPlaceId());
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_BLUETOOTH_LIST, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
     * Local DB Method
     */
    public List<PlugVo> selectDbPlugList(){
        List<PlugVo> plugVoList = new ArrayList<>();
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao dbPlugVoDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> dbPlugVoList = dbPlugVoDao.queryBuilder()
                    .where(DbPlugVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();

            if (dbPlugVoList != null) {
                int offset = 0;
                for (DbPlugVo vo : dbPlugVoList) {
                    PlugVo plugVo = new PlugVo(vo.getPlugName(), vo.getPlugId(), vo.getPlugType(), vo.getPlugImg(), false);
                    String type = plugVo.getNetworkType();
                    if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
                        plugVo.setUuid(vo.getUuid());
                    } else if(type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
                        plugVo.setRouterIp(vo.getRouterIp());
                    } else if(type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)){
                        plugVo.setGatewayIp(vo.getGatewayIp());
                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                        plugVo.setBssid(vo.getBssId());
                    }
                    plugVoList.add(plugVo);
                }

                mDBHelper.closeSession();
                return plugVoList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean insertDbPlugList(List<PlugVo> plugVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao theDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> theDbVoList = new ArrayList<>();
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            for(PlugVo plugVo : plugVoList){
                DbPlugVo theDbVo = new DbPlugVo(
                        placeVo.getPlaceId(), plugVo.getPlugId(), plugVo.getPlugName(), plugVo.getNetworkType(),
                        plugVo.getPlugIconImg(), plugVo.getBssid(), plugVo.getRouterIp(), plugVo.getGatewayIp(), plugVo.getUuid());
                theDbVoList.add(theDbVo);
            }

            theDao.insertOrReplaceInTx(theDbVoList);

            mDBHelper.closeSession();
            return true;
        }catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public DbLastDataVo selectDbLastData(PlugVo plugVo){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbLastDataVoDao dbLastDataVoDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo dbLastDataVo = dbLastDataVoDao.queryBuilder()
                    .where(DbLastDataVoDao.Properties.PlugId.eq(plugVo.getPlugId())).unique();

            mDBHelper.closeSession();
            return dbLastDataVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    private DbBluetoothVo selectDbBluetooth(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbBluetoothVoDao theDao = daoSession.getDbBluetoothVoDao();
            DbBluetoothVo dbBluetoothVo = theDao.queryBuilder().where(DbBluetoothVoDao.Properties.Uuid.eq(Integer.parseInt(plugVo.getUuid()))).unique();

            return dbBluetoothVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();

        return null;
    }

    public DbApVo selectDbAp(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbApVoDao theDao = daoSession.getDbApVoDao();
            DbApVo dbApVo = theDao.queryBuilder().where(DbApVoDao.Properties.BssId.eq(plugVo.getBssid())).unique();

            return dbApVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();

        return null;
    }

    public boolean updateDbLastData(DbLastDataVo dbLastDataVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbLastDataVoDao dbLastDataVoDao = daoSession.getDbLastDataVoDao();

            dbLastDataVoDao.insertOrReplace(dbLastDataVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbPlugList(List<PlugVo> plugVoList){
        if( plugVoList.size() < 1)
            return false;
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao dbPlugVoDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> dbPlugVoList = new ArrayList<>();
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            for(PlugVo vo : plugVoList){
                if( !deleteDbLastData(daoSession, vo)){
                    return false;
                }
                dbPlugVoList.add(new DbPlugVo(
                        placeVo.getPlaceId(), vo.getPlugId(), vo.getPlugName(), vo.getNetworkType(),
                        vo.getPlugIconImg(), vo.getBssid(), vo.getRouterIp(), vo.getGatewayIp(), vo.getUuid()));
            }

            dbPlugVoDao.deleteInTx(dbPlugVoList);
            mDBHelper.closeSession();
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbPlugAllInPlace(){
        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao theDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> theDbVoList = theDao.queryBuilder().where(DbPlugVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();

            theDao.deleteInTx(theDbVoList);
            mDBHelper.closeSession();
            return true;
        }catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbLastData(DaoSession daoSession, PlugVo plugVo){
        if( plugVo == null)
            return false;
        try{
            DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo theDbVo = theDao.queryBuilder().where(DbLastDataVoDao.Properties.PlugId.eq(plugVo.getPlugId())).unique();
            if( theDbVo != null) {
                theDao.delete(theDbVo);
            }
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDbBluetoothList(List<DbBluetoothVo> dbBluetoothVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbBluetoothVoDao dbLastDataVoDao = daoSession.getDbBluetoothVoDao();

            dbLastDataVoDao.insertOrReplaceInTx(dbBluetoothVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    /*
     * Bluetooth Device Method
     */
    public void requestGetBLLastData(List<PlugVo> plugVoList){
        for( PlugVo plugVo : plugVoList) {
            String deviceStringId = String.format("%x", Integer.parseInt(plugVo.getUuid()));
            String unixTime = String.format("%x", System.currentTimeMillis() / 1000);
            String requestMessage = BLConfig.DATA_REQUEST_NUM + deviceStringId + unixTime;
            Log.d(TAG, "Request BL Last Data (message : " + requestMessage + ")");
            if( MainActivity.stBluetoothManager.isConnected()) {
                MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
            }
            SPUtil.sleep(5000);
        }
    }
    public void deleteAssociatedDevice(PlugVo plugVo){
        String requestMessage = BLMessage.getAssociationRequest(MainActivity.stBluetoothManager, Integer.parseInt(plugVo.getUuid()));
        MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
    }

    /*
     * Business Logic
     */
    public boolean isEqualBluetoothPassword(PlugVo plugVo){
        //String placeBLPassword = MainActivity.stBluetoothManager.getNetworkKeyPhrase();
        DbBluetoothVo dbBluetoothVo = selectDbBluetooth(plugVo);
        if( dbBluetoothVo == null){
//            PlaceSettingService placeSettingService = new PlaceSettingService(mContext);
//            PlaceSettingVo settingVo = placeSettingService.selectDbBLPassword();
//            if( settingVo == null){
//                SPUtil.showToast(mContext, "현재 플레이스에 블루투스 비밀번호가 저장되어있지 않습니다.");
//                return false;
//            }
//            dbBluetoothVo = new DbBluetoothVo(plugVo.getUuid(), settingVo.getSetVal());

            return false;
        }
        String bluetoothPassword = dbBluetoothVo.getPassword();

        return SPConfig.CURRENT_PLACE_BL_PASSWORD.equalsIgnoreCase(bluetoothPassword);
    }
}
