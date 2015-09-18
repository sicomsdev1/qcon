package com.sicoms.smartplug.plug.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.CommonDataVo;
import com.sicoms.smartplug.domain.NodeListRequestVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.WifiModeData;
import com.sicoms.smartplug.domain.WifiModeRequestVo;
import com.sicoms.smartplug.domain.WifiModeResponseVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.network.wifi.WifiConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.util.SPUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class RegRouterService {
    private static final String TAG = RegRouterService.class.getSimpleName();

    private Activity mActivity;

    private int mResult;
    private Handler mHandler;
    private WifiConnectionManager mWifiConnectionManager;
    private WifiVo mCurrentWifiVo;
    private String mPlugName;
    private String mPlugId;

    public RegRouterService(Activity activity){
        mActivity = activity;
    }

    public boolean connectPlugWifi(PlugVo plugVo){

        mPlugName = plugVo.getPlugName();
        mPlugId = plugVo.getPlugId();
        mWifiConnectionManager = new WifiConnectionManager(mActivity);

        // 1. Plug AP 에 접속
        String ssid = plugVo.getPlugId().split("/")[0];
        String bssid = plugVo.getPlugId().split("/")[0];

        WifiVo wifiVo = new WifiVo(ssid, bssid);
        wifiVo.setSecurityType(SPConfig.PLUG_AP_SECURITY_TYPE);
        wifiVo.setPassword(SPConfig.PLUG_AP_PASSWORD);

        if( !mWifiConnectionManager.connectWifi(wifiVo)){
            // retry 5
            Toast.makeText(mActivity, "AP 접속에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public String getWifiInfoJsonData(WifiVo wifiVo){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.WIFIMODE_MSG, HttpConfig.WIFIMODE_CMD);
        commonDataVo.setTr(HttpConfig.WIFIMODE_TR);
        commonDataVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        WifiModeRequestVo wifiModeRequestVo = new WifiModeRequestVo(commonDataVo);
        WifiModeData wifiModeData = new WifiModeData();
        wifiModeData.setM("s"); // Station Mode
        wifiModeData.setI(wifiVo.getSsid());
        String []auths = wifiVo.getSecurityType().split("[\\[|\\]]");
        String auth = WifiConfig.WIFI_AUTH_TYPE[0]; // Open

        for( int authCnt=0; authCnt< WifiConfig.WIFI_AUTH_TYPE.length; authCnt++) {
            for( int cnt=0; cnt<auths.length; cnt++) {
                String a = auths[cnt];
                if( a.equalsIgnoreCase("")){
                    continue;
                }
                a = a.replace("-", "").replace("PSK", "").replace("CCMP", "");
                if (WifiConfig.WIFI_AUTH_TYPE[authCnt].equalsIgnoreCase(a)) {
                    auth = WifiConfig.WIFI_AUTH_TYPE[authCnt];
                    break;
                }
            }
        }
        wifiModeData.setA(auth + "," + wifiVo.getPassword());
        wifiModeRequestVo.setDp(wifiModeData);

        String json = new Gson().toJson(wifiModeRequestVo) + "\n\n";

        return json;
    }

    private String getNodeListJsonData(){
        CommonDataVo commonDataVo = new CommonDataVo(HttpConfig.NODE_LIST_MSG, HttpConfig.NODE_LIST_CMD);
        commonDataVo.setTr(HttpConfig.NODE_LIST_TR);
        commonDataVo.setTm(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        NodeListRequestVo nodeListRequestVo = new NodeListRequestVo(commonDataVo);

        String json = new Gson().toJson(nodeListRequestVo) + "\n\n";

        return json;
    }

    private Runnable showResultMessage = new Runnable() {
        @Override
        public void run() {
            if( mResult == UDPConfig.UDP_CONNECT_FAIL){
                Toast.makeText(mActivity, "AP 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
            } else if( mResult == UDPConfig.UDP_REQUEST_FAIL){
                Toast.makeText(mActivity, "AP 요청에 실패하였습니다 (요청 실패)", Toast.LENGTH_SHORT).show();
            } else if( mResult == UDPConfig.UDP_RESPONSE_FAIL){
                Toast.makeText(mActivity, "AP 요청에 실패하였습니다 (응답 실패)", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
