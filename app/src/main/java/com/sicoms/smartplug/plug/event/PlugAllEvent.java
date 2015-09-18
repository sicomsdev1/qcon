package com.sicoms.smartplug.plug.event;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.csr.mesh.PowerModelApi.PowerState;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DbApVo;
import com.sicoms.smartplug.domain.ControlNodeDataVo;
import com.sicoms.smartplug.domain.ControlNodeResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.main.service.RealtimeService;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.network.udp.UDPConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.adapter.PlugAdapter;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.PlugUDPService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class PlugAllEvent implements View.OnClickListener, PlugAdapter.OnItemClickListener,  UDPClient.UDPResponseCallbacks {
    private static final String TAG = PlugAllEvent.class.getSimpleName();

    private Context mContext;
    private PlugAllService mService;
    private PlugVo mPlugVo;
    private ControlResultCallbacks mControlCallbacks;
    private HttpResponseCallbacks mHttpCallbacks;
    private Handler mHandler;

    public PlugAllEvent(Context context) {
        mContext = context;
        mService = new PlugAllService(mContext);
        mHandler = new Handler();
    }

    public void setOnControlResultCallbacks(ControlResultCallbacks callbacks){
        mControlCallbacks = callbacks;
    }

    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mHttpCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sync_btn :
                SPUtil.showDialog(mContext);
                mService.setOnHttpResponseCallbacks(mHttpCallbacks);
                mService.requestSelectPlugList();
                break;
            // Plug Main
//            case R.id.iv_all_power_btn:
//                boolean isStartOn = !v.isSelected();
//                List<PlugVo> allPlugVoList = mService.selectDbPlugList();
//                List<PlugVo> blPlugVoList = new ArrayList<>();
//                List<PlugVo> apPlugVoList = new ArrayList<>();
//                List<PlugVo> stPlugVoList = new ArrayList<>();
//                List<PlugVo> gwPlugVoList = new ArrayList<>();
//                for(int voCnt=0; voCnt<allPlugVoList.size(); voCnt++){
//                    PlugVo plugVo = allPlugVoList.get(voCnt);
//                    String type = plugVo.getNetworkType();
//                    if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
//                        blPlugVoList.add(plugVo);
//                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
//                        apPlugVoList.add(plugVo);
//                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
//                        stPlugVoList.add(plugVo);
//                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)){
//                        gwPlugVoList.add(plugVo);
//                    }
//                }
//
//                // TODO : Bluetooth 전체 제어 할 때 0 이 아닌 그룹으로 묶어서 해야 함
//                if( blPlugVoList.size() > 0) {
//                    if (!MainActivity.stBluetoothManager.isConnected()) {
//                        SPUtil.showToast(mContext, "블루투스 장비와 연결되지 않았습니다.");
//                    }
//                    PowerState onoffState = isStartOn ? PowerState.ON : PowerState.OFF;
//                    //MainActivity.stBluetoothManager.setOnBLControlResultCallbacks(this);
//                    MainActivity.stBluetoothManager.setSelectedDeviceId(0); // 추후 그룹1 이 전체로 하여 변경
//                    MainActivity.stBluetoothManager.setLightPower(onoffState);
//                }
//                if( apPlugVoList.size() > 0){
//                    String currentSsid = new WifiConnectionManager(mContext).getConnectedWifiInfo().getSsid();
//                    for(int apCnt=0; apCnt<apPlugVoList.size(); apCnt++){
//                        PlugVo plugVo = apPlugVoList.get(apCnt);
//                        String plugSsid = mService.selectDbAp(plugVo).getSsId();
//                        if( currentSsid.equalsIgnoreCase(plugSsid)){
//                            String onoff = isStartOn ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
//                            PlugUDPService udpService = new PlugUDPService(mContext);
//                            udpService.setOnUDPResponseCallbacks(this);
//                            udpService.requestOnOffMessageTypeAP(onoff);
//                            break;
//                        }
//                    }
//                }
//                if( stPlugVoList.size() > 0 ){
//                    String onoff = isStartOn ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
//                    PlugUDPService udpService = new PlugUDPService(mContext);
//                    udpService.setOnUDPResponseCallbacks(this);
//                    udpService.requestAllOnOffMessageTypeRouter(stPlugVoList, onoff);
//                }
//                if( gwPlugVoList.size() > 0){
//                    String onoff = isStartOn ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
//                    PlugUDPService udpService = new PlugUDPService(mContext);
//                    udpService.setOnUDPResponseCallbacks(this);
//                    udpService.requestAllOnOffMessageTypeGW(gwPlugVoList, onoff);
//                }
//
//                mControlCallbacks.onGroupControlOnOffResult(isStartOn);
//
//                v.setSelected(isStartOn);
//                //SPUtil.showDialog(mContext, 3000);
//
//                break;
        }
    }

    // On & Off Control
    @Override
    public void onItemClick(View view, PlugVo theVo) {
        mPlugVo = theVo;
        String type = mPlugVo.getNetworkType();
        if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
            //MainActivity.stBluetoothManager.setOnBLControlResultCallbacks(this);
            if( !MainActivity.stBluetoothManager.isConnected()){
                SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
                return;
            }
            if( !mService.isEqualBluetoothPassword(mPlugVo)){
                SPUtil.showToast(mContext, "블루투스 비밀번호가 틀렸습니다. Place Setting 메뉴에서 비밀번호를 변경해주세요.");
                return;
            }
            int deviceId = Integer.parseInt(mPlugVo.getUuid());
            boolean isOn = !mPlugVo.isOn();
            PowerState onoffState = isOn ? PowerState.ON : PowerState.OFF;
            MainActivity.stBluetoothManager.setSelectedDeviceId(deviceId);
            MainActivity.stBluetoothManager.setLightPower(onoffState);
            mControlCallbacks.onControlOnOffResult(mPlugVo, isOn);
        } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)) {
            String onoff = !mPlugVo.isOn() ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
            PlugUDPService udpService = new PlugUDPService(mContext);
            udpService.setOnUDPResponseCallbacks(this);
            udpService.requestOnOffMessageByRouter(mPlugVo, onoff);
        } else if (type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
            String currentSsid = new WifiConnectionManager(mContext).getConnectedWifiInfo().getSsid();
            DbApVo dbApVo = mService.selectDbAp(mPlugVo);
            if( dbApVo == null){
                return;
            }
            String plugSsid = dbApVo.getSsId();
            if( currentSsid.equalsIgnoreCase(plugSsid)) {
                String onoff = !mPlugVo.isOn() ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
                PlugUDPService udpService = new PlugUDPService(mContext);
                udpService.setOnUDPResponseCallbacks(this);
                udpService.requestOnOffMessageTypeAP(onoff);
            } else {
                SPUtil.showToast(mContext, plugSsid + " Wi-Fi에 접속 후 다시 시도해주세요.");
            }
        }
    }

//    @Override
//    public void onBLControlOnOffResult(String deviceId, String onoff) {
//        String status = onoff;
//        PlugDBService dbService = new PlugDBService(mContext);
//        dbService.updateDbLastStatusData(deviceId, status);
//        mControlCallbacks.onControlOnOffResult(status == SPConfig.STATUS_ON);
//    }
//
//    @Override
//    public void onBLAllControlOnOffResult(List<DbLastDataVo> dbLastDataVoList) {
//        PlugDBService dbService = new PlugDBService(mContext);
//        dbService.updateDbLastStatusDataList(dbLastDataVoList);
//        for(int voCnt=0; voCnt<dbLastDataVoList.size(); voCnt++) {
//            String status = dbLastDataVoList.get(voCnt).getOnOff();
//            mControlCallbacks.onControlOnOffResult(status == SPConfig.STATUS_ON);
//        }
//        SPUtil.dismissDialog();
//    }

    @Override
    public void onUDPResponseResultStatus(final int result, String response) {
        try {
            if(result == UDPConfig.UDP_SUCCESS) {
                ControlNodeResponseVo controlNodeResponseVo = new Gson().fromJson(response, ControlNodeResponseVo.class);
                if (controlNodeResponseVo != null) {
                    final int controlResult = Integer.parseInt(controlNodeResponseVo.getRet());
                    Log.d(TAG, "UDP Response Result : " + controlResult);
                    if (controlResult == HttpConfig.CONTROL_SUCCESS) {
                        if( controlNodeResponseVo != null && controlNodeResponseVo.getDp() != null) {
                            ControlNodeDataVo controlNodeDataVo = controlNodeResponseVo.getDp().get(0);
                            String onoffStatus = !mPlugVo.isOn() ? SPConfig.STATUS_ON : SPConfig.STATUS_OFF;
                            if (controlNodeDataVo != null) {
                                onoffStatus = controlNodeDataVo.getS();
                            }
//                            PlugDBService dbService = new PlugDBService(mContext);
//                            dbService.updateDbLastStatusData(mPlugVo.getPlugId(), onoffStatus);
                            mControlCallbacks.onControlOnOffResult(mPlugVo, onoffStatus == SPConfig.STATUS_ON);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e){
            e.printStackTrace();
        }
    }

    private Runnable mReceiveLastDataTimeout = new Runnable() {
        @Override
        public void run() {
            RealtimeService realtimeService = new RealtimeService(mContext);
            realtimeService.runService();
            mControlCallbacks.onControlOnOffResult(null, true);
        }
    };
}
