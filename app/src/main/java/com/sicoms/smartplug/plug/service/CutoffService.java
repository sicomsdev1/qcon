package com.sicoms.smartplug.plug.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbCutOffVo;
import com.sicoms.smartplug.dao.DbCutOffVoDao;
import com.sicoms.smartplug.domain.CommonDataVo;
import com.sicoms.smartplug.domain.CutoffResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.CutoffDataVo;
import com.sicoms.smartplug.domain.CutoffRequestVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.network.bluetooth.BLConfig;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class CutoffService implements UDPClient.UDPResponseCallbacks {
    private static final String TAG = CutoffService.class.getSimpleName();

    private Context mContext;
    private DBHelper mDBHelper;
    private CutoffResultCallbacks mCallbacks;

    public CutoffService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnCutoffResultCallbacks(CutoffResultCallbacks callbacks){
        mCallbacks = callbacks;
    }

    public List<CutoffVo> selectDbCutoffList(PlugVo plugVo){
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbCutOffVoDao theDao = daoSession.getDbCutOffVoDao();
            List<DbCutOffVo> theDbVoList = theDao.queryBuilder()
                    .where(DbCutOffVoDao.Properties.PlugId.eq(plugVo.getPlugId())).list();

            if (theDbVoList != null) {
                List<CutoffVo> scheduleVoList = new ArrayList<>();
                for (DbCutOffVo vo : theDbVoList) {
                    boolean isOn = vo.getUseYn().equalsIgnoreCase(SPConfig.STATUS_ON);
                    String setMin = vo.getSetMin();
                    String setWatt = vo.getSetWatt();
                    CutoffVo cutoffVo = new CutoffVo(setWatt, setMin, isOn);
                    scheduleVoList.add(cutoffVo);
                }

                mDBHelper.closeSession();
                return scheduleVoList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean updateDbCutoff(PlugVo plugVo, CutoffVo cutoffVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbCutOffVoDao theDao = daoSession.getDbCutOffVoDao();
            DbCutOffVo theDbVo = new DbCutOffVo();

            theDbVo.setCutSeq((long) 0);
            theDbVo.setPlugId(plugVo.getPlugId());
            theDbVo.setSetWatt(cutoffVo.getPower());
            theDbVo.setSetMin(cutoffVo.getMin());
            theDbVo.setUseYn(cutoffVo.isOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);

            theDao.insertOrReplace(theDbVo);

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

    public boolean deleteDbCutoff(PlugVo plugVo, CutoffVo cutoffVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbCutOffVoDao theDao = daoSession.getDbCutOffVoDao();
            DbCutOffVo theDbVo = new DbCutOffVo();

            theDbVo.setPlugId(plugVo.getPlugId());
            theDbVo.setSetWatt(cutoffVo.getPower());
            theDbVo.setSetMin(cutoffVo.getMin());
            theDbVo.setUseYn(SPConfig.STATUS_ON);

            theDao.delete(theDbVo);

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

    public boolean deleteDbAllCutoff(){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbCutOffVoDao theDao = daoSession.getDbCutOffVoDao();
            theDao.deleteAll();
            mDBHelper.closeSession();
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    /*
     * Network Method
     */
    public void requestGetCutoffInDevice(PlugVo plugVo){
        String type = plugVo.getNetworkType();
        if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetCutoffJsonData("");
            if( json != null) {
                udpClient.execute(plugVo.getBssid(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetCutoffJsonData("");
            if( json != null) {
                udpClient.execute(json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetCutoffJsonData(plugVo.getPlugId());
            if( json != null) {
                udpClient.execute(plugVo.getGatewayIp(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
            int uuid = Integer.parseInt(plugVo.getUuid());
            String requestMessage = BLMessage.getGetCutoffRequestMessage(MainActivity.stBluetoothManager, uuid);

            if( !MainActivity.stBluetoothManager.isConnected()){
                return;
            }
            MainActivity.stBluetoothManager.setOnCutoffResultCallbacks(mCallbacks);
            MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
        }
    }

    public void setCutoffDeviceInDevice(PlugVo plugVo, CutoffVo cutoffVo){
        // 비활성화시 0W로 설정하여 전원차단 안되게 값 저장
        if( !cutoffVo.isOn()){
            cutoffVo.setPower(SPConfig.NO_CUTOFF);
            cutoffVo.setMin("00");
        }

        String type = plugVo.getNetworkType();
        if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetCutoffJsonData("", cutoffVo);
            if( json != null) {
                udpClient.execute(plugVo.getBssid(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetCutoffJsonData("", cutoffVo);
            if( json != null) {
                udpClient.execute(json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetCutoffJsonData(plugVo.getPlugId(), cutoffVo);
            if( json != null) {
                udpClient.execute(plugVo.getGatewayIp(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
            try {
                int uuid = Integer.parseInt(plugVo.getUuid());
                int power = Integer.parseInt(cutoffVo.getPower());
                int min = Integer.parseInt(cutoffVo.getMin()) * 60;
                String status = cutoffVo.isOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF;
                String requestMessage = BLMessage.getSetCutoffRequestMessage(MainActivity.stBluetoothManager, uuid, power, min, status);

                MainActivity.stBluetoothManager.setCutoffVo(cutoffVo);
                MainActivity.stBluetoothManager.setOnCutoffResultCallbacks(mCallbacks);
                if (MainActivity.stBluetoothManager.isConnected()) {
                    MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
                }
            } catch (NumberFormatException nfe){
                return;
            }
        }
    }

    private String getSetCutoffJsonData(String plugId, CutoffVo cutoffVo){
        try {
            CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CUTOFF_MSG, HttpConfig.CUTOFF_CMD);
            int mA = Integer.parseInt(cutoffVo.getPower()) * 1000;
            int second = Integer.parseInt(cutoffVo.getMin()) * 60;
            String useYN = cutoffVo.isOn() ? "Y" : "N";
            CutoffDataVo cutoffDataVo = new CutoffDataVo(plugId, String.valueOf(mA), String.valueOf(second), useYN);
            CutoffRequestVo scheduleRequestVo = new CutoffRequestVo(commonDataVo, cutoffDataVo);
            String json = new Gson().toJson(scheduleRequestVo);

            return json;
        }catch (NumberFormatException nfe){
            return null;
        }
    }

    private String getGetCutoffJsonData(String plugId){
        try {
//            CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CUTOFF_MSG, HttpConfig.CUTOFF_CMD);
//            int mA = Integer.parseInt(cutoffVo.getPower()) * 1000;
//            int second = Integer.parseInt(cutoffVo.getMin()) * 60;
//            String useYN = cutoffVo.isOn() ? "Y" : "N";
//            CutoffDataVo cutoffDataVo = new CutoffDataVo(plugId, String.valueOf(mA), String.valueOf(second), useYN);
//            CutoffRequestVo scheduleRequestVo = new CutoffRequestVo(commonDataVo, cutoffDataVo);
            String json = "";//new Gson().toJson(scheduleRequestVo);

            return json;
        }catch (NumberFormatException nfe){
            return null;
        }
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {
        if (result != UDPConfig.UDP_SUCCESS) {
            SPUtil.showToast(mContext, "전원 차단 설정에 실패하였습니다.");
            return;
        }
        try {
            CutoffResponseVo cutoffResponseVo = new Gson().fromJson(response, CutoffResponseVo.class);
            CutoffDataVo scheduleDataVo = cutoffResponseVo.getDp();
            float watt = Integer.parseInt(scheduleDataVo.getP()) / 1000;
            int min = Integer.parseInt(scheduleDataVo.getS()) / 60;
            boolean isOn = scheduleDataVo.getU().equalsIgnoreCase("Y") ? true : false;
            CutoffVo cutoffVo = new CutoffVo(String.valueOf(watt), String.valueOf(min), isOn);
            mCallbacks.onCutoffResult(cutoffVo);
        } catch (JsonSyntaxException exception) {
            return;
        } catch (NumberFormatException nfe){
            return;
        }
    }
}
