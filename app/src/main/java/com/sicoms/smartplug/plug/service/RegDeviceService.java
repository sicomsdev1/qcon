package com.sicoms.smartplug.plug.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DbApVoDao;
import com.sicoms.smartplug.dao.DbBluetoothVoDao;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbApVo;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.dao.DbGatewayVo;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbRouterVo;
import com.sicoms.smartplug.dao.DbGatewayVoDao;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbRouterVoDao;
import com.sicoms.smartplug.domain.CommonDataVo;
import com.sicoms.smartplug.domain.NodeListRequestVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.bluetooth.util.DeviceController;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 10..
 */
public class RegDeviceService {

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public RegDeviceService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    // 플러그 리스트 추가
    public void requestInsertDevice(List<RegDeviceVo> voList){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_PLUG;

        try{
            if( voList.size() < 1){
                return;
            }
            List<PlugVo> plugVoList = new ArrayList<>();
            for(RegDeviceVo vo : voList){
                String type = vo.getNetworkType();
                if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
                    PlugVo plugVo = new PlugVo(vo.getPlugId(), vo.getPlugId(), vo.getNetworkType(), SPConfig.PLUG_DEFAULT_IMAGE_NAME + "_00", false);
                    plugVo.setRouterIp(vo.getIpAddress());
                    plugVoList.add(plugVo);
                } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)){
                    PlugVo plugVo = new PlugVo(vo.getPlugId(), vo.getPlugId(), vo.getNetworkType(), SPConfig.PLUG_DEFAULT_IMAGE_NAME + "_00", false);
                    plugVo.setGatewayIp(vo.getIpAddress());
                    plugVoList.add(plugVo);
                } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                    PlugVo plugVo = new PlugVo(vo.getPlugId(), vo.getPlugId(), vo.getNetworkType(), SPConfig.PLUG_DEFAULT_IMAGE_NAME + "_00", false);
                    plugVo.setUuid(String.valueOf(vo.getDeviceId()));
                    plugVoList.add(plugVo);
                }
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(plugVoList);
            RequestParams params = new RequestParams();
            params.put("placeId", placeVo.getPlaceId());
            params.put("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_PLUG, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestUpdateBluetooth(DbBluetoothVo dbBluetoothVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_BLUETOOTH;

        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(dbBluetoothVo);
            RequestParams params = new RequestParams();
            params.put("placeId", placeVo.getPlaceId());
            params.put("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_BLUETOOTH, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void deleteAssociatedDevice(RegDeviceVo regDeviceVo){
        String deviceStringId = String.format("%x", regDeviceVo.getDeviceId());
        String unixTime = String.format("%x", System.currentTimeMillis() / 1000);

        String requestMessage = BLConfig.ASSOCIATION_REQUEST_NUM + deviceStringId + unixTime;

        MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
    }

    public boolean updateDbPlugList(List<PlugVo> voList, String ssid){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbPlugVoDao theDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> dbPlugVoList = new ArrayList<>();
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            for(PlugVo vo : voList) {
                DbPlugVo dbPlugVo = new DbPlugVo(
                        placeVo.getPlaceId(), vo.getPlugId(), vo.getPlugName(), vo.getNetworkType(),
                        vo.getPlugIconImg(), vo.getBssid(), vo.getRouterIp(), vo.getGatewayIp(), vo.getUuid());
                dbPlugVoList.add(dbPlugVo);
                String type = dbPlugVo.getPlugType();
                if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
                    updateDbRouter(daoSession, vo, ssid);
                }  else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
                    updateDbGateway(daoSession, vo, ssid);
                } else if(type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                    updateDbAP(daoSession, vo, ssid);
                }
//                else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
//                    updateDbBluetooth(daoSession, vo);
//                }
            }

            theDao.insertOrReplaceInTx(dbPlugVoList);

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

    private boolean updateDbRouter(DaoSession daoSession, PlugVo plugVo, String ssid){
        try{
            DbRouterVoDao theDao = daoSession.getDbRouterVoDao();
            DbRouterVo theDbVo = new DbRouterVo();

            theDbVo.setRouterIp(plugVo.getRouterIp());
            theDbVo.setSsId(ssid);

            theDao.insertOrReplace(theDbVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateDbAP(DaoSession daoSession, PlugVo plugVo, String ssid){
        try{
            DbApVoDao theDao = daoSession.getDbApVoDao();
            DbApVo theDbVo = new DbApVo();

            theDbVo.setBssId(plugVo.getBssid());
            theDbVo.setSsId(ssid);
            theDbVo.setPassword("12345678");

            theDao.insertOrReplace(theDbVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateDbGateway(DaoSession daoSession, PlugVo plugVo, String ssid){
        try{
            DbGatewayVoDao theDao = daoSession.getDbGatewayVoDao();
            DbGatewayVo theDbVo = new DbGatewayVo();

            theDbVo.setGatewayIp(plugVo.getGatewayIp()); // TODO : Gateway ID -> Gateway IP
            theDbVo.setSsId(ssid); // TODO : Ip Address -> SSID

            theDao.insertOrReplace(theDbVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDbBluetooth(DbBluetoothVo dbBluetoothVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbBluetoothVoDao theDao = daoSession.getDbBluetoothVoDao();

            theDao.insertOrReplace(dbBluetoothVo);
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

    // Set Data
    public String getUDPScanJsonData(){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.NODE_LIST_MSG, HttpConfig.NODE_LIST_CMD);
        NodeListRequestVo nodeListRequestVo = new NodeListRequestVo(commonDataVo);

        String json = new Gson().toJson(nodeListRequestVo);
        return json;
    }

    private final String ARG_WIFI_NAME = "wifi";
    private final String ARG_WIFI_PASSWORD = "wifi_password";
    public void saveWifiPassword(String password){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_WIFI_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_WIFI_PASSWORD, password);
        edit.commit();
    }

    public String loadWifiPassword(){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_WIFI_NAME, 0);
        String password = preference.getString(ARG_WIFI_PASSWORD, "");

        return password;
    }

    public boolean isAPMode(){
        WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mContext);
        WifiVo wifiVo = wifiConnectionManager.getConnectedWifiInfo();
        return wifiVo.getSsid().contains(SPConfig.PLUG_SSID_WIFI_AP_NAME);
    }

    public boolean checkDuplicatedPlug(List<RegDeviceVo> regDeviceVoList){
        PlugAllService service = new PlugAllService(mContext);
        for( RegDeviceVo regDeviceVo : regDeviceVoList){
            String type = regDeviceVo.getNetworkType();
            List<PlugVo> plugVoList = service.selectDbPlugList();
            for(PlugVo plugVo : plugVoList){
                if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
                    String plug_ip = regDeviceVo.getIpAddress();
                    if( plug_ip.equalsIgnoreCase(plugVo.getBssid())){
                        SPUtil.showToast(mContext, "선택하신 " + regDeviceVo.getPlugId() + "(은)는 이미 추가되어 있습니다. 추가된 플러그의 이름은 " + plugVo.getPlugName() + "입니다.");
                        return false;
                    }
                } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
                    String bssid = regDeviceVo.getBssid();
                    if( bssid.equalsIgnoreCase(plugVo.getBssid())){
                        SPUtil.showToast(mContext, "선택하신 " + regDeviceVo.getPlugId() + "(은)는 이미 추가되어 있습니다. 추가된 플러그의 이름은 " + plugVo.getPlugName() + "입니다.");
                    }
                }
            }
        }
        return true;
    }
}
