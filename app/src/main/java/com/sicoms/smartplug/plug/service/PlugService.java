package com.sicoms.smartplug.plug.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.sicoms.smartplug.domain.CommonDataVo;
import com.sicoms.smartplug.domain.ControlNodeDataVo;
import com.sicoms.smartplug.domain.ControlNodeRequestVo;
import com.sicoms.smartplug.domain.LEDDataVo;
import com.sicoms.smartplug.domain.LEDRequestVo;
import com.sicoms.smartplug.domain.PointListRequestVo;
import com.sicoms.smartplug.domain.CutoffDataVo;
import com.sicoms.smartplug.domain.CutoffRequestVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.plug.interfaces.LastDataResultCallbacks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 12..
 */
public class PlugService {
    private static final String TAG = PlugService.class.getSimpleName();
    private final String ARG_MOOD_NAME = "mood";
    private final String ARG_MOOD_LAST_STATUS = "mood_last_status";

    private Activity mActivity;
    private Context mContext;
    private LastDataResultCallbacks mLastDataCallbacks;

    public PlugService(Activity activity){
        mActivity = activity;
        mContext = mActivity;
    }
    public PlugService(Context context){
        mContext = context;
    }
    public void setOnLastDataResultCallbacks(LastDataResultCallbacks callbacks){
        mLastDataCallbacks = callbacks;
    }

    public String getControlNodeRequestJsonData(List<String> plugIdList, String on_off){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CONTROL_ON_OFF_MSG, HttpConfig.CONTROL_ON_OFF_CMD);
        commonDataVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        commonDataVo.setTr("1234567");
        ControlNodeRequestVo controlNodeRequestVo = new ControlNodeRequestVo(commonDataVo);
        ControlNodeDataVo data = new ControlNodeDataVo();
        data.setS(on_off);
        if( plugIdList != null) {
            data.setD(plugIdList);
        }
        controlNodeRequestVo.setDp(data);

        Gson gson = new Gson();
        String json = gson.toJson(controlNodeRequestVo) + "\n\n";

        return json;
    }

    public String getControlNodeRequestJsonDataForAP(List<String> plugIdList, String on_off){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CONTROL_ON_OFF_MSG, HttpConfig.CONTROL_ON_OFF_CMD);
        commonDataVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        commonDataVo.setTr("1234567");
        ControlNodeRequestVo controlNodeRequestVo = new ControlNodeRequestVo(commonDataVo);
        ControlNodeDataVo data = new ControlNodeDataVo();
        data.setS(on_off);
        if( plugIdList != null) {
            data.setD(plugIdList);
        }
        controlNodeRequestVo.setDp(data);

        Gson gson = new Gson();
        String json = gson.toJson(controlNodeRequestVo) + "\n\n";

        return json;
    }

    public String getLEDRequestJsonData(boolean isOn){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CONTROL_ON_OFF_MSG, HttpConfig.CONTROL_ON_OFF_CMD);
        commonDataVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        commonDataVo.setTr("1234567");

        String on_off = isOn ? "1" : "0";
        LEDDataVo data = new LEDDataVo(on_off);

        LEDRequestVo ledRequestVo = new LEDRequestVo(commonDataVo, data);

        Gson gson = new Gson();
        String json = gson.toJson(ledRequestVo) + "\n\n";

        return json;
    }

//    private void updateDBLastData(List<String> plugIdList, String on_off){
//        DBHelper dbHelper = new DBHelper(mActivity);
//        DaoSession daoSession = dbHelper.getSession(true);
//        DbLastDataVoDao lastDataVoDao = daoSession.getDbLastDataVoDao();
//
//        if( lastDataVoDao == null){
//            return;
//        }
//
//        for(String plugId : plugIdList) {
//            DbLastDataVo dbLastDataVo = lastDataVoDao.queryBuilder().where(DbLastDataVoDao.Properties.PlugId.eq(plugId)).unique();
//            if( dbLastDataVo != null) {
//                dbLastDataVo.setRecTime(new Date());
//                dbLastDataVo.setOnOff(on_off);
//                lastDataVoDao.insertOrReplaceInTx(dbLastDataVo);
//                mLastDataCallbacks.onGetLastData(mPlugIdList.get(0), String.valueOf(dbLastDataVo.getWh()), String.valueOf(dbLastDataVo.getW()), dbLastDataVo.getOnOff());
//            }
//        }
//    }

    private Runnable showResultMessage = new Runnable() {

        @Override
        public void run() {

        }
    };

    public String getStandbyPowerRequestJsonData(CutoffDataVo cutoffDataVo){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.CUTOFF_MSG, HttpConfig.CUTOFF_CMD);
        CutoffRequestVo cutoffRequestVo = new CutoffRequestVo(commonDataVo, cutoffDataVo);
        String json = new Gson().toJson(cutoffRequestVo) + "\n\n";

        return json;
    }

    public String getPointListRequestJsonData(){
        PointListRequestVo pointListRequestVo = new PointListRequestVo();
        pointListRequestVo.setMsg(HttpConfig.POINT_LIST_MSG);
        pointListRequestVo.setCmd(HttpConfig.POINT_LIST_CMD);
        pointListRequestVo.setTr(HttpConfig.POINT_LIST_TR);
        pointListRequestVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));

        Gson gson = new Gson();
        String json = gson.toJson(pointListRequestVo) + "\n\n";

        return json;
    }
}
