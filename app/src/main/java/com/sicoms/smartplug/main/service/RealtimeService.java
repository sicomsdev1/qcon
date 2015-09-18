package com.sicoms.smartplug.main.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.csr.mesh.MeshService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbApVo;
import com.sicoms.smartplug.dao.DbApVoDao;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.dao.DbBluetoothVoDao;
import com.sicoms.smartplug.dao.DbGatewayVo;
import com.sicoms.smartplug.dao.DbGatewayVoDao;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbLastDataVoDao;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbRouterVo;
import com.sicoms.smartplug.dao.DbRouterVoDao;
import com.sicoms.smartplug.domain.BluetoothVo;
import com.sicoms.smartplug.domain.PlaceSettingVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.PointListDataVo;
import com.sicoms.smartplug.domain.PointListRequestVo;
import com.sicoms.smartplug.domain.PointListResponseVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.main.interfaces.BLIsEnabledCallbacks;
import com.sicoms.smartplug.main.interfaces.BLScanResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.menu.service.PlaceSettingService;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.bluetooth.BLScanner;
import com.sicoms.smartplug.network.bluetooth.BluetoothManager;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.interfaces.LastDataResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pc-11-user on 2015-04-17.
 */
public class RealtimeService implements LastDataResultCallbacks, UDPClient.UDPResponseCallbacks, BLIsEnabledCallbacks, BLScanResultCallbacks, BluetoothManager.BLResultCallbacks {

    private static final String TAG = "AsyncRealtimeService";

    private Context mContext;
    private String mJson;
    private DBHelper mDBHelper;
    private BLScanner mScanner;

    private boolean isConnecting = false;
    private boolean isBind = false;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public RealtimeService(Context activity){
        mContext = activity;
        mJson = getPointListRequestJsonData();
        mDBHelper = new DBHelper(mContext);
        mScanner = new BLScanner(mContext);
        mScanner.setOnBLScanResultCallbacks(this);
        mScanner.setOnBluetoothIsEnabledCallbacks(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mScanner.getBluetoothReceiver(), filter);
        MainActivity.stBluetoothManager.setOnBLResultCallbacks(this);
        MainActivity.stBluetoothManager.setOnLastDataResultCallbacks(this);
    }

    public void runService() {
        try {
            if (mScanner.isEnableBluetooth()) {
                if( !MainActivity.stBluetoothManager.isConnected() && !isConnecting){
                    connectBluetooth();
                }
            }
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbPlugVoDao theDao = daoSession.getDbPlugVoDao();
            List<DbPlugVo> dataList = theDao.queryBuilder().listLazy();
            List<PlugVo> plugVoList = new ArrayList<>();

            int offset = 0;
            for (DbPlugVo dbPlugVo : dataList) {
                PlugVo plugVo = new PlugVo(dbPlugVo.getPlugName(), dbPlugVo.getPlugId(), dbPlugVo.getPlugType(), dbPlugVo.getPlugImg(), false);
                plugVo.setBssid(dbPlugVo.getBssId());
                plugVo.setRouterIp(dbPlugVo.getRouterIp());
                plugVo.setGatewayIp(dbPlugVo.getGatewayIp());
                plugVo.setUuid(dbPlugVo.getUuid());
                plugVoList.add(plugVo);
            }
            setData(plugVoList);
        } catch ( SQLiteException se){
            se.printStackTrace();
        }
        mDBHelper.closeSession();
    }

    private void setData(List<PlugVo> plugVoList) {
        List<PlugVo> gwVoList = new ArrayList<>();
        List<PlugVo> apVoList = new ArrayList<>();
        List<PlugVo> stVoList = new ArrayList<>();
        List<PlugVo> blVoList = new ArrayList<>();

        // 현재 연결 된 Wi-Fi 가 저장 된 AP 정보와 같을 경우 AP 만 데이터 수집
        WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mContext);
        String wifiSssid = wifiConnectionManager.getConnectedWifiInfo().getSsid();

        DaoSession daoSession = mDBHelper.getSession(true);

        for (PlugVo plugVo : plugVoList) {
            if (plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
                gwVoList.add(plugVo);
            } else if (plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
                apVoList.add(plugVo);
            } else if (plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
                stVoList.add(plugVo);
            } else if (plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                blVoList.add(plugVo);
            } else {
                continue;
            }
        }

        if (apVoList.size() > 0) {
            DbApVoDao theDao = daoSession.getDbApVoDao();

            for(PlugVo apVo : apVoList) {
                DbApVo theDbVo = theDao.queryBuilder().where(DbApVoDao.Properties.BssId.eq(apVo.getBssid())).unique();
                if( theDbVo == null){
                    continue;
                }
                if (wifiSssid.equalsIgnoreCase(theDbVo.getSsId())) {
                    setRealtimeAPData(apVo);
                }
            }
        }

        if (gwVoList.size() > 0) {
            DbGatewayVoDao theDao = daoSession.getDbGatewayVoDao();

            for(PlugVo gwVo : gwVoList) {
                DbGatewayVo theDbVo = theDao.queryBuilder().where(DbGatewayVoDao.Properties.GatewayIp.eq(gwVo.getGatewayIp())).unique();
                if( wifiSssid.equalsIgnoreCase(theDbVo.getSsId())) {
                    setRealtimeGatewayData(gwVo);
                }
            }
        }

        if (stVoList.size() > 0) {
            DbRouterVoDao theDao = daoSession.getDbRouterVoDao();

            for(PlugVo stVo : stVoList) {
                DbRouterVo theDbVo = theDao.queryBuilder().where(DbRouterVoDao.Properties.RouterIp.eq(stVo.getBssid())).unique();
                if( theDbVo == null){
                    continue;
                }
                if( wifiSssid.equalsIgnoreCase(theDbVo.getSsId())) {
                    setRealtimeStationData(stVoList);
                }
            }
        }
        if (blVoList.size() > 0) {
            setRealtimeBluetoothData(blVoList);
        }
    }

    private void setRealtimeGatewayData(final PlugVo plugVo){
        UDPClient udpClient = new UDPClient();
        udpClient.setOnUDPResponseCallbacks(this);
        String params[] = {plugVo.getGatewayIp(), mJson};
        udpClient.execute(params);
        Log.d(TAG, "setRealtimeGatewayData (name : " + plugVo.getPlugName() + ")");
    }

    private void setRealtimeAPData(final PlugVo plugVo){
        UDPClient udpClient = new UDPClient();
        udpClient.setOnUDPResponseCallbacks(this);
        udpClient.execute(SPConfig.AP_IP, mJson);
    }

    private void setRealtimeStationData(List<PlugVo> plugVoList){
        for(PlugVo plugVo : plugVoList){
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String params[] = {plugVo.getBssid(), mJson};
            udpClient.execute(params);
        }
        Log.d(TAG, "setRealtimeStationData (size : " + plugVoList.size() + ")");
    }

    private void setRealtimeBluetoothData(List<PlugVo> plugVoList){
        for( int cnt=0; cnt<plugVoList.size(); cnt++) {
            PlugVo plugVo = plugVoList.get(cnt);
            int deviceId = Integer.parseInt(plugVo.getUuid());
            String requestMessage = BLMessage.getDataRequestMessage(MainActivity.stBluetoothManager, deviceId);
            Log.d(TAG, "Request BL Last Data (message : " + requestMessage + ")");
            if( MainActivity.stBluetoothManager.isConnected()) {
                MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
            }
            SPUtil.sleep(5000);
        }
    }

    /*
     * Point List 관련 HTTP 요청에 대한 요청 메시지 생성
     */
    private String getPointListRequestJsonData(){
        PointListRequestVo pointListRequestVo = new PointListRequestVo();
        pointListRequestVo.setMsg(HttpConfig.POINT_LIST_MSG);
        pointListRequestVo.setCmd(HttpConfig.POINT_LIST_CMD);
        pointListRequestVo.setTr(HttpConfig.POINT_LIST_TR);
        pointListRequestVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));

        Gson gson = new Gson();
        String json = gson.toJson(pointListRequestVo) + "\n\n";

        return json;
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {
        if (result != UDPConfig.UDP_SUCCESS)
            return;
        try {
            PointListResponseVo pointListResponseVo = new Gson().fromJson(response, PointListResponseVo.class);
            List<PointListDataVo> pointListDataVoList = pointListResponseVo.getDp();
            if( pointListDataVoList == null){
                return;
            }
            for(PointListDataVo pointListDataVo : pointListDataVoList) {
                final String plugId = pointListDataVo.getND_CODE();
                // update db
                final float wh = Float.parseFloat(pointListDataVo.getWh());
                final float w = Float.parseFloat(pointListDataVo.getW());
                final String on_off = pointListDataVo.getS();

                if (!updateDbLastData(plugId, wh, w, on_off)) {
                    return;
                }
                if( SPConfig.IS_TEST) {
                    SPUtil.showToast(mContext, "Receive Message\nPlug Name : " + plugId + "\nUsage : " + wh + "wh, On/Off : " + on_off + "(0:off,1:on)");
                }
                Log.d(TAG, "[onUDPResponseResultStatus] Gateway & Router Mode Update");
            }
        } catch (JsonSyntaxException exception) {
            return;
        }
    }

    @Override
    public void onGetLastData(String deviceId, String wh, String w, String onoff) {
        float fWh = SPUtil.getLastWh(wh);
        float fW = SPUtil.getLastW(w);
        updateDbLastData(deviceId, fWh, fW, onoff);
    }

    private DbBluetoothVo selectDbBluetooth(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbBluetoothVoDao dbBluetoothVoDao = daoSession.getDbBluetoothVoDao();
            DbBluetoothVo dbBluetoothVo = dbBluetoothVoDao.queryBuilder().where(DbBluetoothVoDao.Properties.Uuid.eq(Integer.parseInt(plugVo.getUuid()))).unique();

            return dbBluetoothVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private boolean updateDbLastData(String plugId, Float wh, Float w, String on_off){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            DbLastDataVo dbLastDataVo = theDao.queryBuilder().where(DbPlugVoDao.Properties.PlugId.eq(plugId)).unique();
            if (dbLastDataVo == null) {
                dbLastDataVo = new DbLastDataVo(plugId, new Date(), 0.0f, 0.0f, SPConfig.STATUS_OFF, SPConfig.STATUS_ON);
            }

            dbLastDataVo.setRecTime(new Date());
            dbLastDataVo.setOnOff(on_off);
            dbLastDataVo.setWh(wh);
            dbLastDataVo.setW(w);
            theDao.insertOrReplaceInTx(dbLastDataVo);

            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
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

    private void connectBluetooth() {
        // 블루투스 활성화 시 연결
        mScanner.scanBluetoothDevice();
    }

    public void unregisterScannerReceiver(){
        mContext.unregisterReceiver(mScanner.getBluetoothReceiver());
    }

    @Override
    public void onIsEnablbled(boolean isEnabled) {
        if (isEnabled) {
            connectBluetooth();
        } else {
            if (isBind) {
                mContext.unbindService(MainActivity.stBluetoothManager.mServiceConnection);
                isBind = false;
            }
            SPUtil.dismissDialog();
        }
    }

    @Override
    public void onBLScanResult(ArrayList<BluetoothDevice> devices) {
        if (devices == null) {
            return;
        }
        SPUtil.showToast(mContext, "블루투스 플러그에 연결을 시도합니다.\n사용하지 않을 경우 블루투스를 꺼주세요.");
        MainActivity.stBluetoothManager.setDevices(devices);
        SPUtil.sleep(1000);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                isConnecting = true;
                if (!isBind) {
                    Intent bindIntent = new Intent(mContext, MeshService.class);
                    mContext.bindService(bindIntent, MainActivity.stBluetoothManager.mServiceConnection, Context.BIND_AUTO_CREATE);
                    isBind = true;
                } else {
                    MainActivity.stBluetoothManager.connect();
                }
            }
        });
    }

    @Override
    public void onBLConnectedResult(boolean isConnected) {
        if (isConnected) {
            SPUtil.showToast(mContext, "블루투스가 연결되었습니다.");
            PlaceSettingService service = new PlaceSettingService(mContext);
            PlaceSettingVo settingVo = service.selectDbBLPassword();
            if( settingVo == null || settingVo.getSetVal().equalsIgnoreCase("")){
                PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
                if( placeVo == null){
                    return;
                }
                settingVo = new PlaceSettingVo(placeVo.getPlaceId(), SPConfig.PLACE_SETTING_BL_PASSWORD, BLConfig.BL_DEFAULT_SECURITY_PASSWORD);
            }
            MainActivity.stBluetoothManager.setSecurity(settingVo.getSetVal(), false);
        } else {
            SPUtil.showToast(mContext, "Bluetooth 연결이 해제되었습니다.");
            MainActivity.stBluetoothManager.disconnectBluetooth();
        }
        isConnecting = false;
    }

    @Override
    public void onBLScanNonAssociationResult(BluetoothVo bluetoothVo) {

    }

    @Override
    public void onBLAssociationCompleteResult(int deviceId, int uuidHash) {

    }
}
