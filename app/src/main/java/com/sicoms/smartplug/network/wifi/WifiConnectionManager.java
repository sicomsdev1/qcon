package com.sicoms.smartplug.network.wifi;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.WifiVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-03-24.
 */
public class WifiConnectionManager {

    private Context mContext;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResultList;

    WifiScanResultCallbacks mScanResultCallbacks;

    public WifiConnectionManager(Context context){
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public static boolean isWifiEnabled(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public void scanWifiDevice(){
        if (!mWifiManager.isWifiEnabled()) {
            return;
        }

        mScanResultList = mWifiManager.getScanResults();
        List<WifiVo> wifiVoList = new ArrayList<>();
        for(ScanResult result : mScanResultList){
            if( result.SSID.contains(SPConfig.PLUG_SSID_WIFI_NAME)) { // Smart Plug WIFI 만 저장
                if( result.SSID.contains(SPConfig.PLUG_SSID_WIFI_AP_NAME)) {
                    wifiVoList.add(new WifiVo(result.SSID, result.BSSID, result.capabilities));
                }
            }
        }
        if( mScanResultCallbacks != null) {
            mScanResultCallbacks.onWifiScanResult(wifiVoList);
        }
    }

    public interface WifiScanResultCallbacks {
        void onWifiScanResult(List<WifiVo> wifiList);
    }

    public void setOnWifiScanResultCallbacks(final WifiScanResultCallbacks callbacks){
        mScanResultCallbacks = callbacks;
    }

    public WifiVo getConnectedWifiInfo(){
        WifiVo wifiVo = new WifiVo();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        List<ScanResult> scanResultList = mWifiManager.getScanResults();
        for(ScanResult result : scanResultList){
            if( result.BSSID.equalsIgnoreCase(wifiInfo.getBSSID())){
                wifiVo.setSsid(result.SSID);
                wifiVo.setBssid(result.BSSID);
                wifiVo.setSecurityType(result.capabilities);
            }
        }
        
        return wifiVo;
    }

    public String getScanResultSecurity(String securityType) {
        final String[] securityModes = { "WEP", "PSK", "EAP", "AP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (securityType.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    public boolean connectWifi(WifiVo wifiVo){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        String ssid = wifiVo.getSsid();
        String password = wifiVo.getPassword();
        String securityMode = getScanResultSecurity(wifiVo.getSecurityType());

        wifiConfiguration.SSID = "\"".concat(ssid) + "\"";;
        wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
        wifiConfiguration.priority = 40;
        if (securityMode.equalsIgnoreCase("OPEN")) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (securityMode.equalsIgnoreCase("WEP")) {
            wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (securityMode.equalsIgnoreCase("AP")){
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.preSharedKey = "\"".concat(password).concat("\"");
        } else {
            wifiConfiguration.preSharedKey = "\"" + password + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        }
        mWifiManager.setWifiEnabled(true);
        int res = mWifiManager.addNetwork(wifiConfiguration);
        if( res != -1) {
            mWifiManager.enableNetwork(res, true);
        }
        boolean changeHappen = mWifiManager.saveConfiguration();
        int connectCount = 0;
        if(changeHappen){
            boolean isConnected = isConnected();
            String connectedSsid = getWiFiInfo().getSsid().replace("\"", "");
            while(!ssid.equalsIgnoreCase(connectedSsid)){
                try {
                    if( connectCount++ > 5){ // 5초 동안 연결 안될 시
                        return false;
                    }
                    isConnected = isConnected();
                    connectedSsid = getWiFiInfo().getSsid().replace("\"", "");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }else{
            //Log.d(TAG, "*** Change NOT happen");
            return false;
        }
    }

    private boolean isConnected(){
        ConnectivityManager connManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiState = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiState.isConnected();
    }

    public WifiVo getWiFiInfo()
    {
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        String bssid = wifiInfo.getBSSID();

        WifiVo wifiVo = new WifiVo(ssid, bssid);

        return wifiVo;
    }
}
