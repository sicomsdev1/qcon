package com.sicoms.smartplug.domain;

import com.sicoms.smartplug.network.bluetooth.util.DeviceController;

/**
 * Created by gudnam on 2015. 5. 30..
 */
public class RegDeviceVo {
    private int plugTypeIconImg = 0;
    private int plugIconImg = 0;
    private String plugId = "";
    private boolean isRegDevice = false;
    private String networkType = "";

    // Device 속성
    private int deviceId = 0; // Bluetooth Mode
    private int uuidHash = 0; // Bluetooth Mode
    private String blPassword = ""; // Bluetooth Mode
    private String bssid = ""; // AP Mode
    private String ipAddress = ""; // GW & Router Mode

    public RegDeviceVo(int plugTypeIconImg, int plugIconImg, String plugId, String networkType){
        this.plugTypeIconImg = plugTypeIconImg;
        this.plugIconImg = plugIconImg;
        this.plugId = plugId;
        this.networkType = networkType;
    }

    public int getPlugTypeIconImg() {
        return plugTypeIconImg;
    }

    public void setPlugTypeIconImg(int plugTypeIconImg) {
        this.plugTypeIconImg = plugTypeIconImg;
    }

    public int getPlugIconImg() {
        return plugIconImg;
    }

    public void setPlugIconImg(int plugIconImg) {
        this.plugIconImg = plugIconImg;
    }

    public String getPlugId() {
        return plugId;
    }

    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public boolean isRegDevice() {
        return isRegDevice;
    }
    public void setIsRegDevice(boolean isRegDevice) {
        this.isRegDevice = isRegDevice;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getUuidHash() {
        return uuidHash;
    }

    public void setUuidHash(int uuidHash) {
        this.uuidHash = uuidHash;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getBlPassword() {
        return blPassword;
    }

    public void setBlPassword(String blPassword) {
        this.blPassword = blPassword;
    }
}
