package com.sicoms.smartplug.plug.service;

import android.app.Activity;
import android.widget.Toast;

import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.network.bluetooth.BLSender;
import com.sicoms.smartplug.network.udp.UDPClient;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class PlugBLService {
    private static final String TAG = PlugBLService.class.getSimpleName();

    private Activity mActivity;

    public PlugBLService(Activity activity){
        mActivity = activity;
    }

    private String mOnOff;
    public void requestOnOffMessage(PlugVo plugVo, String json, String on_off){
        mOnOff = on_off;

        if( plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)) { // BL 모드
            BLSender blSender = new BLSender(mActivity);
            blSender.sendData(json);
        }
    }
}
