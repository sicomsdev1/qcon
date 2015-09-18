package com.sicoms.smartplug.plug.service;


import android.content.Context;
import android.util.Log;

import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.network.udp.UDPBroadcaster;
import com.sicoms.smartplug.network.udp.UDPClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class PlugUDPService {
    private static final String TAG = PlugUDPService.class.getSimpleName();

    private Context mContext;
    private UDPClient.UDPResponseCallbacks mClientCallbacks;

    public PlugUDPService(Context activity){
        mContext = activity;
    }
    public void setOnUDPResponseCallbacks(UDPClient.UDPResponseCallbacks callbacks){
        mClientCallbacks = callbacks;
    }

    public void requestOnOffMessageByRouter(PlugVo plugVo, String onoff){
        UDPClient udpClient = new UDPClient();
        if( mClientCallbacks != null) {
            udpClient.setOnUDPResponseCallbacks(mClientCallbacks);
        }
        List<String> plugIdList = new ArrayList<>();
        plugIdList.add(plugVo.getPlugId());
        PlugService service = new PlugService(mContext);
        String json = service.getControlNodeRequestJsonData(plugIdList, onoff) + "\n\n";

        udpClient.execute(plugVo.getBssid(), json);

        Log.d(TAG, "Router Request On/Off (" + onoff + ")");
        Log.d(TAG, "Json : " + json);
    }

    public void requestOnOffMessageTypeAP(String onoff){
        UDPClient udpClient = new UDPClient();
        if( mClientCallbacks != null) {
            udpClient.setOnUDPResponseCallbacks(mClientCallbacks);
        }
        PlugService service = new PlugService(mContext);
        String json = service.getControlNodeRequestJsonData(null, onoff) + "\n\n";

        udpClient.execute(json);

        Log.d(TAG, "AP Request On/Off (" + onoff + ")");
        Log.d(TAG, "Json : " + json);
    }

    public void requestAllOnOffMessageTypeRouter(List<PlugVo> plugVoList, String onoff){
        UDPClient udpClient = new UDPClient();

        PlugService service = new PlugService(mContext);
        for(int voCnt=0; voCnt<plugVoList.size(); voCnt++) {
            PlugVo plugVo = plugVoList.get(voCnt);
            List<String> plugIdList = new ArrayList<>();
            plugIdList.add(plugVo.getPlugId());
            String json = service.getControlNodeRequestJsonData(plugIdList, onoff) + "\n\n";
            udpClient.execute(plugVo.getBssid(), json);
        }
    }

    public void requestAllOnOffMessageTypeGW(List<PlugVo> plugVoList, String onoff){
        UDPClient udpClient = new UDPClient();

        HashMap<String, List<String>> gwMap = new HashMap<>(); // Key : Gateway IP, Value : Gateway IP 에 속한 Plug ID
        List<String> gwIpList = new ArrayList<>();
        gwIpList.add(plugVoList.get(0).getGatewayIp()); // 초기화로 첫번째 Gateway IP 저장
        for(int voCnt=0; voCnt<plugVoList.size(); voCnt++){
            PlugVo plugVo = plugVoList.get(voCnt);
            for( int gwCnt=0; gwCnt<gwIpList.size(); gwCnt++){ // Gateway IP List 를 탐색하여 다른 IP가 있다면 HashMap 에 추가하는 로직.
                String hasGwIp = gwIpList.get(gwCnt);
                String plugGwIp = plugVo.getGatewayIp();
                if( !plugGwIp.equalsIgnoreCase(hasGwIp)){
                    gwIpList.add(plugGwIp);
                    List<String> plugIdList = gwMap.get(plugGwIp); // 현재 저장돼있는 Gateway IP 의 Value를 불러온다
                    if( plugIdList == null){
                        plugIdList = new ArrayList<>();
                    }
                    plugIdList.add(plugVo.getPlugId());
                    gwMap.put(plugGwIp, plugIdList); // 새로 저장한 Value 를 업데이트한다.
                }
            }
        }

        PlugService service = new PlugService(mContext);

        Iterator<String> iterator = gwMap.keySet().iterator();
        while(iterator.hasNext()) {
            String gwIp = iterator.next();
            List<String> plugIdList = gwMap.get(gwIp);
            String json = service.getControlNodeRequestJsonData(plugIdList, onoff) + "\n\n";
            udpClient.execute(gwIp, json);
            Log.d(TAG, "Request On/Off (" + onoff + ")");
            Log.d(TAG, "Json : " + json);
        }
    }
//    public void requestLastData(String ip, String json){
//        UDPBroadcaster udpBroadcasterAsync = new UDPBroadcaster();
//        if( mBroadcastCallbacks != null) {
//            udpBroadcasterAsync.setOnUDPResponseCallbacks(mBroadcastCallbacks);
//        }
//        String params[] = {ip, json};
//        udpBroadcasterAsync.sendBroadcast(params);
//    }
}
