package com.sicoms.smartplug.plug.service;

import android.app.Activity;
import android.database.sqlite.SQLiteConstraintException;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbScheduleVo;
import com.sicoms.smartplug.dao.DbScheduleVoDao;
import com.sicoms.smartplug.domain.CommonDataVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.ScheduleDataVo;
import com.sicoms.smartplug.domain.ScheduleRequestVo;
import com.sicoms.smartplug.domain.ScheduleResponseVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class ScheduleService implements UDPClient.UDPResponseCallbacks {
    private static final String TAG = ScheduleService.class.getSimpleName();

    private Activity mActivity;
    private DBHelper mDBHelper;
    private ScheduleResultCallbacks mCallbacks;

    public ScheduleService(Activity activity) {
        mActivity = activity;
        mDBHelper = new DBHelper(mActivity);
    }
    public void setOnScheduleResultCallbacks(ScheduleResultCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Local DB Method
     */
    public List<ScheduleVo> selectDbScheduleList(PlugVo plugVo) {
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbScheduleVoDao theDao = daoSession.getDbScheduleVoDao();
            List<DbScheduleVo> theDbVoList = theDao.queryBuilder()
                    .where(DbScheduleVoDao.Properties.PlugId.eq(plugVo.getPlugId())).list();

            if (theDbVoList != null) {
                List<ScheduleVo> scheduleVoList = new ArrayList<>();
                for (DbScheduleVo vo : theDbVoList) {
                    boolean isStartOn = vo.getStartUseYn().equalsIgnoreCase(SPConfig.STATUS_ON);
                    boolean isEndOn = vo.getEndUseYn().equalsIgnoreCase(SPConfig.STATUS_ON);
                    String startAmPm = vo.getStartTime().split(" ")[0];
                    String endAmPm = vo.getEndTime().split(" ")[0];
                    String startTime = vo.getStartTime().split(" ")[1];
                    String endTime = vo.getEndTime().split(" ")[1];
                    ScheduleVo scheduleVo = new ScheduleVo(vo.getSchSeq(), startAmPm, endAmPm, startTime, endTime, isStartOn, isEndOn);
                    scheduleVoList.add(scheduleVo);
                }

                mDBHelper.closeSession();
                return scheduleVoList;
            }
        } catch (SQLiteConstraintException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }

    public boolean updateDbSchedule(PlugVo plugVo, ScheduleVo scheduleVo) {
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbScheduleVoDao theDao = daoSession.getDbScheduleVoDao();
            DbScheduleVo theDbVo = new DbScheduleVo();

            theDbVo.setSchSeq(scheduleVo.getSchSeq());
            theDbVo.setPlugId(plugVo.getPlugId());
            theDbVo.setStartTime(scheduleVo.getStartAmPm() + " " + scheduleVo.getStartTime());
            theDbVo.setEndTime(scheduleVo.getEndAmPm() + " " + scheduleVo.getEndTime());
            theDbVo.setStartUseYn(scheduleVo.isStartOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);
            theDbVo.setEndUseYn(scheduleVo.isEndOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF);

            theDao.insertOrReplace(theDbVo);

            mDBHelper.closeSession();
            return true;
        } catch (SQLiteConstraintException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbSchedule(PlugVo plugVo, ScheduleVo scheduleVo) {
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbScheduleVoDao theDao = daoSession.getDbScheduleVoDao();
            DbScheduleVo theDbVo = theDao.queryBuilder().where(DbScheduleVoDao.Properties.SchSeq.eq(scheduleVo.getSchSeq())).unique();

            theDao.delete(theDbVo);

            mDBHelper.closeSession();
            return true;
        } catch (SQLiteConstraintException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    public boolean deleteDbAllSchedule() {
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbScheduleVoDao theDao = daoSession.getDbScheduleVoDao();
            theDao.deleteAll();
            mDBHelper.closeSession();
        } catch (SQLiteConstraintException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return false;
    }

    /*
     * Network Method
     */
    public void requestGetScheduleInDevice(PlugVo plugVo){
        String type = plugVo.getNetworkType();
        if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetScheduleJsonData(plugVo.getPlugId());
            if( json != null) {
                udpClient.execute(plugVo.getBssid(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetScheduleJsonData("");
            if( json != null) {
                udpClient.execute(json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getGetScheduleJsonData(plugVo.getPlugId());
            if( json != null) {
                udpClient.execute(plugVo.getGatewayIp(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
            int uuid = Integer.parseInt(plugVo.getUuid());
            String requestMessage = BLMessage.getGetScheduleRequestMessage(MainActivity.stBluetoothManager, uuid, 1);

            if( !MainActivity.stBluetoothManager.isConnected()){
                return;
            }
            MainActivity.stBluetoothManager.setOnScheduleResultCallbcks(mCallbacks);
            MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
        }
    }

    public void setScheduleInDevice(PlugVo plugVo, ScheduleVo scheduleVo) {
        // 비활성화시 25시로 설정하여 스케쥴 제어 안되게 값 저장
        if( !scheduleVo.isStartOn()){
            scheduleVo.setStartAmPm(SPConfig.AM);
            scheduleVo.setStartTime(SPConfig.NO_SCHEDULE);
        }
        if( !scheduleVo.isEndOn()){
            scheduleVo.setEndAmPm(SPConfig.AM);
            scheduleVo.setEndTime(SPConfig.NO_SCHEDULE);
        }

        String type = plugVo.getNetworkType();
        if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetScheduleJsonData(plugVo.getPlugId(), scheduleVo);
            if( json != null) {
                udpClient.execute(plugVo.getBssid(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetScheduleJsonData("", scheduleVo);
            if( json != null) {
                udpClient.execute(json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)) {
            UDPClient udpClient = new UDPClient();
            udpClient.setOnUDPResponseCallbacks(this);
            String json = getSetScheduleJsonData(plugVo.getPlugId(), scheduleVo);
            if( json != null) {
                udpClient.execute(plugVo.getGatewayIp(), json);
            }
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) {
            try {
                int uuid = Integer.parseInt(plugVo.getUuid());
                // String deviceStringId = String.format("%x", Integer.parseInt(plugVo.getUuid()));
                String startHour = scheduleVo.getStartTime().split(":")[0];
                String startMin = scheduleVo.getStartTime().split(":")[1];
                if (scheduleVo.getStartAmPm().equalsIgnoreCase(SPConfig.PM)) {
                    int hour = Integer.parseInt(startHour) + 12;
                    if (hour == 24) {
                        hour = 0;
                    }
                    startHour = String.valueOf(hour);
                }
                String startTime = startHour + startMin;
                String endHour = scheduleVo.getEndTime().split(":")[0];
                String endMin = scheduleVo.getEndTime().split(":")[1];
                if (scheduleVo.getEndAmPm().equalsIgnoreCase(SPConfig.PM)) {
                    int hour = Integer.parseInt(endHour) + 12;
                    if (hour == 24) {
                        hour = 0;
                    }
                    endHour = String.valueOf(hour);
                }
                String endTime = endHour + endMin;
                String status = scheduleVo.isStartOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF;
                int nStartTime = Integer.parseInt(startTime);
                int nEndTime = Integer.parseInt(endTime);

                String requestMessage = BLMessage.getScheduleRequestMessage(MainActivity.stBluetoothManager, uuid, scheduleVo.getSchSeq(), nStartTime, nEndTime, status);

                MainActivity.stBluetoothManager.setOnScheduleResultCallbcks(mCallbacks);
                MainActivity.stBluetoothManager.setSchedule(scheduleVo);
                MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
            } catch (NumberFormatException nfe){
                return;
            }
        }
    }

    private String getSetScheduleJsonData(String plugId, ScheduleVo scheduleVo){
        try {
            CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.SCHEDULE_MSG, HttpConfig.SCHEDULE_CMD);

            String startHour = scheduleVo.getStartTime().split(":")[0];
            String startMin = scheduleVo.getStartTime().split(":")[1];
            if (scheduleVo.getStartAmPm().equalsIgnoreCase(SPConfig.PM)) {
                int hour = Integer.parseInt(startHour) + 12;
                if (hour == 24) {
                    hour = 0;
                }
                startHour = String.format("%02d", hour);
            }
            String startTime = startHour + ":" + startMin;

            String endHour = scheduleVo.getEndTime().split(":")[0];
            String endMin = scheduleVo.getEndTime().split(":")[1];
            if (scheduleVo.getEndAmPm().equalsIgnoreCase(SPConfig.PM)) {
                int hour = Integer.parseInt(endHour) + 12;
                if (hour == 24) {
                    hour = 0;
                }
                endHour = String.format("%02d", hour);
            }
            String endTime = endHour + ":" + endMin;
            if( startTime.length() != 5 || endTime.length() != 5){
                Toast.makeText(mActivity, "시간 자리수 오류", Toast.LENGTH_SHORT).show();
                return null;
            }
            ScheduleDataVo scheduleDataVo = new ScheduleDataVo(plugId, startTime, endTime, "Y");
            ScheduleRequestVo scheduleRequestVo = new ScheduleRequestVo(commonDataVo, scheduleDataVo);
            String json = new Gson().toJson(scheduleRequestVo);

            return json;
        }catch (NumberFormatException nfe){
            return null;
        }
    }

    private String getGetScheduleJsonData(String plugId){
        try {
//            CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.SCHEDULE_MSG, HttpConfig.SCHEDULE_CMD);
//
//            String startHour = scheduleVo.getStartTime().split(":")[0];
//            String startMin = scheduleVo.getStartTime().split(":")[1];
//            if (scheduleVo.getStartAmPm().equalsIgnoreCase(SPConfig.PM)) {
//                int hour = Integer.parseInt(startHour) + 12;
//                if (hour == 24) {
//                    hour = 0;
//                }
//                startHour = String.format("%02d", hour);
//            }
//            String startTime = startHour + ":" + startMin;
//
//            String endHour = scheduleVo.getEndTime().split(":")[0];
//            String endMin = scheduleVo.getEndTime().split(":")[1];
//            if (scheduleVo.getEndAmPm().equalsIgnoreCase(SPConfig.PM)) {
//                int hour = Integer.parseInt(endHour) + 12;
//                if (hour == 24) {
//                    hour = 0;
//                }
//                endHour = String.format("%02d", hour);
//            }
//            String endTime = endHour + ":" + endMin;
//            if( startTime.length() != 5 || endTime.length() != 5){
//                Toast.makeText(mActivity, "시간 자리수 오류", Toast.LENGTH_SHORT).show();
//                return null;
//            }
//            ScheduleDataVo scheduleDataVo = new ScheduleDataVo(plugId, startTime, endTime, "Y");
//            ScheduleRequestVo scheduleRequestVo = new ScheduleRequestVo(commonDataVo, scheduleDataVo);
            String json = "";//new Gson().toJson(scheduleRequestVo);

            return json;
        }catch (NumberFormatException nfe){
            return null;
        }
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {
        if (result != UDPConfig.UDP_SUCCESS) {
            SPUtil.showToast(mActivity, "스케쥴 설정에 실패하였습니다.");
            return;
        }
        try {
            ScheduleResponseVo scheduleResponseVo = new Gson().fromJson(response, ScheduleResponseVo.class);
            ScheduleDataVo scheduleDataVo = scheduleResponseVo.getDp();
            String startAmPm = SPConfig.AM;
            String startTime = scheduleDataVo.getS();
            int hour = Integer.parseInt(startTime.split(":")[0]);
            if( hour > 12){
                startAmPm = SPConfig.PM;
            }
            String endAmPm = SPConfig.AM;
            String endTime = scheduleDataVo.getE();
            hour = Integer.parseInt(endTime.split(":")[0]);
            if( hour > 12){
                endAmPm = SPConfig.PM;
            }
            boolean isOn = scheduleDataVo.getU().equalsIgnoreCase("Y") ? true : false;
            ScheduleVo scheduleVo = new ScheduleVo(startAmPm, endAmPm, startTime, endTime, isOn, isOn);
            mCallbacks.onScheduleResult(scheduleVo);
        } catch (JsonSyntaxException exception) {
            return;
        } catch (NumberFormatException nfe){
            return;
        }
    }
}